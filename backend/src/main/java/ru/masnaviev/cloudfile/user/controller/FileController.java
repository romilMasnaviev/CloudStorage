package ru.masnaviev.cloudfile.user.controller;

import io.minio.errors.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.user.dto.response.resource.UploadedResource;
import ru.masnaviev.cloudfile.user.service.S3FileService;
import ru.masnaviev.cloudfile.user.service.UserService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.GET_INFO;
import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.UPLOAD;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileController {
    private final S3FileService service;
    private final UserService userService;

    @GetMapping(GET_INFO)
    public ResponseEntity<?> getInfo(
            @RequestParam(name = "path") @NotBlank(message = PATH_MUST_NOT_BE_EMPTY) String path,
            @AuthenticationPrincipal UserDetails userDetails) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse resource = service.getResourceInfo(userId, path);
        return ResponseEntity.ok(resource);
    }

    @PostMapping(UPLOAD)
    ResponseEntity<?> uploadFiles(@RequestParam(name = "path", required = false) String path,
                                  @RequestParam(name = "file") List<MultipartFile> multipartFiles,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<UploadedResource> resources = service.uploadResources(userId, path, multipartFiles);
        return ResponseEntity.ok().body(resources);
    }

}
