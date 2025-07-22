package ru.masnaviev.cloudfile.user.dto.response.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoResponse {
    private String path;
    private String name;
    private Long size;
    private ResourceType resourceType;
}
