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
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class VaultResourceTest {

    private static final byte[] EXAMPLE_ENC_DATA = "foobar".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXAMPLE_ENC_DATA_2 = "foobar_2".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXAMPLE_ENC_DATA_3 = "foobar_3".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXAMPLE_ENC_DATA_AT_LIMIT = new byte[VaultEntry.ENC_DATA_MAX_LENGTH];
    private static final byte[] EXAMPLE_ENC_DATA_OVER_LIMIT = new byte[VaultEntry.ENC_DATA_MAX_LENGTH + 1];
    private static final int OK = javax.ws.rs.core.Response.Status.OK.getStatusCode();
    private static final int CREATED = javax.ws.rs.core.Response.Status.CREATED.getStatusCode();
    private static final int NO_CONTENT = javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode();
    private static final int BAD_REQUEST = javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode();
    private static final int NOT_FOUND = javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode();
    private static final int METHOD_NOT_ALLOWED = javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final String EXPECTED_LOCATION_SUBSTRING = "/pswd-mngr/vault/";

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private Jsonb jsonb;

    private static final Matcher<String> mimeJsonMatcher = allOf(containsString(MediaType.APPLICATION_JSON), containsString(StandardCharsets.UTF_8.name()));
    private static final Matcher<String> mimeProblemMatcher = allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name()));

    private static final ProblemDto vaultEntryIdProblem;
    private static final ProblemDto emptyVaultProblem;
    private static final ProblemDto nullViolationProblem;
    private static final ProblemDto sizeViolationProblem;

    static {
        vaultEntryIdProblem = VaultProblems.createDefaultVaultEntryIdProblem();
        emptyVaultProblem = VaultProblems.createDefaultEmptyVaultProblem();
        VaultProblems.vaultEntryIdProblem(vaultEntryIdProblem);
        VaultProblems.emptyVaultProblem(emptyVaultProblem);
        nullViolationProblem = VaultProblems.createDefaultEncDataProblem();
        sizeViolationProblem = VaultProblems.createDefaultEncDataProblem();
    }

    private static void createValidationProblem(ProblemDto dto, VaultEntry vaultEntry) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<VaultEntry>> encDataViolations = validator.validateProperty(vaultEntry, VaultEntry_.encData.getName());
        for (ConstraintViolation<VaultEntry> cv : encDataViolations) {
            VaultProblems.encDataViolationToProblemDto(dto, cv);
        }
    }

    @BeforeAll
    public static void initAll() {
        emf = Persistence.createEntityManagerFactory("test-tcp");
        em = emf.createEntityManager();
        createValidationProblem(nullViolationProblem, new VaultEntry(null, null));
        createValidationProblem(sizeViolationProblem, new VaultEntry(EXAMPLE_ENC_DATA_OVER_LIMIT, null));
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
            cd.from(VaultEntry.class);
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
            cd.from(User.class);
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

    private Response post(String token, byte[] encData) {
        return
            given().
                header(tokenToAuthHeader(token)).
                contentType(MediaType.APPLICATION_OCTET_STREAM).body(encData).
            when().
                post("pswd-mngr/vault").
            then().assertThat().
                statusCode(CREATED).
                    and().
                extract().response();
    }

    private Response get(String token, long id) {
        return
            given().
                header(tokenToAuthHeader(token)).
            when().
                get("pswd-mngr/vault/" + id);
    }

    private Response get(String token) {
        return
            given().
                header(tokenToAuthHeader(token)).
            when().
                get("pswd-mngr/vault");
    }

    private Response delete(String token, long id) {
        return
            given().
                header(tokenToAuthHeader(token)).
            when().
                delete("pswd-mngr/vault/" + id);
    }

    private Response delete(String token) {
        return
            given().
                header(tokenToAuthHeader(token)).
            when().
                delete("pswd-mngr/vault");
    }

    @Test
    public void createVaultEntry_returnsCreated() {
        createAccount();
        String token = login();

        // POST
        post(token, EXAMPLE_ENC_DATA).
            then().assertThat().
                header(HttpHeaders.LOCATION, containsString(EXPECTED_LOCATION_SUBSTRING)).
                    and().
                body(is(emptyString()));

        logout(token);
    }

    @Test
    public void createVaultEntry_atSizeLimit_returnsCreated() {
        createAccount();
        String token = login();

        // POST
        post(token, EXAMPLE_ENC_DATA_AT_LIMIT).
            then().assertThat().
                header(HttpHeaders.LOCATION, containsString(EXPECTED_LOCATION_SUBSTRING)).
                    and().
                body(is(emptyString()));

        logout(token);
    }

    @Test
    public void createVaultEntry_overSizeLimit_returnsSizeViolationProblem() {
        createAccount();
        String token = login();

        // POST
        Response response =
            given().
                header(tokenToAuthHeader(token)).
                contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA_OVER_LIMIT).
            when().
                post("pswd-mngr/vault");

        ProblemDto problemDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        response.then().assertThat().
            statusCode(BAD_REQUEST).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(sizeViolationProblem, problemDto);

        logout(token);
    }

    @Test
    public void getVaultEntryById_returnsOk() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);

        // POST, so there's an extra data in DB that's not supposed be to returned from GET
        Response postResponseExtra = post(token, EXAMPLE_ENC_DATA_2);

        long id = getIdFromLocationHeader(postResponse);
        long idExtra = getIdFromLocationHeader(postResponseExtra);
        assertNotEquals(idExtra, id);

        // GET
        Response getResponse = get(token, id);
        VaultEntryDto responseDto = jsonb.fromJson(getResponse.getBody().asString(), VaultEntryDto.class);

        getResponse.then().assertThat().
            statusCode(OK).
                and().
            contentType(mimeJsonMatcher);
             // and()
            assertEquals(id, responseDto.getId());
             // and()
            assertArrayEquals(EXAMPLE_ENC_DATA, responseDto.getEncData());

        logout(token);
    }

    @Test
    public void getVaultEntryByWrongId_returnsVaultEntryIdProblem() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);
        long id = getIdFromLocationHeader(postResponse);
        long wrongId = id + 1;

        // GET
        Response getResponse = get(token, wrongId);
        ProblemDto problemDto = jsonb.fromJson(getResponse.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        getResponse.then().assertThat().
            statusCode(NOT_FOUND).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(vaultEntryIdProblem, problemDto);

        logout(token);
    }

    @Test
    public void FooTriesToGetBarsDataById_returnsVaultEntryIdProblem() {
        // Bar's login
        String payloadBar = getPayload("Barwise");
        createAccount(payloadBar);
        String tokenBar = login(payloadBar);

        // Bar's POST
        Response postResponseBar = post(tokenBar, EXAMPLE_ENC_DATA);
        long idBar = getIdFromLocationHeader(postResponseBar);

        // Foo's login
        String payloadFoo = getPayload("MrFoodo");
        createAccount(payloadFoo);
        String tokenFoo = login(payloadFoo);

        // Foo's GET
        Response getResponseFoo = get(tokenFoo, idBar);
        ProblemDto problemDto = jsonb.fromJson(getResponseFoo.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        getResponseFoo.then().assertThat().
            statusCode(NOT_FOUND).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(vaultEntryIdProblem, problemDto);

        logout(tokenFoo);
        logout(tokenBar);
    }

    @Test
    public void getAllVaultEntries_single_returnsOk() {
        createAccount();
        String token = login();

        // POST
        post(token, EXAMPLE_ENC_DATA);

        // GET
        Response response = get(token);
        // https://javaee.github.io/jsonb-spec/docs/user-guide.html#mapping-a-generic-collection
        List<VaultEntryDto> list = jsonb.fromJson(response.getBody().asString(), new ArrayList<VaultEntryDto>(){}.getClass().getGenericSuperclass());

        response.then().assertThat().
            statusCode(OK).
                and().
            contentType(mimeJsonMatcher);
             // and()
            assertEquals(list.size(), 1);
             // and()
            assertArrayEquals(list.get(0).getEncData(), EXAMPLE_ENC_DATA);  // enc_data after POST and GET remain unchanged

        logout(token);
    }

    @Test
    public void getAllVaultEntries_multi_returnsOk() {
        createAccount();
        String token = login();

        List<byte[]> payload = new ArrayList<>();
        payload.add(EXAMPLE_ENC_DATA);
        payload.add(EXAMPLE_ENC_DATA_2);
        payload.add(EXAMPLE_ENC_DATA_3);

        // POST
        for (byte[] p : payload) {
            post(token, p);
        }

        // GET
        Response response = get(token);

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
            contentType(mimeJsonMatcher);
             // and()
            assertEquals(expected.size(), actual.size());
             // and()
            assertEquals(expected, actual);  // enc_data after POST and GET remain unchanged

        logout(token);
    }

    @Test
    public void getAllVaultEntries_afterRelogging_returnsOk() {
        createAccount();
        String token = login();

        List<byte[]> payload = new ArrayList<>();
        payload.add(EXAMPLE_ENC_DATA);
        payload.add(EXAMPLE_ENC_DATA_2);
        payload.add(EXAMPLE_ENC_DATA_3);

        // POST
        for (byte[] p : payload) {
            post(token, p);
        }

        // logout
        logout(token);
        // log back in
        token = login();

        // GET
        Response response = get(token);

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
            contentType(mimeJsonMatcher);
             // and()
            assertEquals(expected.size(), actual.size());
             // and()
            assertEquals(expected, actual);  // enc_data after logout remain unchanged

        logout(token);
    }

    @Test
    public void deleteAllVaultEntries_returnsNoContent() {
        createAccount();
        String token = login();

        // create vault entries
        List<byte[]> payload = new ArrayList<>();
        payload.add(EXAMPLE_ENC_DATA);
        payload.add(EXAMPLE_ENC_DATA_2);
        payload.add(EXAMPLE_ENC_DATA_3);

        // POST
        for (byte[] p : payload) {
            post(token, p);
        }

        // DELETE
        delete(token).
            then().assertThat().
                statusCode(NO_CONTENT);

        // GET
        get(token).
            then().assertThat().
                statusCode(OK).
                    and().
                body(is(EMPTY_JSON_ARRAY));

        logout(token);
    }

    @Test
    public void deleteAllVaultEntries_returnsEmptyVaultProblem() {
        createAccount();
        String token = login();

        // GET
        get(token).
            then().assertThat().
                statusCode(OK).
                    and().
                body(is(EMPTY_JSON_ARRAY));

        // DELETE
        Response response = delete(token);
        ProblemDto problemDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        response.then().assertThat().
            statusCode(NOT_FOUND).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(emptyVaultProblem, problemDto);

        logout(token);
    }

    @Test
    public void postById_returnsMethodNotAllowed() {
        createAccount();
        String token = login();
        int id = 1;

        // POST
        given().
            header(tokenToAuthHeader(token)).
            contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA).
        when().
            post("pswd-mngr/vault/" + id).
        then().assertThat().
            statusCode(METHOD_NOT_ALLOWED).
                and().
            header(HttpHeaders.ALLOW, allOf(
                containsString(HttpMethod.GET),
                containsString(HttpMethod.PUT),
                containsString(HttpMethod.DELETE)
            ));
    }

    @Test
    public void putWithoutId_returnsMethodNotAllowed() {
        createAccount();
        String token = login();

        // PUT
        given().
            header(tokenToAuthHeader(token)).
            contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA).
        when().
            put("pswd-mngr/vault").
        then().assertThat().
            statusCode(METHOD_NOT_ALLOWED).
                and().
            header(HttpHeaders.ALLOW, allOf(
                containsString(HttpMethod.GET),
                containsString(HttpMethod.POST),
                containsString(HttpMethod.DELETE)
            ));
    }

    @Test
    public void putById_returnsNoContent() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);
        long id = getIdFromLocationHeader(postResponse);

        // PUT
        given().
            header(tokenToAuthHeader(token)).
            contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA_2).
        when().
            put("pswd-mngr/vault/" + id).
        then().assertThat().
            statusCode(NO_CONTENT);

        // GET
        Response getResponse = get(token, id);
        VaultEntryDto responseDto = jsonb.fromJson(getResponse.getBody().asString(), VaultEntryDto.class);

        getResponse.then().assertThat().
            statusCode(OK).
                and().
            contentType(mimeJsonMatcher);
             // and()
            assertEquals(id, responseDto.getId());
             // and()
            assertArrayEquals(EXAMPLE_ENC_DATA_2, responseDto.getEncData());

        logout(token);
    }

    @Test
    public void putByWrongId_returnsVaultEntryIdProblem() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);
        long id = getIdFromLocationHeader(postResponse);
        long wrongId = id + 1;

        // PUT
        Response putResponse =
            given().
                header(tokenToAuthHeader(token)).
                contentType(MediaType.APPLICATION_OCTET_STREAM).body(EXAMPLE_ENC_DATA_2).
            when().
                put("pswd-mngr/vault/" + wrongId);

        ProblemDto problemDto = jsonb.fromJson(putResponse.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        putResponse.then().assertThat().
            statusCode(NOT_FOUND).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(vaultEntryIdProblem, problemDto);

        logout(token);
    }

    @Test
    public void deleteById_returnsNoContent() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);
        long id = getIdFromLocationHeader(postResponse);

        // DELETE
        delete(token, id).
            then().assertThat().
                statusCode(NO_CONTENT);

        logout(token);
    }

    @Test
    public void deleteByWrongId_returnsVaultEntryIdProblem() {
        createAccount();
        String token = login();

        // POST
        Response postResponse = post(token, EXAMPLE_ENC_DATA);
        long id = getIdFromLocationHeader(postResponse);
        long wrongId = id + 1;

        // DELETE
        Response deleteResponse = delete(token, wrongId);
        ProblemDto problemDto = jsonb.fromJson(deleteResponse.getBody().asString(), ProblemDto.class);
        problemDto.setStatus(null);

        deleteResponse.then().assertThat().
            statusCode(NOT_FOUND).
                and().
            contentType(mimeProblemMatcher);
             // and()
            assertEquals(vaultEntryIdProblem, problemDto);

        logout(token);
    }

    private void createAccount() {
        createAccount(getPayload());
    }

    private void createAccount(String payload) {
        given().
            contentType(MediaType.APPLICATION_JSON).body(payload).
        when().
            post("pswd-mngr/account").
        then().assertThat().
            statusCode(OK);
    }

    private String login() {
        return login(getPayload());
    }

    private String login(String payload) {
        Response loginResponse =
            given().
                contentType(MediaType.APPLICATION_JSON).body(payload).
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
        return getPayload("JohnDoe");
    }

    private String getPayload(String name) {
        AuthenticationDto dto = new AuthenticationDto();
        dto.setUsername(name);
        dto.setMasterPswd("FY7iq0Y1ja2loHmMurgM78VW8Kt3PUpK2oKNjSd0Tt8=");
        return jsonb.toJson(dto);
    }

    private Long getIdFromLocationHeader(Response response) {
        try {
            String locationHeader = response.getHeader(HttpHeaders.LOCATION);
            String path = new URI(locationHeader).getPath();
            return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        }
        catch (URISyntaxException ex) {
            fail(ex);
        }
        return null; // appeasing the compiler: this line will never be executed
    }

    private Header tokenToAuthHeader(String token) {
        return new Header(HttpHeaders.AUTHORIZATION, AuthenticationRequestFilter.AUTH_SCHEME + " " + token);
    }

}
