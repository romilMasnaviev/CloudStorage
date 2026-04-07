package ru.masnaviev.cloudstorage.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.masnaviev.cloudstorage.model.ResourceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о файле или директории")
public record ResourceInfoResponse(
        @Schema(description = "Путь к ресурсу", example = "documents/")
        String path,
        @Schema(description = "Имя ресурса", example = "report.pdf")
        String name,
        @Schema(description = "Размер файла в байтах (null для директорий)", example = "1024")
        Long size,
        @Schema(description = "Тип ресурса (FILE или DIRECTORY)", example = "FILE")
        ResourceType type
) {
}