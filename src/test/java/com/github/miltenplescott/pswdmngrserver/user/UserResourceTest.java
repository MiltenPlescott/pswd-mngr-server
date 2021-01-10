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
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class UserResourceTest {

    private static final String EXAMPLE_USERNAME = "JohnDoe";
    private static final String EXAMPLE_USERNAME_2 = "SomeoneElse";
    private static final String EXAMPLE_MASTERPSWD = "FY7iq0Y1ja2loHmMurgM78VW8Kt3PUpK2oKNjSd0Tt8=";
    private static final String EXAMPLE_MASTERPSWD_2 = "la0kPjgwVQUm4wcu3druz2cSGM2Q2BIwu8mSwv9LNz8=";
    private static final int OK = javax.ws.rs.core.Response.Status.OK.getStatusCode();
    private static final int BAD_REQUEST = javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode();
    private static final int UNAUTHORIZED = javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode();

    private static List<String> createdUsernames;
    private static EntityManagerFactory emf;
    private static EntityManager em;

    private Jsonb jsonb;

    private static final ProblemDto usernameProblem;
    private static final ProblemDto pswdLengthProblem;
    private static final ProblemDto pswdFormatProblem;
    private static final ProblemDto authProblem;

    private static final ProblemDto authHeaderProblem;
    private static final ProblemDto tokenLengthProblem;
    private static final ProblemDto tokenFormatProblem;
    private static final ProblemDto tokenExpiredProblem;

    static {
        usernameProblem = UserProblems.createDefaultUsernameProblem();
        pswdLengthProblem = UserProblems.createDefaultPasswordProblem();
        pswdFormatProblem = UserProblems.createDefaultPasswordProblem();
        authProblem = UserProblems.createDefaultAuthProblem();
        UserProblems.usernameNotUniqueProblem(usernameProblem);
        UserProblems.masterPswdLengthProblem(pswdLengthProblem);
        UserProblems.masterPswdFormatProblem(pswdFormatProblem);
        UserProblems.authProblem(authProblem);

        authHeaderProblem = UserProblems.createDefaultAuthorizationHeaderProblem();
        tokenLengthProblem = UserProblems.createDefaultTokenProblem();
        tokenFormatProblem = UserProblems.createDefaultTokenProblem();
        tokenExpiredProblem = UserProblems.createDefaultTokenProblem();
        UserProblems.authorizationHeaderProblem(authHeaderProblem);
        UserProblems.tokenLengthProblem(tokenLengthProblem);
        UserProblems.tokenFormatProblem(tokenFormatProblem);
        UserProblems.tokenExpiredProblem(tokenExpiredProblem);
    }

    @BeforeAll
    public static void initAll() {
        createdUsernames = new ArrayList<>();
        emf = Persistence.createEntityManagerFactory("test-tcp");
        em = emf.createEntityManager();
    }

    @BeforeEach
    public void initEach() {
        jsonb = JsonbBuilder.create();
    }

    @AfterEach
    public void tearDownEach() {
        for (Iterator<String> it = createdUsernames.iterator(); it.hasNext();) {
            String username = it.next();
            try {
                em.getTransaction().begin();
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaDelete<User> cd = cb.createCriteriaDelete(User.class);
                Root<User> root = cd.from(User.class);
//                cd.where(cb.equal(root.get("username"), username));  // using string
                cd.where(cb.equal(root.get(User_.username), username));  // using metamodel
                em.createQuery(cd).executeUpdate();
                em.getTransaction().commit();
                it.remove();
            }
            catch (Exception e) {
                em.getTransaction().rollback();
            }
        }
    }

    @AfterAll
    public static void tearDownAll() {
        em.close();
        emf.close();
    }

    private String createAuthenticationDtoPayload(String username, String pswd) {
        AuthenticationDto dto = new AuthenticationDto();
        dto.setUsername(username);
        dto.setMasterPswd(pswd);
        return jsonb.toJson(dto);
    }

    // returns true, if a and b are within 10 seconds of each other
    private static boolean areCloseEnough(Instant a, Instant b) {
        return a.isBefore(b.plusSeconds(10)) && a.isAfter(b.minusSeconds(10));
    }

    private void createValidAccount(String payload) {
        given().
            contentType(MediaType.APPLICATION_JSON).body(payload).
        when().
            post("pswd-mngr/account").
        then().assertThat().
            statusCode(OK);
    }

    // same as createValidAccount(), but without assertThat().statusCode(OK)
    private Response createInvalidAccount(String payload) {
        return
            given().
                contentType(MediaType.APPLICATION_JSON).body(payload).
            when().
                post("pswd-mngr/account");
    }

    private Response login(String payload) {
        return
            given().
                contentType(MediaType.APPLICATION_JSON).body(payload).
            when().
                post("pswd-mngr/account/login");
    }

    private Response logout(Header header) {
        return
            given().
                header(header).
            when().
                post("pswd-mngr/account/logout");
    }

    private Header getCorrectAuthHeader(String token) {
        return new Header(HttpHeaders.AUTHORIZATION, AuthenticationRequestFilter.AUTH_SCHEME + " " + token);
    }

    @Test
    public void createAccount_returnsOk() {
        String registerPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(registerPayload);
    }

    @Test
    public void createAccount_withDuplicateUsername_returnsUsernameNotUniqueProblem() {
        String registerPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);

        createValidAccount(registerPayload);  // create an account
        Response response = createInvalidAccount(registerPayload);  // try to create an account using the same username

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, usernameProblem);
    }

    @Test
    public void createAccount_withPswdTooShort_returnsPswdLengthProblem() {
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, "thisIsTooShort==");
        createdUsernames.add(EXAMPLE_USERNAME);

        Response response = createInvalidAccount(payload);

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, pswdLengthProblem);
    }

    @Test
    public void createAccount_withNonBase64Pswd_returnsPswdFormatProblem() {
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, "some extra non-base64 symbols: .,<>:/()[]{}'!@#$%^&*-_=+");
        createdUsernames.add(EXAMPLE_USERNAME);

        Response response = createInvalidAccount(payload);

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, pswdFormatProblem);
    }

    @Test
    public void login_withCorrectUsernameCorrectPassword_returnsOk_andToken() {
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);

        createValidAccount(payload);
        Response response = login(payload);

        AuthTokenResponseDto responseDto = jsonb.fromJson(response.getBody().asString(), AuthTokenResponseDto.class);
        response.
            then().assertThat().
                statusCode(OK).
                    and().
                contentType(allOf(containsString(MediaType.APPLICATION_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(CryptoUtils.decodeToken(responseDto.getToken()).length, AuthTokenManager.TOKEN_LENGTH_BYTES);
                 // and()
                assertEquals(responseDto.getTokenType(), AuthTokenResponseDto.BEARER);
                 // and()
                assertTrue(areCloseEnough(Instant.ofEpochMilli(responseDto.getExpiration()), Instant.now().plus(AuthTokenManager.EXPIRATION_DURATION_MINUTES)));
    }

    @Test
    public void login_withCorrectUsernameIncorrectPassword_returnsAuthProblem_withoutToken() {
        String registerPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        String loginPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD_2);
        createdUsernames.add(EXAMPLE_USERNAME);

        createValidAccount(registerPayload);
        Response response = login(loginPayload);

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, authProblem);
    }

    @Test
    public void login_withIncorrectUsernameCorrectPassword_returnsAuthProblem_withoutToken() {
        String registerPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        String loginPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME_2, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);

        createValidAccount(registerPayload);
        Response response = login(loginPayload);

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, authProblem);
    }

    @Test
    public void login_withIncorrectUsernameIncorrectPassword_returnsAuthProblem_withoutToken() {
        String registerPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        String loginPayload = createAuthenticationDtoPayload(EXAMPLE_USERNAME_2, EXAMPLE_MASTERPSWD_2);
        createdUsernames.add(EXAMPLE_USERNAME);

        createValidAccount(registerPayload);
        Response response = login(loginPayload);

        ProblemDto responseDto = jsonb.fromJson(response.getBody().asString(), ProblemDto.class);
        responseDto.setStatus(null);
        response.
            then().assertThat().
                statusCode(BAD_REQUEST).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(responseDto, authProblem);
    }

    @Test
    public void logout_returnsOk() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        Response loginResponse = login(payload);
        AuthTokenResponseDto loginResponseDto = jsonb.fromJson(loginResponse.getBody().asString(), AuthTokenResponseDto.class);

        // logout (i.e. access @Secured resource)
        Response logoutResponse = logout(getCorrectAuthHeader(loginResponseDto.getToken()));
        logoutResponse.
            then().assertThat().
                statusCode(OK);
    }

    @Test
    public void logout_withMissingAuthHeader_returnsAuthHeaderProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        login(payload);

        // logout (i.e. access @Secured resource)
        Response logoutResponse =
            when().
                post("pswd-mngr/account/logout");
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, authHeaderProblem);
    }

    @Test
    public void logout_withEmptyAuthHeader_returnsAuthHeaderProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        login(payload);

        // logout (i.e. access @Secured resource)
        Response logoutResponse = logout(new Header(HttpHeaders.AUTHORIZATION, ""));
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, authHeaderProblem);
    }

    @Test
    public void logout_withMultipleAuthHeaders_returnsAuthHeaderProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        Response loginResponse = login(payload);
        AuthTokenResponseDto loginResponseDto = jsonb.fromJson(loginResponse.getBody().asString(), AuthTokenResponseDto.class);

        // logout (i.e. access @Secured resource)
        Response logoutResponse =
            given().
                header(getCorrectAuthHeader(loginResponseDto.getToken())).
                header(getCorrectAuthHeader(loginResponseDto.getToken())).
            when().
                post("pswd-mngr/account/logout");
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, authHeaderProblem);
    }

    @Test
    public void logout_withIncorrectAuthHeaderFormat_returnsAuthHeaderProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        Response loginResponse = login(payload);
        AuthTokenResponseDto loginResponseDto = jsonb.fromJson(loginResponse.getBody().asString(), AuthTokenResponseDto.class);

        // logout (i.e. access @Secured resource)
        Response logoutResponse = logout(new Header(
            HttpHeaders.AUTHORIZATION,
            "Flu" + AuthenticationRequestFilter.AUTH_SCHEME + " " + loginResponseDto.getToken())
        );
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, authHeaderProblem);
    }

    @Test
    public void logout_withTokenTooShort_returnsTokenLengthProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        login(payload);

        // logout (i.e. access @Secured resource)
        Response logoutResponse = logout(getCorrectAuthHeader("thisIsTooShort=="));
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, tokenLengthProblem);
    }

    @Test
    public void logout_withNonBase64Token_returnsTokenFormatProblem() {
        // create account
        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
        createdUsernames.add(EXAMPLE_USERNAME);
        createValidAccount(payload);

        // login
        login(payload);

        // logout (i.e. access @Secured resource)
        // don't include a comma so you don't trigger authorization header problem
        Response logoutResponse = logout(getCorrectAuthHeader("some extra non-base64 symbols: .<>:/()[]{}'!@#$%^&*-_=+"));
        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
        logoutResponseDto.setStatus(null);
        logoutResponse.
            then().assertThat().
                statusCode(UNAUTHORIZED).
                    and().
                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
                    and().
                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
                 // and()
                assertEquals(logoutResponseDto, tokenFormatProblem);
    }

    // test works, but takes 15 minutes to run so it's commented out, so run it only once in a while
//    @Test
//    public void logout_withExpiredToken_returnsTokenExpiredProblem() throws InterruptedException {
//        // create account
//        String payload = createAuthenticationDtoPayload(EXAMPLE_USERNAME, EXAMPLE_MASTERPSWD);
//        createdUsernames.add(EXAMPLE_USERNAME);
//        createValidAccount(payload);
//
//        // login
//        Response loginResponse = login(payload);
//        AuthTokenResponseDto loginResponseDto = jsonb.fromJson(loginResponse.getBody().asString(), AuthTokenResponseDto.class);
//
//        System.out.println("Waiting for " + AuthTokenManager.EXPIRATION_DURATION_MINUTES.plusSeconds(10).toMinutes() + " minutes!");
//        Thread.sleep(AuthTokenManager.EXPIRATION_DURATION_MINUTES.plusSeconds(10).toMillis());
//
//        // logout (i.e. access @Secured resource)
//        Response logoutResponse = logout(getCorrectAuthHeader(loginResponseDto.getToken()));
//        ProblemDto logoutResponseDto = jsonb.fromJson(logoutResponse.getBody().asString(), ProblemDto.class);
//        logoutResponseDto.setStatus(null);
//        logoutResponse.
//            then().assertThat().
//                statusCode(UNAUTHORIZED).
//                    and().
//                header(HttpHeaders.WWW_AUTHENTICATE, AuthenticationRequestFilter.AUTH_SCHEME).
//                    and().
//                contentType(allOf(containsString(ProblemDto.MEDIA_TYPE_PROBLEM_JSON), containsString(StandardCharsets.UTF_8.name())));
//                 // and()
//                assertEquals(logoutResponseDto, tokenExpiredProblem);
//    }

}
