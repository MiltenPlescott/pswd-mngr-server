/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.util.Optional;
import java.util.Set;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

@Stateless
public class UserValidation {

    private final Validator validator;
    private ProblemDto dto;

    public UserValidation() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        dto = UserProblems.createDefaultUsernameProblem();
    }

    public Optional<ProblemDto> validate(User user) {
        if (validator.validate(user).isEmpty()) {  // no problems
            return Optional.empty();
        }

        dto = UserProblems.createDefaultUsernameProblem();

        Set<ConstraintViolation<User>> usernameViolations = validator.validateProperty(user, "username");
        for (ConstraintViolation<User> cv : usernameViolations) {
            UserProblems.usernameViolationToProblemDto(dto, cv);
        }

        return Optional.of(dto);
    }

}
