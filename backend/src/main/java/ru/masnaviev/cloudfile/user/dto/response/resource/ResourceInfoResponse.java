package ru.masnaviev.cloudfile.user.dto.response.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoResponse {
    private String path;
    private String name;
    private Long size;
    private ResourceType resourceType;
}
