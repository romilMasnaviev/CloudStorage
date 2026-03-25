package ru.masnaviev.cloudfile.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudfile.util.ResourceType;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoResponse {
    private String path;
    private String name;
    private Long size;
    private ResourceType resourceType;
}