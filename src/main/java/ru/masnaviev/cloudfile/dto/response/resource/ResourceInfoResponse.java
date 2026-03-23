package ru.masnaviev.cloudfile.dto.response.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ru.masnaviev.cloudfile.util.ResourceType;
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoResponse {
    private final String path;
    private final String name;
    private final Long size;
    private final ResourceType resourceType;
}
