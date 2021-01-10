/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import java.util.Arrays;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;

@JsonbPropertyOrder({"id", "encData"})
public class VaultEntryDto {

    @JsonbProperty
    private Long id;

    @JsonbProperty("enc_data")
    private byte[] encData;

    public VaultEntryDto() {
    }

    public VaultEntryDto(Long id, byte[] encData) {
        this.id = id;
        this.encData = encData.clone();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getEncData() {
        return encData.clone();
    }

    public void setEncData(byte[] encData) {
        this.encData = encData.clone();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, Arrays.hashCode(this.encData));
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
        final VaultEntryDto other = (VaultEntryDto) obj;
        return Objects.equals(this.id, other.id)
            && Arrays.equals(this.encData, other.encData);
    }

}
