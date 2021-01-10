/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import javax.validation.ConstraintViolation;

public class VaultProblems {

    public static final String PROBLEM_TITLE_ENC_DATA = "Invalid encrypted data.";
    public static final String PROBLEM_TITLE_VAULT_ENTRY_ID = "Invalid vault entry ID.";
    public static final String PROBLEM_TITLE_EMPTY_VAULT = "Vault is empty.";

    public static final String MSG_VAULT_ENTRY_ID = "Vault entry with the specified ID not found.";
    public static final String MSG_EMPTY_VAULT = "This user's vault contains no vault entries.";

    private VaultProblems() {
        throw new AssertionError("Suppress default constructor for noninstantiability.");
    }

    public static ProblemDto createDefaultEncDataProblem() {
        return new ProblemDto(PROBLEM_TITLE_ENC_DATA);
    }

    public static ProblemDto createDefaultVaultEntryIdProblem() {
        return new ProblemDto(PROBLEM_TITLE_VAULT_ENTRY_ID);
    }

    public static ProblemDto createDefaultEmptyVaultProblem() {
        return new ProblemDto(PROBLEM_TITLE_EMPTY_VAULT);
    }

    public static void encDataViolationToProblemDto(ProblemDto dto, ConstraintViolation<VaultEntry> cv) {
        dto.getInvalidParams().add(new ProblemDto.Extension("encData", cv.getMessage()));
    }

    public static ProblemDto vaultEntryIdProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("id", MSG_VAULT_ENTRY_ID));
        return dto;
    }

    public static ProblemDto emptyVaultProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("id", MSG_EMPTY_VAULT));
        return dto;
    }

}
