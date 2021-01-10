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
import javax.persistence.EntityManager;

public interface GenericDao<T extends BaseEntity> {

    public EntityManager getEntityManager();

    void create(T t);

    Optional<T> findOne(Object id);

    List<T> findAll();

    T update(T t);

    void delete(T t);

}
