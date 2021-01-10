/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.CryptoUtils;
import java.security.SecureRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    private static final String EXAMPLE_BASE64_STRING = "FY7iq0Y1ja2loHmMurgM78VW8Kt3PUpK2oKNjSd0Tt8=";

    private static SecureRandom rng;

    public CryptoUtilsTest() {
    }

    @BeforeAll
    public static void initAll() {
        rng = new SecureRandom();
    }

    @BeforeEach
    public void initEach() {
    }

    @AfterEach
    public void tearDownEach() {
    }

    @AfterAll
    public static void tearDownAll() {
    }

    @Test
    public void clearNullArrayTest() {
        byte[] expected = null;
        byte[] actual = null;
        CryptoUtils.clearArray(actual);
        assertArrayEquals(expected, actual, "Clearing a null should not throw any exceptions");
    }

    @Test
    public void clearEmptyArrayTest() {
        byte[] expected = new byte[]{};
        byte[] actual = new byte[]{};
        CryptoUtils.clearArray(actual);
        assertArrayEquals(expected, actual, "Clearing an empty array should not make any changes to the array");
    }

    @Test
    public void clearArrayTest() {
        byte[] expected = new byte[]{0, 0, 0};
        byte[] actual = new byte[]{1, 2, 3};
        CryptoUtils.clearArray(actual);
        assertArrayEquals(expected, actual, "Clearing a non-empty array should result in an array of the same length filled with zeros");
    }

    @Test
    public void encodeTokenTest() {
        byte[] bytes = new byte[20];
        rng.nextBytes(bytes);
        String encoded = CryptoUtils.encodeToken(bytes);
        byte[] decoded = CryptoUtils.decodeToken(encoded);
        assertArrayEquals(bytes, decoded, "Encoding, then decoding, should be the same as the original");
    }

    @Test
    public void decodeTokenTest() {
        byte[] decoded = CryptoUtils.decodeToken(EXAMPLE_BASE64_STRING);
        String encoded = CryptoUtils.encodeToken(decoded);
        assertEquals(EXAMPLE_BASE64_STRING, encoded, "Decoding, then encoding, should be the same as the original");
    }

    @Test
    public void decodeTokenExceptionTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CryptoUtils.decodeToken("some non-base64 symbols: .,<>:/()[]{}'!@#$%^&*-_=+"),
            "Decoding non-base64 string should throw an exception"
        );
    }

    @Test
    public void encodePswdTest() {
        byte[] bytes = new byte[20];
        rng.nextBytes(bytes);
        String encoded = CryptoUtils.encodePswd(bytes);
        byte[] decoded = CryptoUtils.decodePswd(encoded);
        assertArrayEquals(bytes, decoded, "Encoding, then decoding, should be the same as the original");
    }

    @Test
    public void decodePswdTest() {
        byte[] decoded = CryptoUtils.decodePswd(EXAMPLE_BASE64_STRING);
        String encoded = CryptoUtils.encodePswd(decoded);
        assertEquals(EXAMPLE_BASE64_STRING, encoded, "Decoding, then encoding, should be the same as the original");
    }

    @Test
    public void decodePswdExceptionTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CryptoUtils.decodePswd("some non-base64 symbols: .,<>:/()[]{}'!@#$%^&*-_=+"),
            "Decoding non-base64 string should throw an exception"
        );
    }

}
