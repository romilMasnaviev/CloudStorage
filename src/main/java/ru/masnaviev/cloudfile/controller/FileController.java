package ru.masnaviev.cloudfile.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.service.S3FileService;
import ru.masnaviev.cloudfile.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static ru.masnaviev.cloudfile.constatnts.ApiPath.*;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;
import static ru.masnaviev.cloudfile.util.NormalizedResourceData.ResourceType.DIRECTORY;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileController {
    private final S3FileService service;
    private final UserService userService;

    @GetMapping(GET_RESOURCE_INFO)
    public ResponseEntity<?> getResourceInfo(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse resource = service.getResourceInfo(userId, path);
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping(DELETE_RESOURCE)
    //Если путь указан как "/", удалит все содержимое папки
    public ResponseEntity<?> deleteResource(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        service.deleteResource(userId, path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(DOWNLOAD_RESOURCE)
    public ResponseEntity<?> downloadResource(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());

        DownloadResourceResponse response = service.downloadResource(userId, path);
        HttpHeaders headers = new HttpHeaders();
        String postfix = response.getResourceType() == DIRECTORY ? ".zip" : "";
        headers.add(CONTENT_DISPOSITION, "attachment;filename*=utf-8''"
                + encodeFileName(response.getResourceName()) + postfix);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.getResource());
    }

    @PostMapping(UPLOAD_DIRECTORY)
    // Путь указывается со слэшем в конце "/"
    public ResponseEntity<?> uploadDirectory(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse response = service.uploadDirectory(userId, path);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(GET_DIRECTORY_CONTENTS_INFO)
    public ResponseEntity<?> getDirectoryContentsInfo(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<ResourceInfoResponse> response = service.getDirectoryContentsInfo(userId, path);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(path = UPLOAD_RESOURCE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResources(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @RequestPart(name = "file") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<ResourceInfoResponse> response = service.uploadResources(userId, path, files);
        return ResponseEntity.ok().body(response);
    }

    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }

}
