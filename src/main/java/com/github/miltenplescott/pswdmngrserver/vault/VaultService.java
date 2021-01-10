/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import com.github.miltenplescott.pswdmngrserver.user.User;
import com.github.miltenplescott.pswdmngrserver.user.UserDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class VaultService {

    @Inject
    private VaultDao vaultDao;

    @Inject
    private UserDao userDao;

    public VaultService() {
    }

    public void createVaultEntry(String username, byte[] encData) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            VaultEntry vaultEntry = new VaultEntry(encData, user);
            vaultDao.create(vaultEntry);
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    public Optional<ProblemDto> updateVaultEntry(String username, long vaultEntryId, byte[] encData) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            Optional<VaultEntry> maybeVaultEntry = vaultDao.findOne(new VaultEntryPk(vaultEntryId, user.getId()));
            if (maybeVaultEntry.isPresent()) {
                VaultEntry vaultEntry = maybeVaultEntry.get();
                vaultEntry.setEncData(encData);
                vaultDao.update(vaultEntry);
                return Optional.empty();
            }
            else {  // vault entry not found
                return Optional.of(getVaultEntryIdProblemDto());
            }
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    public Optional<ProblemDto> deleteVaultEntry(String username, long vaultEntryId) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            Optional<VaultEntry> maybeVaultEntry = vaultDao.findOne(new VaultEntryPk(vaultEntryId, user.getId()));
            if (maybeVaultEntry.isPresent()) {  // vault entry found
                vaultDao.delete(maybeVaultEntry.get());  // delete it
                return Optional.empty();
            }
            else {  // vault entry not found
                return Optional.of(getVaultEntryIdProblemDto());
            }
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    public Optional<ProblemDto> deleteAllVaultEntries(String username) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            int deleted = vaultDao.deleteAll(user.getId());
            if (deleted > 0) {
                return Optional.empty();
            }
            else {  // nothing to delete
                return Optional.of(getEmptyVaultProblemDto());
            }
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    public List<VaultEntryDto> getAllVaultEntries(String username) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            List<VaultEntry> vaultEntries = vaultDao.findAll(user.getId());  // throwing exception
//            List<VaultEntry> vaultEntries = vaultDao.findAll(user);
            return vaultEntryListToDto(vaultEntries);
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    public Optional<ProblemDto> getVaultEntry(String username, long vaultEntryId, final VaultEntryDto vaultEntryDto) {
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            Optional<VaultEntry> maybeVaultEntry = vaultDao.findOne(new VaultEntryPk(vaultEntryId, user.getId()));
            if (maybeVaultEntry.isPresent()) {  // no problem
                VaultEntry vaultEntry = maybeVaultEntry.get();
                vaultEntryDto.setId(vaultEntry.getId());
                vaultEntryDto.setEncData(vaultEntry.getEncData());
                return Optional.empty();
            }
            else {  // vault entry with id-username combination not found problem
                return Optional.of(getVaultEntryIdProblemDto());
            }
        }
        else {
            throw new AssertionError("Username not found.");
        }
    }

    private ProblemDto getVaultEntryIdProblemDto() {
        ProblemDto problemDto = VaultProblems.createDefaultVaultEntryIdProblem();
        VaultProblems.vaultEntryIdProblem(problemDto);
        return problemDto;
    }

    private ProblemDto getEmptyVaultProblemDto() {
        ProblemDto problemDto = VaultProblems.createDefaultEmptyVaultProblem();
        VaultProblems.emptyVaultProblem(problemDto);
        return problemDto;
    }

    private VaultEntryDto vaultEntryToDto(VaultEntry vaultEntry) {
        return new VaultEntryDto(vaultEntry.getId(), vaultEntry.getEncData());

    }

    private List<VaultEntryDto> vaultEntryListToDto(List<VaultEntry> vaultEntries) {
        List<VaultEntryDto> dtoList = new ArrayList<>();
        for (VaultEntry ve : vaultEntries) {
            dtoList.add(vaultEntryToDto(ve));
        }
        return dtoList;
    }

}
