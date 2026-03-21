package ru.masnaviev.cloudfile.controller;

import com.google.gson.Gson;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import ru.masnaviev.cloudfile.AbstractIntegrationTest;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.helpers.MockMvcTestHelper;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudfile.TestData.*;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.ResourceType.FILE;

@AutoConfigureMockMvc
@SpringBootTest
@Import(MockMvcTestHelper.class)
class FileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvcTestHelper testHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MinioClient minioClient;

    private final Gson gson = new Gson();

    @BeforeEach
    void clearDb() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");

        clearMinioBucket();
    }

    @Test
    void uploadResources_whenOneFile_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());

        var response = testHelper.performUploadFile(file, "/", mockHttpServletResponse.getCookies());

        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt");

        assertEquals(200, response.getStatus());
        assertEquals(file.getSize(), minioResponse.size());
    }

    @Test
    @DisplayName("Загрузка нескольких файлов в одном запросе, в именах файлах есть пути")
    void uploadResources_whenNeOneFile_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performUpload3Files(file1, file2, file3, "", mockHttpServletResponse.getCookies());

        var minioResponse1 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt");
        var minioResponse2 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello.txt");
        var minioResponse3 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/folder2/hello.txt");

        assertEquals(200, response.getStatus());

//        gson.fromJson(response.getContentAsString(),new TypeToken<List<ResourceInfoResponse>>(){}.getType());

        assertEquals(file1.getSize(), minioResponse1.size());
        assertEquals(file2.getSize(), minioResponse2.size());
        assertEquals(file3.getSize(), minioResponse3.size());
    }

    @Test
    void uploadResources_whenFileAlreadyExists_thenReturnException() throws Exception {

        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file1, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performUploadFile(file1, "", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, FILE_ALREADY_EXIST, 409);
    }


    @Test
    void getResourceInfo_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file1, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performGetResourceInfo("hello.txt", mockHttpServletResponse.getCookies());


        assertEquals(200, response.getStatus());
        var gsonResponse = gson.fromJson(response.getContentAsString(), ResourceInfoResponse.class);
        assertEquals(file1.getSize(), gsonResponse.getSize());
        assertEquals(FILE, gsonResponse.getResourceType());
    }

    @Test
    void getResourceInfo_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file2, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performGetResourceInfo("folder1/", mockHttpServletResponse.getCookies());


        assertEquals(200, response.getStatus());
        var gsonResponse = gson.fromJson(response.getContentAsString(), ResourceInfoResponse.class);
        assertEquals(DIRECTORY, gsonResponse.getResourceType());
    }

    @Test
    void getResourceInfo_whenFileDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performGetResourceInfo("test.txt", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, 404);
    }

    @Test
    void getResourceInfo_whenDirectoryDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performGetResourceInfo("folder1/", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, 404);
    }

    @Test
    void getResourceInfo_whenPathDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performGetResourceInfo("folder/pathDoesntExist/", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, 400);
    }

    @Test
    void deleteResource_whenTryToDeleteParentDirectory_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDeleteResource("/", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, PROTECTED_PARENT_DIRECTORY, 405);
    }

    @Test
    void deleteResource_whenFileDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDeleteResource("test.txt", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, 404);
    }

    @Test
    void deleteResource_whenDirectoryDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDeleteResource("folder1/", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, 404);
    }

    @Test
    void deleteResource_whenPathDoesntExist_thenException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDeleteResource("folder1/someFile", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, 400);
    }

    @Test
    void deleteResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file1, "", mockHttpServletResponse.getCookies());
        var minioResponseBeforeDelete = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt");
        assertEquals(file1.getSize(), minioResponseBeforeDelete.size());

        var responseAfterDelete = testHelper.performDeleteResource("hello.txt", mockHttpServletResponse.getCookies());

        assertEquals(204, responseAfterDelete.getStatus());

        var minioException = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt"));
        assertEquals("Object does not exist", minioException.getMessage());
    }

    @Test
    void deleteResource_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUpload3Files(file1, file2, file3, "", mockHttpServletResponse.getCookies());

        var responseBeforeDelete1 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt");
        var responseBeforeDelete2 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello.txt");
        var responseBeforeDelete3 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/folder2/hello.txt");

        assertEquals(file1.getSize(), responseBeforeDelete1.size());
        assertEquals(file2.getSize(), responseBeforeDelete2.size());
        assertEquals(file3.getSize(), responseBeforeDelete3.size());

        var responseAfterDelete = testHelper.performDeleteResource("folder1/", mockHttpServletResponse.getCookies());
        assertEquals(204, responseAfterDelete.getStatus());

        var responseAfterDelete1 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt");
        var minioExceptionAfterDelete2 = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello.txt"));
        var minioExceptionAfterDelete3 = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/folder2/hello.txt"));

        assertEquals(file1.getSize(), responseAfterDelete1.size());
        assertEquals("Object does not exist", minioExceptionAfterDelete2.getMessage());
        assertEquals("Object does not exist", minioExceptionAfterDelete3.getMessage());
    }

    @Test
    void downloadResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file1, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performDownloadResource("hello.txt", mockHttpServletResponse.getCookies());

        assertArrayEquals(file1.getBytes(), response.getContentAsByteArray());
    }

    @Test
    void downloadResource_whenFileExists_thenReturnValidResponse1() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUploadFile(file2, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performDownloadResource("folder1/hello.txt", mockHttpServletResponse.getCookies());

        assertArrayEquals(file1.getBytes(), response.getContentAsByteArray());
    }

    @Test
    void downloadResource_whenDirectoryExists_thenReturnValidResponse1() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        testHelper.performUpload3Files(file4, file5, file6, "", mockHttpServletResponse.getCookies());

        var response = testHelper.performDownloadResource("folder1/", mockHttpServletResponse.getCookies());

        assertTrue(testHelper.checkFileInZipResponse(response, file4));
        assertTrue(testHelper.checkFileInZipResponse(response, file5));
        assertTrue(testHelper.checkFileInZipResponse(response, file6));
    }

    @Test
    void downloadResource_whenFileDoesntExist_thenReturnException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDownloadResource("hello.txt", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, 404);
    }

    @Test
    void downloadResource_whenPathDoesntExist_thenReturnException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performDownloadResource("folder/pathDoesntExist/", mockHttpServletResponse.getCookies());

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, 400);
    }

    @Test
    void uploadDirectory_whenPathExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performUploadDirectory("folder/", mockHttpServletResponse.getCookies());
        assertEquals(201, response.getStatus());
        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/");

        assertEquals(0, minioResponse.size());
    }

    @Test
    void uploadDirectory_whenDirectoryAlreadyExists_thenReturnException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performUploadDirectory("folder/", mockHttpServletResponse.getCookies());
        assertEquals(201, response.getStatus());

        var response2 = testHelper.performUploadDirectory("folder/", mockHttpServletResponse.getCookies());
        testHelper.checkStatusAndMessage(response2, DIRECTORY_ALREADY_EXISTS, 409);
    }

    @Test
    void uploadDirectory_whenParentPathDoesntExist_thenReturnException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performUploadDirectory("folder/folder2/", mockHttpServletResponse.getCookies());
        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, 404);
    }

    @Test
    void uploadDirectory_whenInvalidPath_thenReturnException() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        var response = testHelper.performUploadDirectory("folder", mockHttpServletResponse.getCookies());
        testHelper.checkStatusAndMessage(response, PATH_MUST_BE_END_SLASH, 400);
    }

    @Test
    void getDirectoryContentInfo_whenFilesExists_thenReturnValidResponse() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        var mockHttpServletResponse = testHelper.performAuthorization(USERNAME, PASSWORD, null);

        MockHttpServletResponse response1 = testHelper.performUpload3Files(file4, file5, file6, "", mockHttpServletResponse.getCookies());
        System.out.println("response1.getStatus() = " + response1.getStatus());


        var response = testHelper.performGetDirectoryContentsInfo("folder1/", mockHttpServletResponse.getCookies());
        assertEquals(200, response.getStatus());


    }


    private void clearMinioBucket() throws Exception {
        Iterable<Result<Item>> iterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket("user-files")
                .recursive(true)
                .build());

        for (Result<Item> result : iterable) {
            Item item = result.get();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object(item.objectName())
                            .build()
            );
        }

        minioClient.removeBucket(
                RemoveBucketArgs.builder().bucket("user-files").build()
        );

        minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket("user-files")
                .build());
    }

}