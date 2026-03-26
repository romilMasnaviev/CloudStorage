package ru.masnaviev.cloudstorage.controller;


import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.masnaviev.cloudstorage.AbstractIntegrationTest;
import ru.masnaviev.cloudstorage.MockMvcHelperConfig;
import ru.masnaviev.cloudstorage.MockMvcTestHelper;
import ru.masnaviev.cloudstorage.dto.response.user.UserAuthorizationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.masnaviev.cloudstorage.TestData.PASSWORD;
import static ru.masnaviev.cloudstorage.TestData.USERNAME;
import static ru.masnaviev.cloudstorage.constatnts.ErrorMessages.UNAUTHORIZED;

@AutoConfigureMockMvc
@SpringBootTest
@Import(MockMvcHelperConfig.class)
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private final Gson gson = new Gson();

    @Autowired
    MockMvcTestHelper testHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    @DisplayName("Получение информации о себе: авторизованный пользователь успешно получает данные")
    void getUserMe_whenUserAuthorized_thenReturnsUserInfo() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse meResponse = testHelper.performGetMe(authResponse.getCookies());
        var meInfo = gson.fromJson(meResponse.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, meInfo.username());
        assertEquals(200, meResponse.getStatus());
    }

    @Test
    @DisplayName("Получение информации о себе: неавторизованный пользователь получает 401 Unauthorized")
    void getUserMe_whenUserUnauthorized_thenReturnUnauthorizedError() throws Exception {
        MockHttpServletResponse response = testHelper.performGetMe(null);
        testHelper.checkStatusAndMessage(response, UNAUTHORIZED, 401);
    }

    @Test
    @DisplayName("Получение информации о себе: после логаута доступ запрещен, возвращается 401 Unauthorized")
    void getUserMe_whenUserLoggedOut_thenReturnUnauthorizedError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);
        MockHttpServletResponse logoutResponse = testHelper.performSignOut(authResponse.getCookies());

        MockHttpServletResponse meResponse = testHelper.performGetMe(logoutResponse.getCookies());

        testHelper.checkStatusAndMessage(meResponse, UNAUTHORIZED, 401);
    }

}