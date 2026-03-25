package ru.masnaviev.cloudfile.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudfile.util.ResourceType;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о файле или директории")
public class ResourceInfoResponse {

    @Schema(description = "Путь к ресурсу", example = "documents/")
    private String path;

    @Schema(description = "Имя ресурса", example = "report.pdf")
    private String name;

    @Schema(description = "Размер файла в байтах (null для директорий)", example = "1024")
    private Long size;

    @Schema(description = "Тип ресурса (FILE или DIRECTORY)", example = "FILE")
    private ResourceType resourceType;
}