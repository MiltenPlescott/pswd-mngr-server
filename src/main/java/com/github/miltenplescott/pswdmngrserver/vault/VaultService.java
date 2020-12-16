/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.GenericDao;
import javax.persistence.EntityExistsException;

public class VaultService {

    // TODO: inject
    private GenericDao<VaultEntry> vaultDao; // = new GenericDaoImpl<>(VaultEntry.class);

    void createVaultEntry(String vaultEntryTarget) {
        VaultEntry vaultEntry = new VaultEntry();
        vaultEntry.setTarget(vaultEntryTarget);
        try {
            vaultDao.create(vaultEntry);
        }
        catch (EntityExistsException e) {
            System.err.println("VaultEntry with ID=" + vaultEntry.getId() + " already exists.");
        }
    }

}
