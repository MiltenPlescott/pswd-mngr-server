/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.BaseEntity;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Milten Plescott
 */
@Entity
public class VaultEntry extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
