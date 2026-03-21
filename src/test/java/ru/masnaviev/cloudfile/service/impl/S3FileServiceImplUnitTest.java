package ru.masnaviev.cloudfile.service.impl;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.masnaviev.cloudfile.exception.resource.PathNotFoundException;
import ru.masnaviev.cloudfile.exception.resource.ResourceNotFoundException;
import ru.masnaviev.cloudfile.repository.MinioRepository;
import ru.masnaviev.cloudfile.util.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.util.ResourceBuilder.createFrom;

@ExtendWith(MockitoExtension.class)
public class S3FileServiceImplUnitTest {

    @Mock
    MinioRepository minioRepository;

    @InjectMocks
    S3FileServiceImpl service;

    @Test
    public void getResourceInfo_whenResourceExist_thenReturnValidResponse() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(true);
        when(minioRepository.getResourceInfo(any())).thenReturn(mock(StatObjectResponse.class));

        service.getResourceInfo(1L, "test.txt");

        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(1)).getResourceInfo(resource.getFullPath());
    }

    @Test
    public void getResourceInfo_whenPathDoesntExists_thenReturnPathNotFoundException() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(false);


        PathNotFoundException pathNotFoundException = assertThrows(PathNotFoundException.class, () -> service.getResourceInfo(1L, "test.txt"));

        assertEquals(PATH_NOT_FOUND, pathNotFoundException.getMessage());
        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(0)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(0)).getResourceInfo(any());
    }

    @Test
    public void getResourceInfo_whenFileDoesntExists_thenReturnResourceNotFoundException() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> service.getResourceInfo(1L, "test.txt"));

        assertEquals(FILE_NOT_FOUND, resourceNotFoundException.getMessage());
        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(0)).getResourceInfo(any());
    }

    @Test
    public void getResourceInfo_whenDirectoryDoesntExists_thenReturnResourceNotFoundException() {
        Resource resource = createFrom(1L, "folder/");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> service.getResourceInfo(1L, "folder/"));

        assertEquals(DIRECTORY_NOT_FOUND, resourceNotFoundException.getMessage());
        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(0)).getResourceInfo(any());
    }


    @Test
    public void deleteResource_whenResourceExist_thenReturnValidResponse() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(true);
        doNothing().when(minioRepository).deleteResource(resource.getFullPath());

        service.deleteResource(1L, "test.txt");

        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(1)).deleteResource(resource.getFullPath());
    }


    @Test
    public void deleteResource_whenPathDoesntExists_thenReturnPathNotFoundException() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(false);

        PathNotFoundException pathNotFoundException = assertThrows(PathNotFoundException.class, () -> service.deleteResource(1L, "test.txt"));

        assertEquals(PATH_NOT_FOUND, pathNotFoundException.getMessage());
        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(0)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(0)).deleteResource(resource.getFullPath());
    }

    @Test
    public void deleteResource_whenResourceDoesntExists_thenReturnResourceNotFoundException() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(false);

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> service.deleteResource(1L, "test.txt"));

        assertEquals(FILE_NOT_FOUND, resourceNotFoundException.getMessage());
        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(0)).deleteResource(resource.getFullPath());
    }

    @Test
    public void downloadResource_whenFileExists_thenReturnValidResponse() {
        Resource resource = createFrom(1L, "test.txt");

        when(minioRepository.checkResourceExists(resource.getPathWithoutResourceName())).thenReturn(true);
        when(minioRepository.checkResourceExists(resource.getFullPath())).thenReturn(true);
        when(minioRepository.downloadResource(resource.getFullPath())).thenReturn(mock(GetObjectResponse.class));

        service.downloadResource(1L, "test.txt");

        verify(minioRepository, times(1)).checkResourceExists(resource.getPathWithoutResourceName());
        verify(minioRepository, times(1)).checkResourceExists(resource.getFullPath());
        verify(minioRepository, times(1)).downloadResource(resource.getFullPath());
    }

    //TODO дописать тесты на остальные методы
}