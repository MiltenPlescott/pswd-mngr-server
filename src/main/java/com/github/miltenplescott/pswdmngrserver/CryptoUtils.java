/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver;

import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

public final class CryptoUtils {

    public static final int SALT_LENGTH_BYTES = 16;
    public static final int KDF_INPUT_LENGTH_BYTES = 32;
    public static final int KDF_OUTPUT_LENGTH_BYTES = 32;
    public static final int KDF_PARALLELISM = 4;
    public static final int KDF_MEMORY_KB = 1024;
    public static final int KDF_ITERATIONS = 10;

    public static final SecureRandom rng = new SecureRandom();

    private CryptoUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability.");
    }

    public static byte[] genRandomBytes(int numberOfBytes) {
        byte[] bytes = new byte[numberOfBytes];
        rng.nextBytes(bytes);
        return bytes;
    }

    public static byte[] genSalt() {
        return genRandomBytes(SALT_LENGTH_BYTES);
    }

    // masterPswd parameter has length KDF_INPUT_LENGTH_BYTES
    public static byte[] kdfAndSalt(byte[] masterPswd, byte[] salt) {
        Argon2Parameters.Builder argonBuilder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id);
        argonBuilder.withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withSalt(salt)
            .withParallelism(KDF_PARALLELISM)
            .withMemoryAsKB(KDF_MEMORY_KB)
            .withIterations(KDF_ITERATIONS);

        Argon2Parameters argonParams = argonBuilder.build();
        Argon2BytesGenerator argonGenerator = new Argon2BytesGenerator();
        argonGenerator.init(argonParams);
        byte[] kdfOutput = new byte[KDF_OUTPUT_LENGTH_BYTES];
        argonGenerator.generateBytes(masterPswd, kdfOutput);
        return kdfOutput;
    }

    public static void clearArray(byte[] array) {
        Arrays.clear(array);
    }

    public static String encodePswd(byte[] pswd) {
        return encodeBase64(pswd);
    }

    public static byte[] decodePswd(String pswd) throws IllegalArgumentException {
        return decodeBase64(pswd);
    }

    public static String encodeToken(byte[] token) {
        return encodeBase64(token);
    }

    public static byte[] decodeToken(String token) throws IllegalArgumentException {
        return decodeBase64(token);
    }

    private static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decodeBase64(String str) throws IllegalArgumentException {
        return Base64.getDecoder().decode(str);
    }

}
