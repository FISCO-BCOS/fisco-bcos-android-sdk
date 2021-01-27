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
package org.fisco.bcos.sdk.crypto.signature;

import android.util.Base64;

import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

import org.fisco.bcos.sdk.crypto.exceptions.SignatureException;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.utils.Hex;
import org.fisco.bcos.sdk.utils.Numeric;

public class ECDSASignature implements Signature {
    private static int INPUT_MESSAGE_SIZE_IN_HEX = 64;

    @Override
    public SignatureResult sign(final String message, final CryptoKeyPair keyPair) {
        // convert signature string to SignatureResult struct
        return new ECDSASignatureResult(signWithStringSignature(message, keyPair));
    }

    @Override
    public SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair) {
        return sign(Hex.toHexString(message), keyPair);
    }

    private void checkInputMessage(final String message) {
        if (message.length() != INPUT_MESSAGE_SIZE_IN_HEX) {
            throw new SignatureException(
                    "Invalid input message " + message + ", must be a hex string of length 64");
        }
    }

    @Override
    public String signWithStringSignature(final String message, final CryptoKeyPair keyPair) {
        String inputMessage = Numeric.cleanHexPrefix(message);
        checkInputMessage(inputMessage);
        String input1 = Base64.encodeToString(Hex.decode(keyPair.getHexPrivateKey()), Base64.NO_WRAP);
        String input2 = Base64.encodeToString(Hex.decode(inputMessage), Base64.NO_WRAP);
        CryptoResult signatureResult =
                NativeInterface.secp256k1Sign(input1, input2);
        // call secp256k1Sign failed
        if (signatureResult.wedprErrorMessage != null
                && !signatureResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Sign with secp256k1 failed:" + signatureResult.wedprErrorMessage);
        }
        // convert signature string to SignatureResult struct
        return Hex.toHexString(Base64.decode(signatureResult.signature, Base64.NO_WRAP));
    }

    @Override
    public boolean verify(final String publicKey, final String message, final String signature) {
        // System.out.println("publicKey: " + publicKey);
        // ("message: " + message);
        // System.out.println("signature: " + signature);
        String inputMessage = Numeric.cleanHexPrefix(message);
        // System.out.println("inputMessage: " + inputMessage);
        checkInputMessage(inputMessage);
        String input1 = Base64.encodeToString(Hex.decode(publicKey), Base64.NO_WRAP);
        String input2 = Base64.encodeToString(Hex.decode(inputMessage), Base64.NO_WRAP);
        String input3 = Base64.encodeToString(Hex.decode(signature), Base64.NO_WRAP);
        // System.out.println("input1: " + input1);
        // System.out.println("input2: " + input2);
        // System.out.println("input3: " + input3);
        CryptoResult verifyResult =
                NativeInterface.secp256k1Verify(input1, input2, input3);
        // call secp256k1verify failed
        if (verifyResult.wedprErrorMessage != null && !verifyResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Verify with secp256k1 failed:" + verifyResult.wedprErrorMessage);
        }
        return verifyResult.booleanResult;
    }

    @Override
    public boolean verify(final String publicKey, final byte[] message, final byte[] signature) {
        return verify(publicKey, Hex.toHexString(message), Hex.toHexString(signature));
    }
}
