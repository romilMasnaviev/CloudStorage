package ru.masnaviev.cloudfile.helpers;

import com.google.gson.Gson;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.masnaviev.cloudfile.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.exception.ErrorResponse;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static ru.masnaviev.cloudfile.constatnts.ApiPath.*;

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

    public MockHttpServletResponse performUploadFile(MockMultipartFile file, String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .multipart(UPLOAD_RESOURCE)
                        .file(file)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performGetResourceInfo(String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_RESOURCE_INFO)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performDeleteResource(String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .delete(DELETE_RESOURCE)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performUpload3Files(MockMultipartFile file1,
                                                       MockMultipartFile file2,
                                                       MockMultipartFile file3,
                                                       String path,
                                                       Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .multipart(UPLOAD_RESOURCE)
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performDownloadResource(String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(DOWNLOAD_RESOURCE)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performUploadDirectory(String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post(UPLOAD_DIRECTORY)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performGetDirectoryContentsInfo(String path, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(GET_DIRECTORY_CONTENTS_INFO)
                        .param("path", path)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performMoveResource(String pathFrom, String pathTo, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(MOVE_RESOURCE)
                        .param("from", pathFrom)
                        .param("to", pathTo)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public MockHttpServletResponse performSearchResource(String query, Cookie[] cookies) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(FIND_RESOURCE)
                        .param("query", query)
                        .cookie(cookies))
                .andReturn()
                .getResponse();
    }

    public StatObjectResponse performGetStatObjectFromMinio(MinioClient minioClient, String path) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket("user-files")
                .object(path)
                .build());
    }

    public boolean checkFileInZipResponse(MockHttpServletResponse actualResponse, MockMultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();

        try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(actualResponse.getContentAsByteArray()))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(fileName)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void checkStatusAndMessage(MockHttpServletResponse actualResponse,
                                      String expectedMessage,
                                      int expectedStatusCode) throws UnsupportedEncodingException {

        var response = gson.fromJson(actualResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(expectedMessage, response.message());
        assertEquals(expectedStatusCode, actualResponse.getStatus());
    }
}
