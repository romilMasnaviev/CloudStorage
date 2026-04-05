package ru.masnaviev.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.exception.ErrorResponse;
import ru.masnaviev.cloudstorage.model.SecurityUser;

import java.util.List;

import static ru.masnaviev.cloudstorage.constants.ApiPath.*;

@Tag(name = "Файлы и папки", description = "API для управления файлами и директориями")
public interface FileApi {

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
    ResponseEntity<ResourceInfoResponse> getResourceInfo(
            @Parameter(description = "Путь к ресурсу", example = "documents/report.pdf")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(hidden = true) SecurityUser user);

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
    ResponseEntity<Object> deleteResource(
            @Parameter(description = "Путь к ресурсу для удаления", example = "images/photo.png")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(hidden = true) SecurityUser user);

    @Operation(
            summary = "Загрузка файлов",
            description = "Загружает один или несколько файлов в указанную директорию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Файлы успешно загружены",
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
    ResponseEntity<List<ResourceInfoResponse>> uploadResources(
            @Parameter(description = "Путь к директории для загрузки", example = "documents/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(description = "Список файлов для загрузки")
            @RequestPart(name = "file") List<MultipartFile> files,
            @Parameter(hidden = true) SecurityUser user);

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
    ResponseEntity<InputStreamResource> downloadResource(
            @Parameter(description = "Путь к ресурсу для скачивания", example = "documents/report.pdf")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(hidden = true) SecurityUser user);

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
    @PatchMapping(MOVE_RESOURCE)
    ResponseEntity<ResourceInfoResponse> moveResource(
            @Parameter(description = "Текущий путь к ресурсу", example = "old_folder/file.txt")
            @RequestParam(name = "from", defaultValue = "/") @Size(max = 100, message = "Длина старого пути не должна превышать 100 символов.") String pathFrom,
            @Parameter(description = "Новый путь к ресурсу", example = "new_folder/file.txt")
            @RequestParam(name = "to", defaultValue = "/") @Size(max = 100, message = "Длина нового пути не должна превышать 100 символов.") String pathTo,
            @Parameter(hidden = true) SecurityUser user);

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
    ResponseEntity<List<ResourceInfoResponse>> searchResource(
            @Parameter(description = "Поисковый запрос", example = "report")
            @RequestParam(name = "query") @Size(max = 100, message = "Длина запроса не должна превышать 100 символов.") String query,
            @Parameter(hidden = true) SecurityUser user);


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
    ResponseEntity<ResourceInfoResponse> uploadDirectory(
            @Parameter(description = "Путь для новой директории", example = "new_folder/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(hidden = true) SecurityUser user);


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
    ResponseEntity<List<ResourceInfoResponse>> getDirectoryContentsInfo(
            @Parameter(description = "Путь к директории", example = "documents/")
            @RequestParam(name = "path", defaultValue = "/") @Size(max = 100, message = "Длина пути не должна превышать 100 символов.") String path,
            @Parameter(hidden = true) SecurityUser user);
}

