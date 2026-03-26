package ru.masnaviev.cloudstorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.masnaviev.cloudstorage.AbstractIntegrationTest;
import ru.masnaviev.cloudstorage.MockMvcTestHelper;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudstorage.TestData.*;
import static ru.masnaviev.cloudstorage.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudstorage.util.ResourceType.FILE;

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

    ObjectMapper mapper = new ObjectMapper();

    private Cookie[] cookies;

    @BeforeEach
    void clearDb() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
        clearMinioBucket();
        cookies = performAuthorization().getCookies();
    }

    @Test
    @DisplayName("Загрузка одного файла: файл загружается и возвращается информация о нем")
    void uploadResources_whenOneFile_thenReturnValidResponse() throws Exception {
        var servletResponse = testHelper.performUploadFile(file1, "/", cookies);
        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        assertEquals(file1.getSize(), minioResponse.size());

        String expectedJson = mapper.writeValueAsString(List.of(file1ExpectedResponse));
        String actualJson = servletResponse.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Загрузка нескольких файлов: файлы с вложенными путями успешно загружаются")
    void uploadResources_whenMultipleFiles_thenReturnValidResponse() throws Exception {
        var servletResponse = testHelper.performUpload3Files(file1, file2, file3, "", cookies);

        var minioResponse1 = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        var minioResponse2 = testHelper.performGetStatObjectFromMinio(minioClient, file2Path);
        var minioResponse3 = testHelper.performGetStatObjectFromMinio(minioClient, file3Path);

        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());

        assertEquals(file1.getSize(), minioResponse1.size());
        assertEquals(file2.getSize(), minioResponse2.size());
        assertEquals(file3.getSize(), minioResponse3.size());

        String expectedJson = mapper.writeValueAsString(List.of(
                file1ExpectedResponse, file2ExpectedResponse, file3ExpectedResponse, folder1, folder1folder2));
        String actualJson = servletResponse.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Загрузка файла: если файл уже существует, возвращается ошибка 409 Conflict")
    void uploadResources_whenFileAlreadyExists_thenReturnException() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var servletResponse = testHelper.performUploadFile(file1, "", cookies);

        testHelper.checkStatusAndMessage(servletResponse, FILE_ALREADY_EXIST, HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Получение информации о файле: если файл существует, возвращается его метадата")
    void getResourceInfo_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var servletResponse = testHelper.performGetResourceInfo("hello.txt", cookies);

        String expectedJson = mapper.writeValueAsString(file1ExpectedResponse);
        String actualJson = servletResponse.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Получение информации о директории: если директория существует, возвращается её метадата")
    void getResourceInfo_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file2, "", cookies);
        var expectedResponse = ResourceInfoResponseBuilder.createResponseFrom("", "folder1", null, DIRECTORY);
        var servletResponse = testHelper.performGetResourceInfo("folder1/", cookies);

        String expectedJson = mapper.writeValueAsString(expectedResponse);
        String actualJson = servletResponse.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Получение информации о файле: если файл не существует, возвращается 404 Not Found")
    void getResourceInfo_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("test.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Получение информации о директории: если директория не существует, возвращается 404 Not Found")
    void getResourceInfo_whenDirectoryDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("folder1/", cookies);

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Получение информации о ресурсе: если путь не существует, возвращается 400 Bad Request")
    void getResourceInfo_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performGetResourceInfo("folder/pathDoesntExist/", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Удаление ресурса: попытка удалить родительскую директорию возвращает 405 Method Not Allowed")
    void deleteResource_whenTryToDeleteParentDirectory_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("", cookies);

        testHelper.checkStatusAndMessage(response, PROTECTED_PARENT_DIRECTORY, HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @Test
    @DisplayName("Удаление файла: если файл не существует, возвращается 404 Not Found")
    void deleteResource_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("test.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление директории: если директория не существует, возвращается 404 Not Found")
    void deleteResource_whenDirectoryDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("folder1/", cookies);

        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление директории: если некорректный путь, возвращается 400 Bad Request")
    void deleteResource_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDeleteResource("folder1/someFile", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Удаление файла: существующий файл успешно удаляется")
    void deleteResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);
        var minioResponseBeforeDelete = testHelper.performGetStatObjectFromMinio(minioClient, file1Path);
        assertEquals(file1.getSize(), minioResponseBeforeDelete.size());

        var responseAfterDelete = testHelper.performDeleteResource("hello.txt", cookies);

        assertEquals(HttpStatus.NO_CONTENT.value(), responseAfterDelete.getStatus());

        assertTrue(minioCheckObjectDoesntExist(file1Path));
    }

    @Test
    @DisplayName("Удаление директории: директория и все вложенные файлы успешно удаляются")
    void deleteResource_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        var responseBeforeDelete4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);
        var responseBeforeDelete5 = testHelper.performGetStatObjectFromMinio(minioClient, file5Path);

        assertEquals(file4.getSize(), responseBeforeDelete4.size());
        assertEquals(file5.getSize(), responseBeforeDelete5.size());

        var responseAfterDelete = testHelper.performDeleteResource("folder1/", cookies);
        assertEquals(HttpStatus.NO_CONTENT.value(), responseAfterDelete.getStatus());

        assertTrue(minioCheckObjectDoesntExist(file4Path));
        assertTrue(minioCheckObjectDoesntExist(file5Path));
    }

    @Test
    @DisplayName("Скачивание файла: существующий файл успешно скачивается")
    void downloadResource_whenFileExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var response = testHelper.performDownloadResource("hello.txt", cookies);

        assertArrayEquals(file1.getBytes(), response.getContentAsByteArray());
    }

    @Test
    @DisplayName("Скачивание файла: файл внутри директории успешно скачивается")
    void downloadResource_whenFileExistsInDirectory_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file2, "", cookies);

        var response = testHelper.performDownloadResource("folder1/hello.txt", cookies);

        assertArrayEquals(file2.getBytes(), response.getContentAsByteArray());
    }

    @Test
    @DisplayName("Скачивание директории: директория успешно скачивается в виде архива")
    void downloadResource_whenDirectoryExists_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        var response = testHelper.performDownloadResource("folder1/", cookies);

        assertTrue(testHelper.checkFileInZipResponse(response, file4));
        assertTrue(testHelper.checkFileInZipResponse(response, file5));
    }

    @Test
    @DisplayName("Скачивание файла: попытка скачать несуществующий файл возвращает 404 Not Found")
    void downloadResource_whenFileDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDownloadResource("hello.txt", cookies);

        testHelper.checkStatusAndMessage(response, FILE_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Скачивание файла: попытка скачать по некорректному пути возвращает 400 Bad Request")
    void downloadResource_whenPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performDownloadResource("folder/pathDoesntExist/", cookies);

        testHelper.checkStatusAndMessage(response, PATH_NOT_FOUND, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Создание директории: новая директория успешно создается")
    void uploadDirectory_whenPathExists_thenReturnValidResponse() throws Exception {
        var response = testHelper.performUploadDirectory("folder/", cookies);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        var minioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/");

        String actualJson = response.getContentAsString();
        String expectedJson = mapper.writeValueAsString(ResourceInfoResponseBuilder.createResponseFrom("", "folder", null, DIRECTORY));

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
        assertEquals(0, minioResponse.size());
    }

    @Test
    @DisplayName("Создание директории: попытка создать существующую директорию возвращает 409 Conflict")
    void uploadDirectory_whenDirectoryAlreadyExists_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder/", cookies);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        var response2 = testHelper.performUploadDirectory("folder/", cookies);
        testHelper.checkStatusAndMessage(response2, DIRECTORY_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Создание директории: попытка создать директорию в несуществующей папке возвращает 404 Not Found")
    void uploadDirectory_whenParentPathDoesntExist_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder/folder2/", cookies);
        testHelper.checkStatusAndMessage(response, DIRECTORY_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Создание директории: отсутствие слэша на конце пути возвращает 400 Bad Request")
    void uploadDirectory_whenPathDoesNotEndWithSlash_thenReturnException() throws Exception {
        var response = testHelper.performUploadDirectory("folder", cookies);
        testHelper.checkStatusAndMessage(response, PATH_MUST_BE_END_SLASH, HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Получение содержимого директории: возвращает список всех файлов в папке")
    void getDirectoryContentsInfo_whenDirectoryContainsFiles_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);
        testHelper.performUploadDirectory("folder1/", cookies);
        testHelper.performUploadDirectory("folder1/folder2/", cookies);

        var response = testHelper.performGetDirectoryContentsInfo("folder1/", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String actualJson = response.getContentAsString(StandardCharsets.UTF_8);
        String expectedJson = mapper.writeValueAsString(List.of(
                file4ExpectedResponse, file5ExpectedResponse, folder1folder2));

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Получение содержимого директории: возвращает список всех файлов в родительской папке")
    void getDirectoryContentsInfo_whenDirectoryParentContainsFiles_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file6, "", cookies);
        testHelper.performUploadFile(file7, "", cookies);
        testHelper.performUploadDirectory("folder1/", cookies);

        var response = testHelper.performGetDirectoryContentsInfo("/", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String actualJson = response.getContentAsString(StandardCharsets.UTF_8);
        String expectedJson = mapper.writeValueAsString(List.of(
                file6ExpectedResponse, file7ExpectedResponse, folder1ExpectedResponse));

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Перемещение ресурса: файл успешно перемещается в другую директорию")
    void moveResource_whenFileMoved_thenReturnValidResponse() throws Exception {
        testHelper.performUploadDirectory("folder/", cookies);
        testHelper.performUploadFile(file1, "", cookies);

        testHelper.performMoveResource("/hello.txt", "folder/hello.txt", cookies);

        assertTrue(minioCheckObjectDoesntExist("user-1-files/hello.txt"));

        var newLocationMinioResponse = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder/hello.txt");
        assertEquals(file1.getSize(), newLocationMinioResponse.size());
    }

    @Test
    @DisplayName("Перемещение ресурса: директория со всеми файлами успешно перемещается в другую папку")
    void moveResource_whenDirectoryMoved_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);
        testHelper.performUploadDirectory("folder2/", cookies);

        testHelper.performGetStatObjectFromMinio(minioClient, file4Path);
        testHelper.performGetStatObjectFromMinio(minioClient, file5Path);

        var response = testHelper.performMoveResource("folder1/", "folder2/folder1/", cookies);

        String actualResponse = response.getContentAsString(StandardCharsets.UTF_8);
        String expectedResponse = mapper.writeValueAsString(ResourceInfoResponseBuilder.createResponseFrom("folder2/", "folder1", null, DIRECTORY));

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/folder1/hello4.txt");
        var newLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/folder1/hello5.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
        assertEquals(file5.getSize(), newLocationMinioFile5.size());
    }

    @Test
    @DisplayName("Переименование ресурса: директория с файлами успешно переименовывается")
    void moveResource_whenDirectoryRenamed_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        testHelper.performGetStatObjectFromMinio(minioClient, file4Path);
        testHelper.performGetStatObjectFromMinio(minioClient, file5Path);

        var response = testHelper.performMoveResource("folder1/", "folder2/", cookies);

        String actualResponse = response.getContentAsString(StandardCharsets.UTF_8);
        String expectedResponse = mapper.writeValueAsString(ResourceInfoResponseBuilder.createResponseFrom("", "folder2", null, DIRECTORY));

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/hello4.txt");
        var newLocationMinioFile5 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder2/hello5.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
        assertEquals(file5.getSize(), newLocationMinioFile5.size());
    }

    @Test
    @DisplayName("Переименование ресурса: файл успешно переименовывается")
    void moveResource_whenFileRenamed_thenReturnValidResponse() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        var response = testHelper.performMoveResource("folder1/hello4.txt", "folder1/newHello4.txt", cookies);

        String actualResponse = response.getContentAsString(StandardCharsets.UTF_8);
        String expectedResponse = mapper.writeValueAsString(ResourceInfoResponseBuilder.createResponseFrom("folder1/", "newHello4.txt", file4.getSize(), FILE));

        JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);

        var newLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/newHello4.txt");

        assertEquals(file4.getSize(), newLocationMinioFile4.size());
    }

    @Test
    @DisplayName("Перемещение ресурса: попытка одновременного переименования и перемещения возвращает 400 Bad Request")
    void moveResource_whenRenamingAndMovingSimultaneously_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        var response = testHelper.performMoveResource("folder1/hello4.txt", "folder2/newHello4.txt", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_OPERATION_COMBINATION, HttpStatus.BAD_REQUEST.value());

        assertTrue(minioCheckObjectDoesntExist("user-1-files/folder1/newHello4.txt"));
    }

    @Test
    @DisplayName("Перемещение ресурса: если исходный и целевой путь одинаковы, возвращается 400 Bad Request")
    void moveResource_whenPathsAreSame_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        var response = testHelper.performMoveResource("folder1/hello4.txt", "folder1/hello4.txt", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_OPERATION_COMBINATION, HttpStatus.BAD_REQUEST.value());

        testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello4.txt");
    }

    @Test
    @DisplayName("Перемещение ресурса: попытка сменить тип (с файла на папку) возвращает 400 Bad Request")
    void moveResource_whenResourceTypeChanges_thenReturnException() throws Exception {
        testHelper.performUploadFile(file4, "", cookies);

        var oldLocationMinioFile4 = testHelper.performGetStatObjectFromMinio(minioClient, file4Path);

        assertEquals(file4.getSize(), oldLocationMinioFile4.size());

        MockHttpServletResponse response = testHelper.performMoveResource("folder1/hello4.txt", "folder1/hello4/", cookies);

        testHelper.checkStatusAndMessage(response, INVALID_RESOURCE_TYPE_CHANGE, HttpStatus.BAD_REQUEST.value());

        var fileLocation = testHelper.performGetStatObjectFromMinio(minioClient, "user-1-files/folder1/hello4.txt");
        assertEquals(file4.getSize(), fileLocation.size());
    }

    @Test
    @DisplayName("Поиск ресурсов: при наличии совпадений возвращается список подходящих файлов")
    void searchResource_whenMultipleMatches_thenValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);
        testHelper.performUploadFile(file4, "", cookies);
        testHelper.performUploadFile(file5, "", cookies);

        var response = testHelper.performSearchResource("hello", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String expectedJson = mapper.writeValueAsString(List.of(
                file1ExpectedResponse, file4ExpectedResponse, file5ExpectedResponse));
        String actualJson = response.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Поиск ресурсов: если найдено ровно одно совпадение, возвращается один файл")
    void searchResource_whenOneMatch_thenValidResponse() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);
        testHelper.performUploadFile(file4, "", cookies);

        var response = testHelper.performSearchResource("hello4", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String expectedJson = mapper.writeValueAsString(List.of(file4ExpectedResponse));
        String actualJson = response.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Поиск ресурсов: если совпадений нет, возвращается пустой список")
    void searchResource_whenNoMatches_thenEmptyList() throws Exception {
        testHelper.performUploadFile(file1, "", cookies);

        var response = testHelper.performSearchResource("unExisted", cookies);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String expectedJson = mapper.writeValueAsString(List.of());
        String actualJson = response.getContentAsString(StandardCharsets.UTF_8);

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    private MockHttpServletResponse performAuthorization() throws Exception {
        testHelper.performRegistration(USERNAME, PASSWORD, null);
        return testHelper.performAuthorization(USERNAME, PASSWORD, null);
    }

    private boolean minioCheckObjectDoesntExist(String filePath) {
        var minioException = assertThrows(MinioException.class, () -> testHelper.performGetStatObjectFromMinio(minioClient, filePath));
        return "Object does not exist".equals(minioException.getMessage());
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