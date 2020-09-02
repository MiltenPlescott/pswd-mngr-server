/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Milten Plescott
 */
public interface UserDao {

    void createUser(User user);

    Optional<User> findUser(Long id);

    List<User> findAll();

    User updateUser(User user);

    void deleteUser(User user);

}
