/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver;

import java.util.List;
import java.util.Optional;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.criteria.CriteriaQuery;

@TransactionManagement(TransactionManagementType.CONTAINER)
public class GenericDaoImpl<T extends BaseEntity> implements GenericDao<T> {

    @PersistenceContext(unitName = "pswd-mngr-persistance-unit", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

    private final Class<T> type;

    public GenericDaoImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void create(T t) {
        entityManager.persist(t);
    }

    @Override
    public Optional<T> findOne(Long id) {
        return Optional.ofNullable(entityManager.find(type, id));
    }

    @Override
    public List<T> findAll() {
        CriteriaQuery<T> cq = entityManager.getCriteriaBuilder().createQuery(type);
        cq.select(cq.from(type));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public T update(T t) {
        return entityManager.merge(t);
    }

    @Override
    public void delete(T t) {
        entityManager.remove(t);
    }

}
