/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.CryptoUtils;
import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import javax.validation.ConstraintViolation;

public final class UserProblems {

    public static final String PROBLEM_TITLE_USERNAME = "Invalid username.";
    public static final String PROBLEM_TITLE_PSWD = "Invalid master password.";
    public static final String PROBLEM_TITLE_AUTH = "Authentication failed.";
    public static final String PROBLEM_TITLE_AUTHORIZATION_HEADER = "Invalid authorization header.";
    public static final String PROBLEM_TITLE_TOKEN = "Invalid authentication token.";

    public static final String MSG_USERNAME_NOT_UNIQUE = "Username already exists.";
    public static final String MSG_PSWD_LENGTH = "Master password is required to be " + (8 * CryptoUtils.KDF_INPUT_LENGTH_BYTES) + "-bit long.";
    public static final String MSG_PSWD_FORMAT = "Master password is not a valid Base64 format.";
    public static final String MSG_AUTH = "Invalid username or master password.";
    public static final String MSG_AUTHORIZATION_HEADER = "Authorization header could not be parsed (possible causes: missing header, wrong syntax, unrecognized authentication scheme, etc.).";
    public static final String MSG_TOKEN_LENGTH = "Authentication token is required to be " + (8 * AuthTokenManager.TOKEN_LENGTH_BYTES) + "-bit long.";
    public static final String MSG_TOKEN_FORMAT = "Authentication token is not a valid Base64 format.";
    public static final String MSG_TOKEN_EXPIRED = "Authentication token has expired.";

    private UserProblems() {
        throw new AssertionError("Suppress default constructor for noninstantiability.");
    }

    public static ProblemDto createDefaultUsernameProblem() {
        return new ProblemDto(PROBLEM_TITLE_USERNAME);
    }

    public static ProblemDto usernameNotUniqueProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("username", MSG_USERNAME_NOT_UNIQUE));
        return dto;
    }

    public static void usernameViolationToProblemDto(ProblemDto dto, ConstraintViolation<User> cv) {
        dto.getInvalidParams().add(new ProblemDto.Extension("username", cv.getMessage()));
    }

    public static ProblemDto createDefaultPasswordProblem() {
        return new ProblemDto(PROBLEM_TITLE_PSWD);
    }

    public static ProblemDto masterPswdLengthProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("masterPswd", MSG_PSWD_LENGTH));
        return dto;
    }

    public static ProblemDto masterPswdFormatProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("masterPswd", MSG_PSWD_FORMAT));
        return dto;
    }

    public static ProblemDto createDefaultAuthProblem() {
        return new ProblemDto(PROBLEM_TITLE_AUTH);
    }

    public static ProblemDto authProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("username", MSG_AUTH));
        dto.getInvalidParams().add(new ProblemDto.Extension("masterPswd", MSG_AUTH));
        return dto;
    }

    public static ProblemDto createDefaultAuthorizationHeaderProblem() {
        return new ProblemDto(PROBLEM_TITLE_AUTHORIZATION_HEADER);
    }

    public static ProblemDto authorizationHeaderProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("Authorization", MSG_AUTHORIZATION_HEADER));
        return dto;
    }

    public static ProblemDto createDefaultTokenProblem() {
        return new ProblemDto(PROBLEM_TITLE_TOKEN);
    }

    public static ProblemDto tokenLengthProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("token", MSG_TOKEN_LENGTH));
        return dto;
    }

    public static ProblemDto tokenFormatProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("token", MSG_TOKEN_FORMAT));
        return dto;
    }

    public static ProblemDto tokenExpiredProblem(ProblemDto dto) {
        dto.getInvalidParams().add(new ProblemDto.Extension("token", MSG_TOKEN_EXPIRED));
        return dto;
    }

}
