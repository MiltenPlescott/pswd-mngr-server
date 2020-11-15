/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.GenericDaoImpl;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
public class UserDaoImpl extends GenericDaoImpl<User> implements UserDao {
// inspired by PersonDao: https://developer.ibm.com/tutorials/j-genericdao/

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByName(String username) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> user = cq.from(User.class);
//        cq.select(user).where(cb.equal(user.get("username"), username));  // using string
        cq.select(user).where(cb.equal(user.get(User_.username), username));  // using metamodel
        List<User> resultList = getEntityManager().createQuery(cq).getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        else {
            if (resultList.size() > 1) {
                throw new AssertionError("Username is unique.");
            }
            return Optional.of(resultList.get(0));
        }
    }

    @Override
    public boolean userWithNameExists(String username) {
        return findByName(username).isPresent();
    }


}
