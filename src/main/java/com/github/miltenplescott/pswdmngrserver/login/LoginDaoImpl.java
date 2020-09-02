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
public class LoginDaoImpl implements LoginDao {

    @PersistenceContext(unitName = "pswd-mngr-persistance-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    @Override
    public void createLogin(Login login) throws EntityExistsException {
        entityManager.persist(login);
    }

    @Override
    public Optional<Login> findLogin(Long id) {
        return Optional.ofNullable(entityManager.find(Login.class, id));
    }

    @Override
    public List<Login> findAll() {
        return entityManager.createQuery("SELECT l FROM Login l", Login.class).getResultList();
    }

    public List<Login> findAllUsingCriteria() {
        CriteriaQuery<Login> cq = entityManager.getCriteriaBuilder().createQuery(Login.class);
        cq.select(cq.from(Login.class));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Login updateLogin(Login login) {
        return entityManager.merge(login);
    }

    @Override
    public void deleteLogin(Login login) {
        entityManager.remove(login);
    }

}
