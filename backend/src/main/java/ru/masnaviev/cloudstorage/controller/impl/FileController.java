package ru.masnaviev.cloudstorage.controller.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.controller.FileApi;
import ru.masnaviev.cloudstorage.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.model.SecurityUser;
import ru.masnaviev.cloudstorage.service.S3FileService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileController implements FileApi {
    private final S3FileService service;

    @Override
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(
            String path,
            @AuthenticationPrincipal SecurityUser user) {
        ResourceInfoResponse resource = service.getResourceInfo(user.getUserId(), path);
        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<Object> deleteResource(
            String path,
            @AuthenticationPrincipal SecurityUser user) {
        service.deleteResource(user.getUserId(), path);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ResourceInfoResponse>> uploadResources(
            String path,
            List<MultipartFile> files,
            @AuthenticationPrincipal SecurityUser user) {
        List<ResourceInfoResponse> response = service.uploadResources(user.getUserId(), path, files);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<InputStreamResource> downloadResource(
            String path,
            @AuthenticationPrincipal SecurityUser user) {

        DownloadResourceResponse response = service.downloadResource(user.getUserId(), path);
        HttpHeaders headers = new HttpHeaders();
        String postfix = response.getType() == DIRECTORY ? ".zip" : "";
        headers.add(CONTENT_DISPOSITION, "attachment;filename*=utf-8''"
                + encodeFileName(response.getResourceName()) + postfix);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.getResource());
    }

    @Override
    public ResponseEntity<ResourceInfoResponse> moveResource(
            String pathFrom,
            String pathTo,
            @AuthenticationPrincipal SecurityUser user) {
        ResourceInfoResponse resource = service.moveResource(user.getUserId(), pathFrom, pathTo);
        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<List<ResourceInfoResponse>> searchResource(
            String query,
            @AuthenticationPrincipal SecurityUser user) {
        List<ResourceInfoResponse> resources = service.searchResource(user.getUserId(), query);
        return ResponseEntity.ok(resources);
    }

    @Override
    public ResponseEntity<ResourceInfoResponse> uploadDirectory(
            String path,
            @AuthenticationPrincipal SecurityUser user) {
        ResourceInfoResponse response = service.uploadDirectory(user.getUserId(), path);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<List<ResourceInfoResponse>> getDirectoryContentsInfo(
            String path,
            @AuthenticationPrincipal SecurityUser user) {
        List<ResourceInfoResponse> response = service.getDirectoryContentsInfo(user.getUserId(), path);
        return ResponseEntity.ok().body(response);
    }

    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}