/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.CryptoUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class AuthTokenManager {

    public static final int TOKEN_LENGTH_BYTES = 16;
    public static final Duration EXPIRATION_DURATION_MINUTES = Duration.ofMinutes(15);

    // tokenToUsername and tokenToInstant maps have to be kept in sync
    // i.e. every time when inserting/deleting, that operation has to be done on both maps
    private BiMap<String, String> tokenToUsername = HashBiMap.create();  // key = token, value = username
    private Map<String, Instant> tokenToInstant = new HashMap<>();  // key = token, value = expiration

    public String generateToken(String username) {
        String token = generateToken();
        while (tokenToUsername.containsKey(token)) {
            // make sure tokens are unique in addition to being random
            token = generateToken();
        }
        // prior to insertion, forcePut deletes any entry(key==token) and any entry(value==username)
        tokenToUsername.forcePut(token, username);  // keep maps in sync!
        tokenToInstant.put(token, Instant.now().plus(EXPIRATION_DURATION_MINUTES));  // keep maps in sync!
        return token;
    }

    public long getTokenExpirationMs(String token) {
        return tokenToInstant.get(token).toEpochMilli();
    }

    public boolean isTokenValid(String token) {
        if (!tokenToUsername.containsKey(token)) {
            return false;
        }
        return tokenToInstant.get(token).isAfter(Instant.now());
    }

    public String getUsername(String token) {
        return tokenToUsername.get(token);
    }

    public void deleteTokenForUsername(String username) {
        if (tokenToUsername.containsValue(username)) {
            String token = tokenToUsername.inverse().get(username);
            tokenToUsername.remove(token);  // keep maps in sync!
            tokenToInstant.remove(token);  // keep maps in sync!
        }
    }

    // returns token
    private String generateToken() {
        byte[] bytes = CryptoUtils.genRandomBytes(TOKEN_LENGTH_BYTES);
        return CryptoUtils.encodeToken(bytes);
    }

    @Schedule(hour = "*", minute = "*/30", persistent = false)
    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> entries = tokenToInstant.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Instant> entry = entries.next();
            if (entry.getValue().isBefore(now)) {
                entries.remove();  // keep maps in sync!
                tokenToUsername.remove(entry.getKey());  // keep maps in sync!
            }
        }
    }

}
