/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.login;

import com.github.miltenplescott.pswdmngrserver.GenericDao;
import javax.persistence.EntityExistsException;

/**
 *
 * @author Milten Plescott
 */
public class LoginService {

    // TODO: inject
    private GenericDao<Login> loginDao; // = new GenericDaoImpl<>(Login.class);

    void createLogin(String loginTarget) {
        Login login = new Login();
        login.setTarget(loginTarget);
        try {
            loginDao.create(login);
        }
        catch (EntityExistsException e) {
            System.err.println("Login with ID=" + login.getId() + " already exists.");
        }
    }

}
