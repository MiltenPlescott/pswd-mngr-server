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
import com.github.miltenplescott.pswdmngrserver.user.Secured;
import com.github.miltenplescott.pswdmngrserver.user.UserResource;
import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/vault")
@Stateless
@Secured
public class VaultResource {

    @Inject
    private VaultService vaultService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVault(@Context HttpServletRequest request) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            List<VaultEntryDto> vaultEntries = vaultService.getAllVaultEntries(maybeUsername.get());
            return Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON + ApplicationConfig.UTF8_SUFFIX)
                .entity(vaultEntries).build();
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @ValidatedInput
    public Response createVaultEntry(@Context HttpServletRequest request, @Context UriInfo uriInfo, byte[] encData) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            long createdId = vaultService.createVaultEntry(maybeUsername.get(), encData);
            UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(createdId));
            return Response
                .status(Response.Status.CREATED)
                .header(HttpHeaders.LOCATION, builder.build()).build();
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    public Response putNotSupported() {
        return Response
            .status(Response.Status.METHOD_NOT_ALLOWED)
            .header(HttpHeaders.ALLOW, HttpMethod.GET + ", " + HttpMethod.POST + ", " + HttpMethod.DELETE).build();
    }

    @DELETE
    @Produces(ProblemDto.MEDIA_TYPE_PROBLEM_JSON)
    public Response deleteVault(@Context HttpServletRequest request) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            Optional<ProblemDto> maybeProblem = vaultService.deleteAllVaultEntries(maybeUsername.get());
            if (maybeProblem.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            else {
                return buildProblemResponse(maybeProblem.get(), Response.Status.NOT_FOUND);
            }
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{id:}")
    @Produces({MediaType.APPLICATION_JSON, ProblemDto.MEDIA_TYPE_PROBLEM_JSON})
    public Response getVaultEntryById(@Context HttpServletRequest request, @PathParam("id") long id) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            VaultEntryDto vaultEntryDto = new VaultEntryDto();
            Optional<ProblemDto> maybeProblem = vaultService.getVaultEntry(maybeUsername.get(), id, vaultEntryDto);
            if (maybeProblem.isEmpty()) {
                return Response
                    .status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON + ApplicationConfig.UTF8_SUFFIX)
                    .entity(vaultEntryDto).build();
            }
            else {
                return buildProblemResponse(maybeProblem.get(), Response.Status.NOT_FOUND);
            }
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{id}")
    public Response postIdNotSupported() {
        return Response
            .status(Response.Status.METHOD_NOT_ALLOWED)
            .header(HttpHeaders.ALLOW, HttpMethod.GET + ", " + HttpMethod.PUT + ", " + HttpMethod.DELETE).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(ProblemDto.MEDIA_TYPE_PROBLEM_JSON)
    @ValidatedInput
    public Response updateVaultEntryById(@Context HttpServletRequest request, @PathParam("id") long id, byte[] encData) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            Optional<ProblemDto> maybeProblem = vaultService.updateVaultEntry(maybeUsername.get(), id, encData);
            if (maybeProblem.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            else {
                return buildProblemResponse(maybeProblem.get(), Response.Status.NOT_FOUND);
            }
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(ProblemDto.MEDIA_TYPE_PROBLEM_JSON)
    public Response deleteVaultEntryById(@Context HttpServletRequest request, @PathParam("id") long id) {
        Optional<String> maybeUsername = UserResource.getUsername(request);
        if (maybeUsername.isPresent()) {
            Optional<ProblemDto> maybeProblem = vaultService.deleteVaultEntry(maybeUsername.get(), id);
            if (maybeProblem.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            else {
                return buildProblemResponse(maybeProblem.get(), Response.Status.NOT_FOUND);
            }
        }
        else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Response buildProblemResponse(ProblemDto problem, Response.Status statusCode) {
        problem.setStatus(statusCode.getStatusCode());
        return Response
            .status(statusCode)
            .type(ProblemDto.MEDIA_TYPE_PROBLEM_JSON + ApplicationConfig.UTF8_SUFFIX)
            .entity(problem).build();
    }

}
