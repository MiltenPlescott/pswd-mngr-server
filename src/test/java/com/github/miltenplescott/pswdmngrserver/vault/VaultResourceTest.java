/*
 * pswd-mngr-server
 *
 * Copyright (c) 2021, Milten Plescott. All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.miltenplescott.pswdmngrserver.vault;

import com.github.miltenplescott.pswdmngrserver.ProblemDto;
import com.github.miltenplescott.pswdmngrserver.user.AuthTokenResponseDto;
import com.github.miltenplescott.pswdmngrserver.user.AuthenticationDto;
import com.github.miltenplescott.pswdmngrserver.user.AuthenticationRequestFilter;
import com.github.miltenplescott.pswdmngrserver.user.User;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class VaultResourceTest {

    private static final byte[] EXAMPLE_ENC_DATA = "foobarf".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXAMPLE_ENC_DATA_2 = "foobar_2f".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXAMPLE_ENC_DATA_3 = "foobar_3f".getBytes(StandardCharsets.UTF_8);
    private static final int OK = javax.ws.rs.core.Response.Status.OK.getStatusCode();
    private static final int NO_CONTENT = javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode();
    private static final int BAD_REQUEST = javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode();
    private static final int NOT_FOUND = javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
    private static final int METHOD_NOT_ALLOWED = javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED.getStatusCode();

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private Jsonb jsonb;

    private static final ProblemDto vaultEntryIdProblem;
    private static final ProblemDto emptyVaultProblem;

    static {
        vaultEntryIdProblem = VaultProblems.createDefaultVaultEntryIdProblem();
        emptyVaultProblem = VaultProblems.createDefaultEmptyVaultProblem();
        VaultProblems.vaultEntryIdProblem(vaultEntryIdProblem);
        VaultProblems.emptyVaultProblem(emptyVaultProblem);
    }

    @BeforeAll
    public static void initAll() {
        emf = Persistence.createEntityManagerFactory("test-tcp");
        em = emf.createEntityManager();
    }

    @BeforeEach
    public void initEach() {
        jsonb = JsonbBuilder.create();
    }

    @AfterEach
    public void tearDownEach() {
        try {
            em.getTransaction().begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<VaultEntry> cd = cb.createCriteriaDelete(VaultEntry.class);
            Root<VaultEntry> root = cd.from(VaultEntry.class);
            em.createQuery(cd).executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception e) {
            em.getTransaction().rollback();
        }
        try {
            em.getTransaction().begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<User> cd = cb.createCriteriaDelete(User.class);
            Root<User> root = cd.from(User.class);
            em.createQuery(cd).executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    @AfterAll
    public static void tearDownAll() {
        em.close();
        emf.close();
    }

    @Test
    public void createVaultEntry_returnsNoContent() {
        createAccount();
        String token = login();

        given().
            header(tokenToAuthHeader(token)).
            contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA).
        when().
            post("pswd-mngr/vault").
        then().assertThat().
            statusCode(NO_CONTENT);

        logout(token);
    }

    @Test
    public void getAllVaultEntriesSingle_returnsOk() {
        createAccount();
        String token = login();

        given().
            header(tokenToAuthHeader(token)).
            contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA).
        when().
            post("pswd-mngr/vault").
        then().assertThat().
            statusCode(NO_CONTENT);

        Response response =
            given().
                header(tokenToAuthHeader(token)).
            when().
                get("pswd-mngr/vault");

        // https://javaee.github.io/jsonb-spec/docs/user-guide.html#mapping-a-generic-collection
        List<VaultEntryDto> list = jsonb.fromJson(response.getBody().asString(), new ArrayList<VaultEntryDto>(){}.getClass().getGenericSuperclass());

        response.then().assertThat().
            statusCode(OK).
                and().
            contentType(allOf(containsString(MediaType.APPLICATION_JSON), containsString(StandardCharsets.UTF_8.name())));
             // and()
            assertEquals(list.size(), 1);
             // and()
            assertArrayEquals(list.get(0).getEncData(), EXAMPLE_ENC_DATA);

        logout(token);
    }

    @Test
    public void getAllVaultEntriesMulti_returnsOk() {
        createAccount();
        String token = login();

        Set<byte[]> payload = new HashSet<>();
        payload.add(EXAMPLE_ENC_DATA);
        payload.add(EXAMPLE_ENC_DATA_2);
        payload.add(EXAMPLE_ENC_DATA_3);

        for (byte[] arr : payload) {
            given().
                header(tokenToAuthHeader(token)).
                contentType(MediaType.APPLICATION_OCTET_STREAM).body(arr).
            when().
                post("pswd-mngr/vault").
            then().assertThat().
                statusCode(NO_CONTENT);
        }

        Response response =
            given().
                header(tokenToAuthHeader(token)).
            when().
                get("pswd-mngr/vault");

        Set<String> expected = new HashSet<>();
        expected.add(Arrays.toString(EXAMPLE_ENC_DATA));
        expected.add(Arrays.toString(EXAMPLE_ENC_DATA_2));
        expected.add(Arrays.toString(EXAMPLE_ENC_DATA_3));

        // https://javaee.github.io/jsonb-spec/docs/user-guide.html#mapping-a-generic-collection
        Set<VaultEntryDto> output = jsonb.fromJson(response.getBody().asString(), new HashSet<VaultEntryDto>(){}.getClass().getGenericSuperclass());
        Set<String> actual = output.stream()
            .map(x -> Arrays.toString(x.getEncData()))
            .collect(Collectors.toCollection(HashSet::new));

        response.then().assertThat().
            statusCode(OK).
                and().
            contentType(allOf(containsString(MediaType.APPLICATION_JSON), containsString(StandardCharsets.UTF_8.name())));
             // and()
            assertEquals(expected.size(), actual.size());
             // and()
            assertEquals(expected, actual);

        logout(token);
    }

    private void createAccount() {
        given().
            contentType(MediaType.APPLICATION_JSON).body(getPayload()).
        when().
            post("pswd-mngr/account").
        then().assertThat().
            statusCode(OK);
    }

    private String login() {
        Response loginResponse =
            given().
                contentType(MediaType.APPLICATION_JSON).body(getPayload()).
            when().
                post("pswd-mngr/account/login");
        loginResponse.
            then().assertThat().
                statusCode(OK);

        AuthTokenResponseDto loginResponseDto = jsonb.fromJson(loginResponse.getBody().asString(), AuthTokenResponseDto.class);
        return loginResponseDto.getToken();
    }

    private void logout(String token) {
        given().
            header(tokenToAuthHeader(token)).
        when().
            post("pswd-mngr/account/logout").
        then().assertThat().
            statusCode(OK);
    }

    private String getPayload() {
        AuthenticationDto dto = new AuthenticationDto();
        dto.setUsername("JohnDoe");
        dto.setMasterPswd("FY7iq0Y1ja2loHmMurgM78VW8Kt3PUpK2oKNjSd0Tt8=");
        return jsonb.toJson(dto);
    }

    private Header tokenToAuthHeader(String token) {
        return new Header(HttpHeaders.AUTHORIZATION, AuthenticationRequestFilter.AUTH_SCHEME + " " + token);
    }

}
