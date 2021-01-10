/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.ApplicationConfig;
import com.github.miltenplescott.pswdmngrserver.CryptoUtils;
import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static java.util.Objects.*;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {

    public static final String AUTH_SCHEME = "Bearer";
    public static final String PROPERTY_USERNAME = AuthenticationRequestFilter.class.getPackageName() + ".username";

    @Inject
    private AuthTokenManager tokenManager;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (isAuthorizationHeaderPresent(authorizationHeader) && isAuthorizationHeaderCorrect(authorizationHeader)) {
            String token = authorizationHeader.substring(AUTH_SCHEME.length()).trim();
            Optional<byte[]> maybeDecodedToken = decodeToken(token);
            if (maybeDecodedToken.isPresent()) {
                if (hasTokenCorrectLength(maybeDecodedToken.get())) {
                    if (tokenManager.isTokenValid(token)) {
                        requestContext.setProperty(PROPERTY_USERNAME, tokenManager.getUsername(token));
                    }
                    else {
                        abortTokenExpiredProblem(requestContext);
                        return;
                    }
                }
                else {
                    abortTokenLengthProblem(requestContext);
                    return;
                }
            }
            else {
                abortTokenFormatProblem(requestContext);
                return;
            }
        }
        else {
            abortAuthorizationHeaderProblem(requestContext);
            return;
        }
    }

    private boolean isAuthorizationHeaderPresent(String header) {
        if (header == null) {
            return false;
        }
        // if request has multiple authorization headers, they are concatenated and separated by comma ','
        if (header.isEmpty() || header.contains(",")) {
            return false;
        }
        return true;
    }

    private boolean isAuthorizationHeaderCorrect(String header) {
        return header.toLowerCase().startsWith(AUTH_SCHEME.toLowerCase() + " ");
    }

    private Optional<byte[]> decodeToken(String token) {
        try {
            byte[] decodedToken = requireNonNull(CryptoUtils.decodeToken(requireNonNull(token)));
            return Optional.of(decodedToken);
        }
        catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private boolean hasTokenCorrectLength(byte[] token) {
        return token.length == AuthTokenManager.TOKEN_LENGTH_BYTES;
    }

    private void abortAuthorizationHeaderProblem(ContainerRequestContext requestContext) {
        ProblemDto problem = UserProblems.createDefaultAuthorizationHeaderProblem();
        UserProblems.authorizationHeaderProblem(problem);
        abort(requestContext, problem);
    }

    private void abortTokenLengthProblem(ContainerRequestContext requestContext) {
        ProblemDto problem = UserProblems.createDefaultTokenProblem();
        UserProblems.tokenLengthProblem(problem);
        abort(requestContext, problem);
    }

    private void abortTokenFormatProblem(ContainerRequestContext requestContext) {
        ProblemDto problem = UserProblems.createDefaultTokenProblem();
        UserProblems.tokenFormatProblem(problem);
        abort(requestContext, problem);
    }

    private void abortTokenExpiredProblem(ContainerRequestContext requestContext) {
        ProblemDto problem = UserProblems.createDefaultTokenProblem();
        UserProblems.tokenExpiredProblem(problem);
        abort(requestContext, problem);
    }

    private void abort(ContainerRequestContext requestContext, ProblemDto problem) {
        requestContext.abortWith(
            Response.
                status(Response.Status.UNAUTHORIZED).
                type(ProblemDto.MEDIA_TYPE_PROBLEM_JSON + ApplicationConfig.UTF8_SUFFIX).
                header(HttpHeaders.WWW_AUTHENTICATE, AUTH_SCHEME).
                entity(problem).build()
        );
    }

}
