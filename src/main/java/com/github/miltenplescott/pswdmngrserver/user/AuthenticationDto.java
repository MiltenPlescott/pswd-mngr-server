/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.json.bind.config.PropertyOrderStrategy;

@JsonbPropertyOrder(PropertyOrderStrategy.LEXICOGRAPHICAL)
public class AuthenticationDto {

    private String username;
    private String masterPswd;

    public AuthenticationDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMasterPswd() {
        return masterPswd;
    }

    public void setMasterPswd(String masterPswd) {
        this.masterPswd = masterPswd;
    }

    @Override
    public String toString() {
        return "CreateAccountDto{" + "username=" + username + ", masterPswd=" + masterPswd + '}';
    }

}
