/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

public class VaultValidation {

    private final Validator validator;
    private ProblemDto dto;

    public VaultValidation() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Optional<ProblemDto> validate(VaultEntry vaultEntry) {
        if (validator.validate(vaultEntry).isEmpty()) {  // no problems
            return Optional.empty();
        }

        dto = VaultProblems.createDefaultEncDataProblem();

        Set<ConstraintViolation<VaultEntry>> encDataViolations = validator.validateProperty(vaultEntry, VaultEntry_.encData.getName());
        for (ConstraintViolation<VaultEntry> cv : encDataViolations) {
            VaultProblems.encDataViolationToProblemDto(dto, cv);
        }

        return Optional.of(dto);
    }

}
