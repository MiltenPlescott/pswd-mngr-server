/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.util.Arrays;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;

import static com.github.miltenplescott.pswdmngrserver.CryptoUtils.*;
import static java.util.Objects.requireNonNull;

@Stateless
public class UserService {

    @Inject
    private UserDao userDao;

    @Inject
    private UserValidation validator;

    @Inject
    private AuthTokenManager tokenManager;

    public UserService() {
    }

    public Optional<ProblemDto> createUser(String username, String masterPswd) {
        User user = new User();
        user.setUsername(username);

        Optional<ProblemDto> maybeProblemDto = validator.validate(user);
        if (maybeProblemDto.isEmpty()) {  // user validation OK
            try {
                ProblemDto problemDto;
                if (userDao.userWithNameExists(username)) {  // username not unique
                    problemDto = UserProblems.createDefaultUsernameProblem();
                    UserProblems.usernameNotUniqueProblem(problemDto);
                    return Optional.of(problemDto);  // return username not unique problem
                }
                else {  // proceed with masterPswd checks
                    byte[] decodedPswd = null;
                    problemDto = UserProblems.createDefaultPasswordProblem();

                    try {
                        decodedPswd = requireNonNull(decodePswd(requireNonNull(masterPswd)));
                        if (decodedPswd.length != KDF_INPUT_LENGTH_BYTES) {
                            clearArray(decodedPswd);
                            UserProblems.masterPswdLengthProblem(problemDto);
                            return Optional.of(problemDto);  // return length problem
                        }
                    }
                    catch (IllegalArgumentException | NullPointerException e) {
                        clearArray(decodedPswd);
                        UserProblems.masterPswdFormatProblem(problemDto);
                        return Optional.of(problemDto);  // return base64 format problem
                    }

                    // KDF
                    byte[] salt = genSalt();
                    byte[] kdfOutput = kdfAndSalt(decodedPswd, salt);
                    user.setSalt(salt);
                    user.setMasterPswd(kdfOutput);

                    // clean up
                    clearArray(decodedPswd);
                    clearArray(salt);
                    clearArray(kdfOutput);

                    userDao.create(user);  // persist user in DB
                    return Optional.empty();  // return OK
                }
            }
            catch (EntityExistsException e) {
                throw new AssertionError("EntityExistsException: userDao.userWithNameExists(username) check failed");
            }
        }
        else {  // user validation error
            return maybeProblemDto;
        }
    }

    public Optional<ProblemDto> login(String username, String masterPswd, final AuthTokenResponseDto tokenDto) {
        ProblemDto problemDto;
        Optional<User> maybeUser = userDao.findByName(username);
        if (maybeUser.isPresent()) {  // username found in DB
            User user = maybeUser.get();

            byte[] decodedPswd = null;
            try {
                decodedPswd = requireNonNull(decodePswd(requireNonNull(masterPswd)));
            }
            catch (IllegalArgumentException | NullPointerException e) {
                clearArray(decodedPswd);
                problemDto = UserProblems.createDefaultAuthProblem();
                UserProblems.authProblem(problemDto);
                return Optional.of(problemDto);
            }

            // KDF
            byte[] kdfOutput = kdfAndSalt(decodedPswd, user.getSalt());
            if (Arrays.equals(kdfOutput, user.getMasterPswd())) {  // correct password
                tokenDto.setToken(tokenManager.generateToken(username));
                tokenDto.setTokenType(AuthTokenResponseDto.BEARER);
                tokenDto.setExpiration(tokenManager.getTokenExpirationMs(tokenDto.getToken()));
            }
            else {
                problemDto = UserProblems.createDefaultAuthProblem();
                UserProblems.authProblem(problemDto);
                return Optional.of(problemDto);
            }

            // clean up
            clearArray(decodedPswd);
            clearArray(kdfOutput);

            return Optional.empty();
        }
        else {  // username not in DB
            tokenManager.deleteTokenForUsername(username);
            problemDto = UserProblems.createDefaultAuthProblem();
            UserProblems.authProblem(problemDto);
            return Optional.of(problemDto);
        }
    }

    public void logout(String username) {
        tokenManager.deleteTokenForUsername(username);
    }

}
