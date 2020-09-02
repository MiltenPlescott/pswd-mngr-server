/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import javax.persistence.EntityExistsException;

/**
 *
 * @author Milten Plescott
 */
public class UserService {

    // TODO: inject
    private UserDao userDao;

    void createUser(String userName) {
        User user = new User();
        user.setName(userName);
        try {
            userDao.createUser(user);
        }
        catch (EntityExistsException e) {
            System.err.println("User with ID=" + user.getId() + " already exists.");
        }
    }

}
