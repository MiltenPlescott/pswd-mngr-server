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

/**
 *
 * @author Milten Plescott
 */
public interface GenericDao<T extends BaseEntity> {

    void create(T t);

    Optional<T> find(Long id);

    List<T> findAll();

    T update(T t);

    void delete(T t);

}
