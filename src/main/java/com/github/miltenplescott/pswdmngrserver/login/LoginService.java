/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.login;

import javax.persistence.EntityExistsException;

/**
 *
 * @author Milten Plescott
 */
public class LoginService {


    // TODO: inject
    private LoginDao loginDao;

    void createLogin(String loginTarget) {
        Login login = new Login();
        login.setTarget(loginTarget);
        try {
            loginDao.createLogin(login);
        }
        catch (EntityExistsException e) {
            System.err.println("Login with ID=" + login.getId() + " already exists.");
        }
    }

}
