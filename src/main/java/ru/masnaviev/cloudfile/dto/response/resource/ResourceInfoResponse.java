package ru.masnaviev.cloudfile.dto.response.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.masnaviev.cloudfile.util.NormalizedResourceData;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceInfoResponse {
    private String path;
    private String name;
    private Long size;
    private NormalizedResourceData.ResourceType resourceType;
}
