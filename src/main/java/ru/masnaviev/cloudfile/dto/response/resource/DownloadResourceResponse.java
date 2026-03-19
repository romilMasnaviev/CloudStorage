package ru.masnaviev.cloudfile.dto.response.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;
import ru.masnaviev.cloudfile.util.ResourceType;

@AllArgsConstructor
@Getter
public class DownloadResourceResponse {
    String resourceName;
    InputStreamResource resource;
    ResourceType resourceType;
}
