/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.annotation.JsonbTransient;

@JsonbPropertyOrder({"token", "tokenType", "expiration"})
public class AuthTokenResponseDto {
// inspired by: https://tools.ietf.org/html/rfc6749#section-4.2.2

    @JsonbTransient
    static final String BEARER = "bearer";

    @JsonbProperty("access_token")
    private String token;

    @JsonbProperty("token_type")
    private String tokenType;

    @JsonbProperty("expiration_ms")
    private long expiration;

    public AuthTokenResponseDto() {
    }

    public AuthTokenResponseDto(String token, String tokenType, long expiration) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

}
