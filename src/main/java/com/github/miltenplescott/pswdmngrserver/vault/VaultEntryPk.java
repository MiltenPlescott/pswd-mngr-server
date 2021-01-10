/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import java.io.Serializable;
import java.util.Objects;


public class VaultEntryPk implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long user;

    public VaultEntryPk() {
    }

    public VaultEntryPk(Long id, Long user) {
        this.id = id;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VaultEntryPk other = (VaultEntryPk) obj;
        return Objects.equals(this.id, other.id)
            && Objects.equals(this.user, other.user);
    }

}
