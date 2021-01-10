/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.ApplicationConfig;
import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@ValidatedInput
@Provider
@Priority(Priorities.USER)
public class VaultValidationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {

        try {
            byte[] requestEntity = requestContext.getEntityStream().readAllBytes();
            VaultEntry vaultEntry = new VaultEntry();
            vaultEntry.setEncData(requestEntity);
            VaultValidation validator = new VaultValidation();
            Optional<ProblemDto> maybeProblem = validator.validate(vaultEntry);
            if (maybeProblem.isPresent()) {
                abort(requestContext, maybeProblem.get());
            }
            else {  // no problems
                // reading stream from requestContext closes it, need to create new stream
                requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void abort(ContainerRequestContext requestContext, ProblemDto problem) {
        requestContext.abortWith(
            Response.
                status(Response.Status.BAD_REQUEST).
                type(ProblemDto.MEDIA_TYPE_PROBLEM_JSON + ApplicationConfig.UTF8_SUFFIX).
                entity(problem).build()
        );
    }

}
