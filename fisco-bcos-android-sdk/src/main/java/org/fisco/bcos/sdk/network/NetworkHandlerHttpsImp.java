package org.fisco.bcos.sdk.network;

import android.content.Context;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.fisco.bcos.sdk.client.exceptions.ClientException;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.network.model.CertInfo;

public class NetworkHandlerHttpsImp implements NetworkHandlerInterface {

    private static Logger logger = LoggerFactory.getLogger(NetworkHandlerHttpsImp.class);
    private static final String PROTOCOL = "TLS";
    private static final String KEY_KEYSTORE_TYPE = "PKCS12";
    private static final String ALAIS = "server";
    private String ipPort = "https://127.0.0.1:8170/";
    private CertInfo certInfo;
    private Context context;

    private SSLSocketFactory getSocketFactory(String cerFileName) throws Exception {
        InputStream cerInputStream = null;
        SSLSocketFactory socketFactory;
        try {
            cerInputStream = context.getAssets().open(cerFileName);
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(null, trustManagers);
            socketFactory = sslContext.getSocketFactory();
        } finally {
            if (cerInputStream != null) {
                cerInputStream.close();
            }
        }
        return socketFactory;
    }

    public SSLSocketFactory getSocketFactory(
            String cerFileName, String p12FileName, String password) throws Exception {
        InputStream cerInputStream = null;
        InputStream p12InputStream = null;
        SSLSocketFactory socketFactory;
        try {
            cerInputStream = context.getAssets().open(cerFileName);
            p12InputStream = context.getAssets().open(p12FileName);
            KeyManager[] keyManagers = getKeyManagers(p12InputStream, password);
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(keyManagers, trustManagers);
            socketFactory = sslContext.getSocketFactory();
        } finally {
            if (cerInputStream != null) {
                cerInputStream.close();
            }
            if (p12InputStream != null) {
                p12InputStream.close();
            }
        }
        return socketFactory;
    }

    private SSLContext getSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers)
            throws Exception {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return sslContext;
    }

    private KeyManager[] getKeyManagers(InputStream inputStream, String password) throws Exception {
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_TYPE);
        keyStore.load(inputStream, password.toCharArray());
        keyManagerFactory.init(keyStore, password.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        return keyManagers;
    }

    private TrustManager[] getTrustManagers(InputStream inputStream) throws Exception {
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_TYPE);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate ca = certificateFactory.generateCertificate(inputStream);
        keyStore.load(null, null);
        keyStore.setCertificateEntry(ALAIS, ca);
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        return trustManagers;
    }

    @Override
    public void setIpAndPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public void setCertInfo(CertInfo certInfo) {
        this.certInfo = certInfo;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String onRPCRequest(String requestBodyJsonStr) {

        String cerFileName = certInfo.getCerFileName();
        String p12FileName = certInfo.getP12FileName();
        String password = certInfo.getPassword();
        try {
            SSLSocketFactory socketFactory;
            if (certInfo.getClientAuth()) {
                socketFactory = getSocketFactory(cerFileName, p12FileName, password);
            } else {
                socketFactory = getSocketFactory(cerFileName);
            }
            X509TrustManager trustManager;
            InputStream cerInputStream = null;
            try {
                cerInputStream = context.getAssets().open(cerFileName);
                TrustManager[] trustManagers = getTrustManagers(cerInputStream);
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new ClientException(
                            "unexpected default trust managers: " + Arrays.toString(trustManagers));
                }
                trustManager = (X509TrustManager) trustManagers[0];
            } catch (Exception e) {
                logger.error("getTrustManagers failed, error info: " + e.getMessage());
                throw new ClientException("getTrustManagers failed, error info: " + e.getMessage());
            } finally {
                if (cerInputStream != null) {
                    cerInputStream.close();
                }
            }

            OkHttpClient okHttpClient =
                    new OkHttpClient.Builder()
                            .sslSocketFactory(socketFactory, trustManager)
                            .hostnameVerifier(
                                    (s, sslSession) -> {
                                        logger.info("hostnameVerifier, host name: " + s);
                                        return true;
                                    })
                            .build();
            String URL = ipPort + "Bcos-node-proxy/rpc/v1";
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, requestBodyJsonStr);
            Request request = new Request.Builder().url(URL).post(requestBody).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBodyJsonStr = response.body().string();
                return responseBodyJsonStr;
            }
        } catch (ConnectException e) {
            logger.error("onRPCRequest failed, error info: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
