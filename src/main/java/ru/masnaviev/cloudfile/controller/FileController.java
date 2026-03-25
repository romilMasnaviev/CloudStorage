package ru.masnaviev.cloudfile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
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
import ru.masnaviev.cloudfile.exception.ErrorResponse;
import ru.masnaviev.cloudfile.service.S3FileService;
import ru.masnaviev.cloudfile.service.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static ru.masnaviev.cloudfile.constatnts.ApiPath.*;
import static ru.masnaviev.cloudfile.util.ResourceType.DIRECTORY;

@RestController
@RequestMapping
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Tag(name = "Файлы и папки", description = "API для управления файлами и директориями")
class FileController {
    private final S3FileService service;
    private final UserService userService;

    @Operation(
            summary = "Получение информации о ресурсе",
            description = "Возвращает метаданные файла или директории по указанному пути"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о ресурсе успешно получена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(GET_RESOURCE_INFO)
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(
            @Parameter(description = "Путь к ресурсу", example = "documents/report.pdf")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse resource = service.getResourceInfo(userId, path);
        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Удаление ресурса",
            description = "Удаляет файл или директорию по указанному пути"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ресурс успешно удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(DELETE_RESOURCE)
    public ResponseEntity<Object> deleteResource(
            @Parameter(description = "Путь к ресурсу для удаления", example = "images/photo.png")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        service.deleteResource(userId, path);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Загрузка файлов",
            description = "Загружает один или несколько файлов в указанную директорию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файлы успешно загружены",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Ошибка загрузки (например, файл не выбран)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Итоговый размер файла очень большой",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = UPLOAD_RESOURCE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceInfoResponse>> uploadResources(
            @Parameter(description = "Путь к директории для загрузки", example = "documents/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(description = "Список файлов для загрузки")
            @RequestPart(name = "file") List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<ResourceInfoResponse> response = service.uploadResources(userId, path, files);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "Скачивание ресурса",
            description = "Скачивает файл или директорию (в виде zip-архива) по указанному пути"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл/архив успешно скачан"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(DOWNLOAD_RESOURCE)
    public ResponseEntity<InputStreamResource> downloadResource(
            @Parameter(description = "Путь к ресурсу для скачивания", example = "documents/report.pdf")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

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

    @Operation(
            summary = "Перемещение/Переименование ресурса",
            description = "Перемещает или переименовывает файл или директорию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ресурс успешно перемещен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Исходный ресурс не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(MOVE_RESOURCE)
    public ResponseEntity<ResourceInfoResponse> moveResource(
            @Parameter(description = "Текущий путь к ресурсу", example = "old_folder/file.txt")
            @RequestParam(name = "from", defaultValue = "/") @Size(max = 100, message = "The pathFrom must not exceed 100 characters.") String pathFrom,
            @Parameter(description = "Новый путь к ресурсу", example = "new_folder/file.txt")
            @RequestParam(name = "to", defaultValue = "/") @Size(max = 100, message = "The pathTo must not exceed 100 characters.") String pathTo,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse resource = service.moveResource(userId, pathFrom, pathTo);
        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Поиск ресурсов",
            description = "Поиск файлов и директорий по имени или частичному совпадению"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(FIND_RESOURCE)
    public ResponseEntity<List<ResourceInfoResponse>> searchResource(
            @Parameter(description = "Поисковый запрос", example = "report")
            @RequestParam(name = "query") @Size(max = 100, message = "The query must not exceed 20 characters.") String query,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<ResourceInfoResponse> resources = service.searchResource(userId, query);
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Создание директории",
            description = "Создает новую пустую директорию по указанному пути"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Директория успешно создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Директория с таким именем уже существует",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(UPLOAD_DIRECTORY)
    public ResponseEntity<ResourceInfoResponse> uploadDirectory(
            @Parameter(description = "Путь для новой директории", example = "new_folder/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        ResourceInfoResponse response = service.uploadDirectory(userId, path);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(
            summary = "Получение содержимого директории",
            description = "Возвращает список файлов и поддиректорий внутри указанной директории"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Содержимое директории успешно получено",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Директория не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(GET_DIRECTORY_CONTENTS_INFO)
    public ResponseEntity<List<ResourceInfoResponse>> getDirectoryContentsInfo(
            @Parameter(description = "Путь к директории", example = "documents/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "The path must not exceed 100 characters.") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getIdByUsername(userDetails.getUsername());
        List<ResourceInfoResponse> response = service.getDirectoryContentsInfo(userId, path);
        return ResponseEntity.ok().body(response);
    }

    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}