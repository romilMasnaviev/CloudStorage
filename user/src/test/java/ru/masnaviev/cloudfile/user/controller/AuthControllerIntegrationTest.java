package ru.masnaviev.cloudfile.user.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.masnaviev.cloudfile.user.AbstractIntegrationTest;
import ru.masnaviev.cloudfile.user.dto.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.ErrorResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static ru.masnaviev.cloudfile.user.TestData.PASSWORD;
import static ru.masnaviev.cloudfile.user.TestData.USERNAME;
import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.*;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private final Gson gson = new Gson();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void clearDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void whenValidData_thenUserRegistrationSucceeds() throws Exception {
        MockHttpServletResponse response = performRegistration(USERNAME, PASSWORD, null);
        var registrationResponse = gson.fromJson(response.getContentAsString(), UserRegistrationResponse.class);

        assertEquals(USERNAME, registrationResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    void whenUserAlreadyExists_thenReturnErrorMessage() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);

        MockHttpServletResponse response = performRegistration(USERNAME, PASSWORD, null);
        var errorResponse = gson.fromJson(response.getContentAsString(), ErrorResponse.class);

        assertEquals(USER_ALREADY_EXISTS, errorResponse.message());
        assertEquals(409, response.getStatus());
    }

    @Test
    void whenUserExists_thenUserAuthorizationSucceeds() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);

        MockHttpServletResponse response = performAuthorization(USERNAME, PASSWORD, null);
        var authResponse = gson.fromJson(response.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, authResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    void whenUserAlreadyAuthorized_thenReturnAccessDeniedOnReAuth() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse firstAuthResponse = performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse secondAuthResponse = performAuthorization(USERNAME, PASSWORD, firstAuthResponse.getCookies());
        var errorResponse = gson.fromJson(secondAuthResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(ACCESS_DENIED, errorResponse.message());
        assertEquals(403, secondAuthResponse.getStatus());
    }

    @Test
    void whenUserAlreadyAuthorized_thenReturnAccessDeniedOnReRegister() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse reRegisterResponse = performRegistration(USERNAME, PASSWORD, authResponse.getCookies());

        var errorResponse = gson.fromJson(reRegisterResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(ACCESS_DENIED, errorResponse.message());
        assertEquals(403, reRegisterResponse.getStatus());
    }

    @Test
    void whenUserAuthorized_thenGetUserMeInfo() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse meResponse = performGetMe(authResponse.getCookies());
        var meInfo = gson.fromJson(meResponse.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, meInfo.username());
        assertEquals(200, meResponse.getStatus());
    }

    @Test
    void whenUserUnauthorized_thenReturnUnauthorizedError() throws Exception {
        MockHttpServletResponse response = performGetMe(null);

        var errorResponse = gson.fromJson(response.getContentAsString(), ErrorResponse.class);

        assertEquals(UNAUTHORIZED, errorResponse.message());
        assertEquals(401, response.getStatus());
    }

    @Test
    void whenUserLogsOut_thenAccessDeniedAfterLogout() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = performAuthorization(USERNAME, PASSWORD, null);
        performGetMe(authResponse.getCookies());
        MockHttpServletResponse logoutResponse = performSignOut(authResponse.getCookies());

        MockHttpServletResponse meResponse = performGetMe(logoutResponse.getCookies());
        var errorResponse = gson.fromJson(meResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(UNAUTHORIZED, errorResponse.message());
        assertEquals(401, meResponse.getStatus());
    }

    @Test
    void whenInvalidCredentials_thenReturnBadCredentialsError() throws Exception {
        performRegistration(USERNAME, PASSWORD, null);

        MockHttpServletResponse response = performAuthorization(USERNAME, PASSWORD + "1", null);
        var errorResponse = gson.fromJson(response.getContentAsString(), ErrorResponse.class);

        assertEquals(BAD_CREDENTIALS, errorResponse.message());
        assertEquals(401, response.getStatus());
    }

    private MockHttpServletResponse performRegistration(String username, String password, Cookie[] cookies) throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);

        MockHttpServletRequestBuilder builder = post(AUTH_SIGN_UP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(gson.toJson(request));

        if (cookies != null && cookies.length > 0) {
            builder.cookie(cookies);
        }

        return mockMvc.perform(builder)
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse performAuthorization(String username, String password, Cookie[] cookies) throws Exception {
        UserAuthorizationRequest request = new UserAuthorizationRequest(username, password);

        MockHttpServletRequestBuilder builder = post(AUTH_SIGN_IN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(gson.toJson(request));

        if (cookies != null && cookies.length > 0) {
            builder.cookie(cookies);
        }

        return mockMvc.perform(builder)
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse performSignOut(Cookie[] cookies) throws Exception {
        MockHttpServletRequestBuilder builder = post(AUTH_SIGN_OUT_URL)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        if (cookies != null && cookies.length > 0) {
            builder.cookie(cookies);
        }

        return mockMvc.perform(builder)
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse performGetMe(Cookie[] cookies) throws Exception {
        MockHttpServletRequestBuilder builder = get(USER_ME_URL)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        if (cookies != null && cookies.length > 0) {
            builder.cookie(cookies);
        }

        return mockMvc.perform(builder)
                .andReturn()
                .getResponse();
    }


}





















