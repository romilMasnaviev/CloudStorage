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
import ru.masnaviev.cloudstorage.dto.response.user.UserRegistrationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.masnaviev.cloudstorage.TestData.PASSWORD;
import static ru.masnaviev.cloudstorage.TestData.USERNAME;
import static ru.masnaviev.cloudstorage.constatnts.ErrorMessages.ACCESS_DENIED;
import static ru.masnaviev.cloudstorage.constatnts.ErrorMessages.USER_ALREADY_EXISTS;

@AutoConfigureMockMvc
@SpringBootTest
@Import(MockMvcHelperConfig.class)
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Регистрация: при передаче валидных данных пользователь успешно регистрируется")
    void registerUser_whenValidData_thenRegistrationSucceeds() throws Exception {
        MockHttpServletResponse response = testHelper.performRegistration(USERNAME, PASSWORD, null);

        var registrationResponse = gson.fromJson(response.getContentAsString(), UserRegistrationResponse.class);

        assertEquals(USERNAME, registrationResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Регистрация: попытка зарегистрировать существующего пользователя возвращает 409 Conflict")
    void registerUser_whenUserAlreadyExists_thenReturnConflictError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);

        var response = testHelper.performRegistration(USERNAME, PASSWORD, null);

        testHelper.checkStatusAndMessage(response, USER_ALREADY_EXISTS, 409);
    }

    @Test
    @DisplayName("Регистрация: авторизованный пользователь не может зарегистрироваться, возвращается 403 Forbidden")
    void registerUser_whenUserAlreadyAuthorized_thenReturnAccessDeniedError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse authResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var reRegisterResponse = testHelper.performRegistration(USERNAME, PASSWORD, authResponse.getCookies());

        testHelper.checkStatusAndMessage(reRegisterResponse, ACCESS_DENIED, 403);
    }

    @Test
    @DisplayName("Авторизация: при верных учетных данных пользователь успешно авторизуется")
    void authorizeUser_whenValidCredentials_thenAuthorizationSucceeds() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse response = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var authResponse = gson.fromJson(response.getContentAsString(), UserAuthorizationResponse.class);

        assertEquals(USERNAME, authResponse.username());
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Авторизация: попытка повторной авторизации залогиненного пользователя возвращает 403 Forbidden")
    void authorizeUser_whenUserAlreadyAuthorized_thenReturnAccessDeniedError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        MockHttpServletResponse firstAuthResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var secondAuthResponse = testHelper.performAuthorization(USERNAME, PASSWORD, firstAuthResponse.getCookies());

        testHelper.checkStatusAndMessage(secondAuthResponse, ACCESS_DENIED, 403);
    }

    @Test
    @DisplayName("Авторизация: при неверном пароле возвращается 401 Unauthorized")
    void authorizeUser_whenInvalidCredentials_thenReturnUnauthorizedError() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);

        var response = testHelper.performAuthorization(USERNAME, PASSWORD + "1", null);

        testHelper.checkStatusAndMessage(response, "Bad credentials", 401);
    }
}
