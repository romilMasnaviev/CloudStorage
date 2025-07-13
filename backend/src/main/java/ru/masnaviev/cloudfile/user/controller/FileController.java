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
import ru.masnaviev.cloudfile.user.dto.response.storage.UploadedFile;
import ru.masnaviev.cloudfile.user.service.S3FileService;
import ru.masnaviev.cloudfile.user.service.UserService;

import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.UPLOAD;

@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileController {
    private final S3FileService service;
    private final UserService userService;

    private final

    @PostMapping(value = UPLOAD)
    ResponseEntity<?> uploadFiles(@RequestParam(name = "path", required = false) String path,
                                  @RequestParam(name = "file") List<MultipartFile> multipartFiles,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<UploadedFile> files = service.uploadFiles(path, multipartFiles, userId);
        return ResponseEntity.ok().body(files);
    }

}
