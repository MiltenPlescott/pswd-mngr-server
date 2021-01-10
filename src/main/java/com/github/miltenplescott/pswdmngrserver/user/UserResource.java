/*
 * pswd-mngr-server
 *
 * Copyright (c) 2020, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.user;

import com.github.miltenplescott.pswdmngrserver.ApplicationConfig;
import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/account")
@Stateless
public class UserResource {

    @Inject
    private UserService userService;

    @POST  // create account
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(ProblemDto.MEDIA_TYPE_PROBLEM_JSON)
    public Response createAccount(AuthenticationDto dto) {
        Optional<ProblemDto> maybeProblem = userService.createUser(dto.getUsername(), dto.getMasterPswd());
        if (maybeProblem.isEmpty()) {
            return Response.status(Response.Status.OK).build();
        }
        else {
            maybeProblem.get().setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return Response.
                status(maybeProblem.get().getStatus()).
                type(ProblemDto.MEDIA_TYPE_PROBLEM_JSON + ApplicationConfig.UTF8_SUFFIX).
                entity(maybeProblem.get()).build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, ProblemDto.MEDIA_TYPE_PROBLEM_JSON})
    public Response login(AuthenticationDto dto) {
        AuthTokenResponseDto token = new AuthTokenResponseDto();
        Optional<ProblemDto> maybeProblem = userService.login(dto.getUsername(), dto.getMasterPswd(), token);

        if (maybeProblem.isEmpty()) {
            return Response.
                status(Response.Status.OK).
                type(MediaType.APPLICATION_JSON + ApplicationConfig.UTF8_SUFFIX).
                entity(token).build();
        }
        else {
            maybeProblem.get().setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return Response.
                status(maybeProblem.get().getStatus()).
                type(ProblemDto.MEDIA_TYPE_PROBLEM_JSON + ApplicationConfig.UTF8_SUFFIX).
                entity(maybeProblem.get()).build();
        }
    }

    @POST
    @Path("/logout")
    @Produces(ProblemDto.MEDIA_TYPE_PROBLEM_JSON)
    @Secured
    public Response logout(@Context HttpServletRequest request) {
        Optional<String> maybeUsername = getUsername(request);
        if (maybeUsername.isPresent()) {
            userService.logout(maybeUsername.get());
            return Response.status(Response.Status.OK).build();
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static Optional<String> getUsername(HttpServletRequest request) {
        return Optional.ofNullable((String) request.getAttribute(AuthenticationRequestFilter.PROPERTY_USERNAME));
    }

}
