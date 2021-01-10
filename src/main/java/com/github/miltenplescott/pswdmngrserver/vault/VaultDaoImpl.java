/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.GenericDaoImpl;
import com.github.miltenplescott.pswdmngrserver.user.User_;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
public class VaultDaoImpl extends GenericDaoImpl<VaultEntry> implements VaultDao {

    public VaultDaoImpl() {
        super(VaultEntry.class);
    }

    @Override
    public List<VaultEntry> findAll(long userId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<VaultEntry> cq = cb.createQuery(VaultEntry.class);
        Root<VaultEntry> vaultEntry = cq.from(VaultEntry.class);
        cq.select(vaultEntry).where(cb.equal(vaultEntry.get(VaultEntry_.user).get(User_.id), userId));

        return getEntityManager().createQuery(cq).getResultList();
    }

    @Override
    public int deleteAll(long userId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<VaultEntry> cd = cb.createCriteriaDelete(VaultEntry.class);
        Root<VaultEntry> vaultEntry = cd.from(VaultEntry.class);
        cd.where(cb.equal(vaultEntry.get(VaultEntry_.user).get(User_.id), userId));
        return getEntityManager().createQuery(cd).executeUpdate();
    }

}
