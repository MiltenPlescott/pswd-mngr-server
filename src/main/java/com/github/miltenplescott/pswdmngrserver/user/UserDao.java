/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.GenericDao;
import java.util.Optional;

public interface UserDao extends GenericDao<User> {

    public Optional<User> findByName(String username);

    public boolean userWithNameExists(String username);

}
