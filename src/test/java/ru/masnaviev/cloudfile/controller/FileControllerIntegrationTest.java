package ru.masnaviev.cloudfile.controller;

import com.google.gson.Gson;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import ru.masnaviev.cloudfile.AbstractIntegrationTest;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.helpers.MockMvcTestHelper;

import java.util.ArrayList;
import java.util.List;

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
    private Cookie[] cookies;

    @BeforeEach
    void clearDb() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
        clearMinioBucket();
        cookies = performAuthorization().getCookies();
    }

    @Test
    @DisplayName("Загрузка одного файла")
    void uploadResources_whenOneFile_thenReturnValidResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello,world".getBytes());

        var response = testHelper.performUploadFile(file, "/", cookies);

        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        assertEquals(file.getSize(), minioResponse.size());
    }

    @Test
    @DisplayName("Загрузка нескольких файлов в одном запросе, в именах файлов есть пути")
    void uploadResources_whenMultipleFiles_thenReturnValidResponse() throws Exception {
        var response = testHelper.performUpload3Files(file1, file2, file3, "", cookies);

        var minioResponse1 = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        var minioResponse2 = testHelper.performGetStatObjectFromMinio(minioClient, file2Path);
        var minioResponse3 = testHelper.performGetStatObjectFromMinio(minioClient, file3Path);

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        assertEquals(file1.getSize(), minioResponse1.size());
        assertEquals(file2.getSize(), minioResponse2.size());
        assertEquals(file3.getSize(), minioResponse3.size());
    }

    @Test
    @DisplayName("Загрузка одного файла, файл уже существует")
    void uploadResources_whenFileAlreadyExists_thenReturnException() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var response = testHelper.performUploadFile(file1, "", cookies);

        testHelper.checkStatusAndMessage(response, FILE_ALREADY_EXIST, HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Получение информации о файле, файл существует")
    void getResourceInfo_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var response = testHelper.performGetResourceInfo("hello.txt", cookies);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        var gsonResponse = gson.fromJson(response.getContentAsString(), ResourceInfoResponse.class);
        assertEquals(file1.getSize(), gsonResponse.getSize());
        assertEquals(FILE, gsonResponse.getResourceType());
    }

    @Test
    @DisplayName("Получение информации о директории, директория существует")
    void getResourceInfo_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file2, "", cookies);

        var response = testHelper.performGetResourceInfo("folder1/", cookies);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        var gsonResponse = gson.fromJson(response.getContentAsString(), ResourceInfoResponse.class);
        assertEquals(DIRECTORY, gsonResponse.getResourceType());
    }

    @Test
    @DisplayName("Получение информации о файле, файл не существует")
    void getResourceInfo_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("test.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Получение информации о директории, директория не существует")
    void getResourceInfo_whenDirectoryDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("folder1/", cookies);

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Получение информации о ресурсе, путь не существует")
    void getResourceInfo_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("folder/pathDoesntExist/", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Удаление ресурса, ресурс - родительская папка")
    void deleteResource_whenTryToDeleteParentDirectory_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("/", cookies);

        testHelper.checkStatusAndMessage(response, PROTECTED_PARENT_DIRECTORY, HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @Test
    @DisplayName("Удаление файла, файл не существует")
    void deleteResource_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("test.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление директории, директория не существует")
    void deleteResource_whenDirectoryDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("folder1/", cookies);

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление директории, пути не существует")
    void deleteResource_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("folder1/someFile", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Удаление файла, файл существует")
    void deleteResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);
        var minioResponseBeforeDelete = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        assertEquals(file1.getSize(), minioResponseBeforeDelete.size());

        var responseAfterDelete = testHelper.performDeleteResource("hello.txt", cookies);

        assertEquals(HttpStatus.NO_CONTENT.value(), responseAfterDelete.getStatus());

        var minioException = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, file1Path));
        assertEquals("Object does not exist", minioException.getMessage());
    }

    @Test
    @DisplayName("Удаление директории и вложенных в нее файлов")
    void deleteResource_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUpload3Files(file1, file2, file3, "", cookies);

        var responseBeforeDelete1 = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        var responseBeforeDelete2 = testHelper.performGetStatObjectFromMinio(minioClient, file2Path);
        var responseBeforeDelete3 = testHelper.performGetStatObjectFromMinio(minioClient, file3Path);

        assertEquals(file1.getSize(), responseBeforeDelete1.size());
        assertEquals(file2.getSize(), responseBeforeDelete2.size());
        assertEquals(file3.getSize(), responseBeforeDelete3.size());

        var responseAfterDelete = testHelper.performDeleteResource("folder1/", cookies);
        assertEquals(HttpStatus.NO_CONTENT.value(), responseAfterDelete.getStatus());

        var responseAfterDelete1 = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        var minioExceptionAfterDelete2 = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, file2Path));
        var minioExceptionAfterDelete3 = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, file3Path));

        assertEquals(file1.getSize(), responseAfterDelete1.size());
        assertEquals("Object does not exist", minioExceptionAfterDelete2.getMessage());
        assertEquals("Object does not exist", minioExceptionAfterDelete3.getMessage());
    }

    @Test
    @DisplayName("Скачивание файла")
    void downloadResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var response = testHelper.performDownloadResource("hello.txt", cookies);

        assertArrayEquals(file1.getBytes(), response.getContentAsByteArray());
    }

    @Test
    @DisplayName("Скачивание файла в директории")
    void downloadResource_whenFileExistsInDirectory_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file2, "", cookies);

        var response = testHelper.performDownloadResource("folder1/hello.txt", cookies);

        assertArrayEquals(file1.getBytes(), response.getContentAsByteArray());
    }

    @Test
    @DisplayName("Скачивание директории")
    void downloadResource_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUpload3Files(file4, file5, file6, "", cookies);

        var response = testHelper.performDownloadResource("folder1/", cookies);

        assertTrue(testHelper.checkFileInZipResponse(response, file4));
        assertTrue(testHelper.checkFileInZipResponse(response, file5));
        assertTrue(testHelper.checkFileInZipResponse(response, file6));
    }

    @Test
    @DisplayName("Скачивание файла, файл не существует")
    void downloadResource_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDownloadResource("hello.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Скачивание файла, путь не существует")
    void downloadResource_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDownloadResource("folder/pathDoesntExist/", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Создание директории")
    void uploadDirectory_whenPathExists_thenReturnValidResponse() throws Exception {
        var response = testHelper.performUploadDirectory("folder/", cookies);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/");

        assertEquals(0, minioResponse.size());
    }

    @Test
    @DisplayName("Создание директории, директория существует")
    void uploadDirectory_whenDirectoryAlreadyExists_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder/", cookies);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        var response2 = testHelper.performUploadDirectory("folder/", cookies);
        testHelper.checkStatusAndMessage(response2, DIRECTORY_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Создание директории, родительская директория не существует")
    void uploadDirectory_whenParentPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder/folder2/", cookies);
        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Создание директории, путь не существует")
    void uploadDirectory_whenInvalidResourceName_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder", cookies);
        testHelper.checkStatusAndMessage(response, PATH_MUST_BE_END_SLASH, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Получение содержимого директории")
    void getDirectoryContentInfo_whenFilesExists_thenReturnValidResponse() throws Exception {
        testHelper.performUpload3Files(file4, file5, file6, "", cookies);

        var response = testHelper.performGetDirectoryContentsInfo("folder1/", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

    @Test
    @DisplayName("Перемещение файла в другую директорию")
    void moveResource_whenFilesExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadDirectory("folder/", cookies);
        testHelper.performUploadFile(file1, "", cookies);

        StatObjectResponse response = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/");
        assertEquals(0, response.size());

        testHelper.performMoveResource("/hello.txt", "folder/hello.txt", cookies);

        var oldLocationMinioResponse = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/hello.txt"));
        assertEquals("Object does not exist", oldLocationMinioResponse.getMessage());

        var newLocationMinioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/hello.txt");
        assertEquals(file1.getSize(), newLocationMinioResponse.size());
    }


    @Test
    @DisplayName("Перемещение директории в другую директорию (вместе с файлами внутри нее)")
    void moveResource_whenFilesExists_thenReturnValidResponse1() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);
        var oldLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, file5Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());
        assertEquals(file5.getSize(), oldLocationMinioFile5.size());

        testHelper.performUploadDirectory("folder2/", cookies);
        testHelper.performMoveResource("folder1/", "folder2/folder1/", cookies);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/folder1/hello4.txt");
        var newLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/folder1/hello5.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
        assertEquals(file5.getSize(), newLocationMinioFile5.size());
    }

    @Test
    @DisplayName("Переименование  директории в (в директории есть файлы)")
    void moveResource_whenFilesExists_thenReturnValidResponse2() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);
        var oldLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());
        assertEquals(file5.getSize(), oldLocationMinioFile5.size());

        testHelper.performMoveResource("folder1/", "folder2/", cookies);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/hello4.txt");
        var newLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/hello5.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
        assertEquals(file5.getSize(), newLocationMinioFile5.size());
    }

    @Test
    @DisplayName("Переименование файла")
    void moveResource_whenFilesExists_thenReturnValidResponse3() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());

        testHelper.performMoveResource("folder1/hello4.txt", "folder1/newHello4.txt", cookies);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/newHello4.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
    }

    @Test
    @DisplayName("Одновременные переименование и перемещение файла")
    void moveResource_whenRenamingAndRemoving_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());

        MockHttpServletResponse response = testHelper.performMoveResource("folder1/hello4.txt", "folder2/newHello4.txt", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_OPERATION_COMBINATION, HttpStatus.BAD_REQUEST.value());

        var minioException = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/newHello4.txt"));
        assertEquals("Object does not exist", minioException.getMessage());
    }

    @Test
    @DisplayName("Одинаковые старый и новый путь")
    void moveResource_whenSameOldAndNewWays_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());

        MockHttpServletResponse response = testHelper.performMoveResource("folder1/hello4.txt", "folder1/hello4.txt", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_OPERATION_COMBINATION, HttpStatus.BAD_REQUEST.value());

        var fileLocation = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello4.txt");
        assertEquals(file4.getSize(), fileLocation.size());
    }

    @Test
    @DisplayName("Смена типа файла (с File на Directory)")
    void moveResource_whenResourceTypeChange_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());

        MockHttpServletResponse response = testHelper.performMoveResource("folder1/hello4.txt", "folder1/hello4/", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_RESOURCE_TYPE_CHANGE, HttpStatus.BAD_REQUEST.value());

        var fileLocation = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello4.txt");
        assertEquals(file4.getSize(), fileLocation.size());
    }


    private MockHttpServletResponse performAuthorization() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        return testHelper.performAuthorization(USERNAME, PASSWORD, null);
    }

    private void clearMinioBucket() throws Exception {
        Iterable<Result<Item>> iterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket("user-files")
                .recursive(true)
                .build());

        List<DeleteObject> resultList = new ArrayList<>();
        for (Result<Item> itemResult : iterable) {
            resultList.add((new DeleteObject(itemResult.get().objectName())));
        }

        RemoveObjectsArgs removeObjectArgs = RemoveObjectsArgs
                .builder()
                .bucket("user-files")
                .objects(resultList)
                .build();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectArgs);

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            throw new RuntimeException("Ошибка удаления файла " + error.objectName());
        }
    }

}