package ru.masnaviev.cloudfile.user.dto.response.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;

@AllArgsConstructor
@Getter
public class DownloadResourceResponse {
    String resourceName;
    InputStreamResource resource;
}
