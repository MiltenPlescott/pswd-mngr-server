/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.BaseEntity;
import com.github.miltenplescott.pswdmngrserver.user.User;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@IdClass(VaultEntryPk.class)
public class VaultEntry extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 1 B
     */
    @Transient
    static final int ENC_DATA_MIN_LENGTH = 1;

    /**
     * 100 MiB
     */
    @Transient
    static final int ENC_DATA_MAX_LENGTH = 100 * 1024 * 1024;

    @Column(nullable = false)
    private byte[] encData;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user", referencedColumnName = "id")
    private User user;

    public VaultEntry() {
    }

    public VaultEntry(byte[] encData, User user) {
        this.encData = encData;
        this.user = user;
    }

    @NotNull(message = "Encrypted data must not be null.")
    @Size(min = ENC_DATA_MIN_LENGTH, max = ENC_DATA_MAX_LENGTH,
        message = "The size of encrypted data must be at least {min} byte and must not exceed {max} bytes ("
        + (ENC_DATA_MAX_LENGTH / 1024 / 1024) + " MiB).")
    public byte[] getEncData() {
        return (encData == null) ? null : encData.clone();
    }

    public void setEncData(byte[] encData) {
        this.encData = (encData == null ? null : encData.clone());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
