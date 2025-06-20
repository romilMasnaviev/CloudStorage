package ru.masnaviev.cloudfile.user;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.masnaviev.cloudfile.user.dto.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.exception.ErrorResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.*;

public class MockMvcTestHelper {

    private final Gson gson = new Gson();
    private final MockMvc mockMvc;

    public MockMvcTestHelper(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public MockHttpServletResponse performGetMe(Cookie[] cookies) throws Exception {
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

    public MockHttpServletResponse performAuthorization(String username, String password, Cookie[] cookies) throws Exception {
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

    public MockHttpServletResponse performSignOut(Cookie[] cookies) throws Exception {
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

    public MockHttpServletResponse performRegistration(String username, String password, Cookie[] cookies) throws Exception {
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

    public void checkErrorResponse(MockHttpServletResponse actualErrorResponse,
                                   String expectedErrorMessage,
                                   int expectedStatusCode) throws UnsupportedEncodingException {

        var errorResponse = gson.fromJson(actualErrorResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(expectedErrorMessage, errorResponse.message());
        assertEquals(expectedStatusCode, actualErrorResponse.getStatus());
    }
}
