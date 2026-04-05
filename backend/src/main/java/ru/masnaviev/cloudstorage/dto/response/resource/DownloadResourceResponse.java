package ru.masnaviev.cloudstorage.dto.response.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;
import ru.masnaviev.cloudstorage.util.ResourceType;


@AllArgsConstructor
@Getter
public class DownloadResourceResponse {
    String resourceName;
    InputStreamResource resource;
    ResourceType type;
}
