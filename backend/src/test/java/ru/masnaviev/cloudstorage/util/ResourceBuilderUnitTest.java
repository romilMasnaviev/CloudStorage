package ru.masnaviev.cloudstorage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.USERID_MUST_NOT_BE_LESS_0;
import static ru.masnaviev.cloudstorage.util.ResourceBuilder.createFrom;


class ResourceBuilderUnitTest {

    @Test
    @DisplayName("Создание ресурса c пустым путем")
    void createFrom_whenPathEmpty_thenThrowIllegalArgumentException() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> createFrom(1L, ""));
        assertEquals(PATH_MUST_NOT_BE_EMPTY, ex.getMessage());
    }

    @Test
    @DisplayName("Создание ресурса с отрицательным userId")
    void createFrom_whenUserIdLess1Empty_thenThrowIllegalArgumentException() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> createFrom(-1L, "path"));
        assertEquals(USERID_MUST_NOT_BE_LESS_0, ex.getMessage());
    }

    @Test
    @DisplayName("Создание ресурса, указывающего на корневую папку")
    void createFrom_whenFilenameIsSlash_thenReturnValidResource() {
        Resource resource = createFrom(1L, "/");

        assertEquals(ResourceType.DIRECTORY, resource.getResourceType());
        assertEquals("", resource.getResourceName());
        assertTrue(resource.getPathsList().isEmpty());
        assertEquals("user-1-files/", resource.getFullPath());
        assertEquals("user-1-files/", resource.getPath());
        assertEquals("/", resource.getPathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из одного файла")
    void createFrom_whenValidData_thenReturnValidResource() {
        Resource resource = createFrom(1L, "test.txt");

        assertEquals(ResourceType.FILE, resource.getResourceType());
        assertEquals("test.txt", resource.getResourceName());
        assertTrue(resource.getPathsList().isEmpty());
        assertEquals("user-1-files/test.txt", resource.getFullPath());
        assertEquals("user-1-files/", resource.getPath());
        assertEquals("/", resource.getPathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из директории и файла")
    void createFrom_whenValidData_thenReturnValidResource1() {
        Resource resource = createFrom(1L, "folder1/test.txt");

        assertEquals(ResourceType.FILE, resource.getResourceType());
        assertEquals("test.txt", resource.getResourceName());
        assertEquals("user-1-files/folder1/", resource.getPathsList().getFirst());
        assertEquals("user-1-files/folder1/test.txt", resource.getFullPath());
        assertEquals("user-1-files/folder1/", resource.getPath());
        assertEquals("folder1/", resource.getPathWithoutUserFolder());
    }

    @Test
    @DisplayName("Создание ресурса из двух директорий")
    void createFrom_whenValidData_thenReturnValidResource2() {
        Resource resource = createFrom(1L, "folder1/folder2/");

        assertEquals(ResourceType.DIRECTORY, resource.getResourceType());
        assertEquals("folder2", resource.getResourceName());
        assertEquals("user-1-files/folder1/", resource.getPathsList().getFirst());
        assertEquals("user-1-files/folder1/folder2/", resource.getFullPath());
        assertEquals("user-1-files/folder1/", resource.getPath());
        assertEquals("folder1/", resource.getPathWithoutUserFolder());
    }
}