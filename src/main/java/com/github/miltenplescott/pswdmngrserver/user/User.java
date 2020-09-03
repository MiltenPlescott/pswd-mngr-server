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
import javax.persistence.Entity;

/**
 *
 * @author Milten Plescott
 */
@Entity
//@Table(name = "\"User\"")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

//    @Column(columnDefinition = "BINARY(32)")
//    private String masterPswd;
//
//    @Column(columnDefinition = "BINARY(16)")
//    private String salt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public String getMasterPswd() {
//        return masterPswd;
//    }
//
//    public void setMasterPswd(String masterPswd) {
//        this.masterPswd = masterPswd;
//    }
//
//    public String getSalt() {
//        return salt;
//    }
//
//    public void setSalt(String salt) {
//        this.salt = salt;
//    }

}
