package ru.masnaviev.cloudfile.user.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import ru.masnaviev.cloudfile.user.AbstractIntegrationTest;
import ru.masnaviev.cloudfile.user.MockMvcHelperConfig;
import ru.masnaviev.cloudfile.user.MockMvcTestHelper;
import ru.masnaviev.cloudfile.user.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.dto.response.user.UserRegistrationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.masnaviev.cloudfile.user.TestData.PASSWORD;
import static ru.masnaviev.cloudfile.user.TestData.USERNAME;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

@AutoConfigureMockMvc
@SpringBootTest
@Import(MockMvcHelperConfig.class)
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private final Gson gson = new Gson();

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MockMvcTestHelper testHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void registerUser_whenValidData_thenUserRegistrationSucceeds() throws Exception {
        MockHttpServletResponse response = testHelper.performRegistration(USERNAME, PASSWORD, null);
        var registrationResponse = gson.fromJson(response.getContentAsString(), UserRegistrationResponse.class);

        assertEquals(USERNAME, registrationResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    void registerUser_whenUserAlreadyExists_thenReturnErrorMessage() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);

        MockHttpServletResponse response = testHelper.performRegistration(USERNAME, PASSWORD, null);

        testHelper.checkErrorResponse(response, USER_ALREADY_EXISTS, 409);
    }

    @Test
    void registerUser_whenUserAlreadyAuthorized_thenReturnAccessDeniedOnReRegister() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse reRegisterResponse = testHelper.performRegistration(USERNAME, PASSWORD, authResponse.getCookies());

        testHelper.checkErrorResponse(reRegisterResponse, ACCESS_DENIED, 403);
    }

    @Test
    void authorizeUser_whenUserExists_thenUserAuthorizationSucceeds() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse response = testHelper.performAuthorization(USERNAME, PASSWORD, null);
        var authResponse = gson.fromJson(response.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, authResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    void authorizeUser_whenUserAlreadyAuthorized_thenReturnAccessDeniedOnReAuth() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse firstAuthResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse secondAuthResponse = testHelper.performAuthorization(USERNAME, PASSWORD, firstAuthResponse.getCookies());

        testHelper.checkErrorResponse(secondAuthResponse, ACCESS_DENIED, 403);
    }

    @Test
    void authorizeUser_whenInvalidCredentials_thenReturnBadCredentialsError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);

        MockHttpServletResponse response = testHelper.performAuthorization(USERNAME, PASSWORD + "1", null);

        testHelper.checkErrorResponse(response, BAD_CREDENTIALS, 401);
    }
}





















