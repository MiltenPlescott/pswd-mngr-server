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
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author Milten Plescott
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
public class UserDaoImpl implements UserDao {

    @PersistenceContext(unitName = "pswd-mngr-persistance-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    @Override
    public void createUser(User user) throws EntityExistsException {
        entityManager.persist(user);
    }

    @Override
    public Optional<User> findUser(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public List<User> findAllUsingCriteria() {
        CriteriaQuery<User> cq = entityManager.getCriteriaBuilder().createQuery(User.class);
        cq.select(cq.from(User.class));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public User updateUser(User user) {
        return entityManager.merge(user);
    }

    @Override
    public void deleteUser(User user) {
        entityManager.remove(user);
    }

}
