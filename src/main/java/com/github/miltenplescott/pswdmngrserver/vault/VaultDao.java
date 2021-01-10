/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.GenericDao;
import java.util.List;

public interface VaultDao extends GenericDao<VaultEntry> {

    public List<VaultEntry> findAll(long userId);

    public int deleteAll(long userId);

}
