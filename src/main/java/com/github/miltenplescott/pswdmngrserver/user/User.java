/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.BaseEntity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
//@Table(name = "\"User\"")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    static final int USERNAME_MIN_LENGTH = 3;

    @Transient
    static final int USERNAME_MAX_LENGTH = 50;

    @Transient
    static final String USERNAME_REGEX = "^[a-zA-Z0-9]*$";

    @Column(nullable = false, unique = true, length = USERNAME_MAX_LENGTH)
    private String username;

    @Column(name = "master_pswd", nullable = false)
    private byte[] masterPswd;

    @Column(nullable = false)
    private byte[] salt;

    public User() {
        super();
    }

    public User(String username, byte[] masterPswd) {
        this.username = username;
        this.masterPswd = masterPswd.clone();
    }

    @NotNull(message = "Username must not be null.")
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "Username length must be between {min} and {max} characters.")
    @Pattern(regexp = USERNAME_REGEX, message = "Username must follow {regexp} pattern.")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getMasterPswd() {
        return masterPswd.clone();
    }

    public void setMasterPswd(byte[] masterPswd) {
        this.masterPswd = masterPswd.clone();
    }

    public byte[] getSalt() {
        return salt.clone();
    }

    public void setSalt(byte[] salt) {
        this.salt = salt.clone();
    }

}
