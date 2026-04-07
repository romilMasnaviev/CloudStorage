package ru.masnaviev.cloudstorage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.masnaviev.cloudstorage.model.Resource;
import ru.masnaviev.cloudstorage.model.ResourceType;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.USERID_MUST_NOT_BE_LESS_0;
import static ru.masnaviev.cloudstorage.model.ResourceFactory.createFromUserInput;


class ResourceFactoryUnitTest {

    @Test
    @DisplayName("Создание ресурса c пустым путем")
    void createFrom_UserInput_whenPathEmpty_thenThrowIllegalArgumentException() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> createFromUserInput(1L, ""));
        assertEquals(PATH_MUST_NOT_BE_EMPTY, ex.getMessage());
    }

    @Test
    @DisplayName("Создание ресурса с отрицательным userId")
    void createFrom_UserInput_whenUserIdLess1Empty_thenThrowIllegalArgumentException() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> createFromUserInput(-1L, "path"));
        assertEquals(USERID_MUST_NOT_BE_LESS_0, ex.getMessage());
    }

    @Test
    @DisplayName("Создание ресурса, указывающего на корневую папку")
    void createFrom_UserInput_whenFilenameIsSlash_thenReturnValidResource() {
        Resource resource = createFromUserInput(1L, "/");

        assertEquals(ResourceType.DIRECTORY, resource.resourceType());
        assertEquals("", resource.resourceName());
        assertTrue(resource.getPathsList().isEmpty());
        assertEquals("user-1-files/", resource.fullPath());
        assertEquals("user-1-files/", resource.path());
        assertEquals("/", resource.pathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из одного файла")
    void createFrom_UserInput_whenValidData_thenReturnValidResource() {
        Resource resource = createFromUserInput(1L, "test.txt");

        assertEquals(ResourceType.FILE, resource.resourceType());
        assertEquals("test.txt", resource.resourceName());
        assertTrue(resource.getPathsList().isEmpty());
        assertEquals("user-1-files/test.txt", resource.fullPath());
        assertEquals("user-1-files/", resource.path());
        assertEquals("/", resource.pathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из директории и файла")
    void createFrom_UserInput_whenValidData_thenReturnValidResource1() {
        Resource resource = createFromUserInput(1L, "folder1/test.txt");

        assertEquals(ResourceType.FILE, resource.resourceType());
        assertEquals("test.txt", resource.resourceName());
        assertEquals("user-1-files/folder1/", resource.getPathsList().getFirst());
        assertEquals("user-1-files/folder1/test.txt", resource.fullPath());
        assertEquals("user-1-files/folder1/", resource.path());
        assertEquals("folder1/", resource.pathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из двух директорий")
    void createFrom_UserInput_whenValidData_thenReturnValidResource2() {
        Resource resource = createFromUserInput(1L, "folder1/folder2/");

        assertEquals(ResourceType.DIRECTORY, resource.resourceType());
        assertEquals("folder2", resource.resourceName());
        assertEquals("user-1-files/folder1/", resource.getPathsList().getFirst());
        assertEquals("user-1-files/folder1/folder2/", resource.fullPath());
        assertEquals("user-1-files/folder1/", resource.path());
        assertEquals("folder1/", resource.pathWithoutUserFolder());
    }
}