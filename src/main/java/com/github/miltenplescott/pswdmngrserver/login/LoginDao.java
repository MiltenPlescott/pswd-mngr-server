/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.login;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Milten Plescott
 */
public interface LoginDao {

    void createLogin(Login Login);

    Optional<Login> findLogin(Long id);

    List<Login> findAll();

    Login updateLogin(Login Login);

    void deleteLogin(Login Login);

}
