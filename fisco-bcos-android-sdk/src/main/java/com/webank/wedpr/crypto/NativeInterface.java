// Copyright 2020 WeDPR Lab Project Authors. Licensed under Apache-2.0.

package com.webank.wedpr.crypto;

/** Native interface for Crypto client. */
public class NativeInterface {

    // TODO: Check this path.
    static {
        System.loadLibrary("ffi_java_crypto");
    }

    // JNI function section.
    public static native CryptoResult secp256k1GenKeyPair();

    public static native CryptoResult secp256k1Sign(String priKey, String messageHash);

    public static native CryptoResult secp256k1Verify(
            String pubKey, String message, String signature);

    public static native CryptoResult keccak256Hash(String message);

    public static native CryptoResult secp256k1DerivePublicKey(String priKey);

    public static native CryptoResult sm2GenKeyPair();

    public static native CryptoResult sm2SignFast(String priKey, String pubKey, String messageHash);

    public static native CryptoResult sm2Verify(String pubKey, String message, String signature);

    public static native CryptoResult sm3Hash(String message);

    public static native CryptoResult sm2DerivePublicKey(String priKey);
}
