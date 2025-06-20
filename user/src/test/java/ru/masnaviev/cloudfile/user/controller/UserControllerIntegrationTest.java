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
import ru.masnaviev.cloudfile.user.dto.response.UserAuthorizationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.masnaviev.cloudfile.user.TestData.PASSWORD;
import static ru.masnaviev.cloudfile.user.TestData.USERNAME;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.UNAUTHORIZED;

@AutoConfigureMockMvc
@SpringBootTest
@Import(MockMvcHelperConfig.class)
class UserControllerIntegrationTest extends AbstractIntegrationTest {

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
    void getUserMe_whenUserAuthorized_thenGetUserMeInfo() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse meResponse = testHelper.performGetMe(authResponse.getCookies());
        var meInfo = gson.fromJson(meResponse.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, meInfo.username());
        assertEquals(200, meResponse.getStatus());
    }

    @Test
    void getUserMe_whenUserUnauthorized_thenReturnUnauthorizedError() throws Exception {
        MockHttpServletResponse response = testHelper.performGetMe(null);
        testHelper.checkErrorResponse(response, UNAUTHORIZED, 401);
    }

    @Test
    void getUserMe_whenUserLogsOut_thenAccessDeniedAfterLogout() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);
        MockHttpServletResponse logoutResponse = testHelper.performSignOut(authResponse.getCookies());

        MockHttpServletResponse meResponse = testHelper.performGetMe(logoutResponse.getCookies());

        testHelper.checkErrorResponse(meResponse, UNAUTHORIZED, 401);
    }

}