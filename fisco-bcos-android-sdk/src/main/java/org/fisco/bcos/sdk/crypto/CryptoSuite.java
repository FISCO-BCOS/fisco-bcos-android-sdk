/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.crypto;

import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.crypto.exceptions.UnsupportedCryptoTypeException;
import org.fisco.bcos.sdk.crypto.hash.Hash;
import org.fisco.bcos.sdk.crypto.hash.Keccak256;
import org.fisco.bcos.sdk.crypto.hash.SM3Hash;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.ECDSAKeyPair;
import org.fisco.bcos.sdk.crypto.keypair.SM2KeyPair;
import org.fisco.bcos.sdk.crypto.signature.ECDSASignature;
import org.fisco.bcos.sdk.crypto.signature.SM2Signature;
import org.fisco.bcos.sdk.crypto.signature.Signature;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.log.Logger;
import org.fisco.bcos.sdk.log.LoggerFactory;
import org.fisco.bcos.sdk.model.CryptoType;

public class CryptoSuite {

    private static Logger logger = LoggerFactory.getLogger(CryptoSuite.class);

    public final int cryptoTypeConfig;

    public final Signature signatureImpl;
    public final Hash hashImpl;
    private final CryptoKeyPair keyPairFactory;
    private CryptoKeyPair cryptoKeyPair;
    private ConfigOption config;

    /**
     * init the common crypto implementation according to the crypto type
     *
     * @param cryptoTypeConfig the crypto type config number
     */
    public CryptoSuite(int cryptoTypeConfig) {
        this.cryptoTypeConfig = cryptoTypeConfig;
        if (this.cryptoTypeConfig == CryptoType.ECDSA_TYPE) {
            this.signatureImpl = new ECDSASignature();
            this.hashImpl = new Keccak256();
            this.keyPairFactory = new ECDSAKeyPair();
        } else if (this.cryptoTypeConfig == CryptoType.SM_TYPE) {
            this.signatureImpl = new SM2Signature();
            this.hashImpl = new SM3Hash();
            this.keyPairFactory = new SM2KeyPair();
        } else {
            throw new UnsupportedCryptoTypeException(
                    "only support "
                            + CryptoType.ECDSA_TYPE
                            + "/"
                            + CryptoType.SM_TYPE
                            + " crypto type");
        }
        // create keyPair randomly
        createKeyPair();
    }

    public void setConfig(ConfigOption config) {
        this.config = config;
        this.keyPairFactory.setConfig(config);
    }

    public int getCryptoTypeConfig() {
        return cryptoTypeConfig;
    }

    public Signature getSignatureImpl() {
        return signatureImpl;
    }

    public Hash getHashImpl() {
        return hashImpl;
    }

    public String hash(final String inputData) {
        return hashImpl.hash(inputData);
    }

    public byte[] hash(final byte[] inputBytes) {
        return hashImpl.hash(inputBytes);
    }

    public SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair) {
        return signatureImpl.sign(message, keyPair);
    }

    public SignatureResult sign(final String message, final CryptoKeyPair keyPair) {
        return signatureImpl.sign(message, keyPair);
    }

    public boolean verify(final String publicKey, final String message, final String signature) {
        return signatureImpl.verify(publicKey, message, signature);
    }

    public boolean verify(final String publicKey, final byte[] message, final byte[] signature) {
        return signatureImpl.verify(publicKey, message, signature);
    }

    public CryptoKeyPair createKeyPair() {
        this.cryptoKeyPair = this.keyPairFactory.generateKeyPair();
        this.cryptoKeyPair.setConfig(config);
        return this.cryptoKeyPair;
    }

    public CryptoKeyPair derivePublicKey(String hexedPrivateKey) {
        this.cryptoKeyPair = this.keyPairFactory.derivePublicKey(hexedPrivateKey);
        this.cryptoKeyPair.setConfig(config);
        return this.cryptoKeyPair;
    }

    public void setCryptoKeyPair(CryptoKeyPair cryptoKeyPair) {
        this.cryptoKeyPair = cryptoKeyPair;
        this.cryptoKeyPair.setConfig(config);
    }

    public CryptoKeyPair getCryptoKeyPair() {
        return this.cryptoKeyPair;
    }

    public ConfigOption getConfig() {
        return this.config;
    }

    public CryptoKeyPair getKeyPairFactory() {
        return this.keyPairFactory;
    }
}
