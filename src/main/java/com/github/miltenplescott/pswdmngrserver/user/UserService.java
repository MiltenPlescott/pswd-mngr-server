/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.GenericDao;
import javax.persistence.EntityExistsException;

/**
 *
 * @author Milten Plescott
 */
public class UserService {

    // TODO: inject
    private GenericDao<User> userDao; // = new GenericDaoImpl<>(User.class);

    void createUser(String userName) {
        User user = new User();
        user.setName(userName);
        try {
            userDao.create(user);
        }
        catch (EntityExistsException e) {
            System.err.println("User with ID=" + user.getId() + " already exists.");
        }
    }

}
