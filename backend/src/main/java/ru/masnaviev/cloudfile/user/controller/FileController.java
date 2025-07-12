package ru.masnaviev.cloudfile.user.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.service.S3FileService;

import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.UPLOAD;

@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileController {
    private final S3FileService service;

    @PostMapping(value = UPLOAD)
    ResponseEntity<?> uploadFiles(@RequestParam(name = "path", required = false, defaultValue = "/") String path,
                                  @RequestParam(name = "file") List<MultipartFile> multipartFiles,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        service.uploadFiles(path, multipartFiles, userDetails.getUsername());
        return null;
    }

}
