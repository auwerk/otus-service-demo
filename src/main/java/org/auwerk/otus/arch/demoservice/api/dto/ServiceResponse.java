package org.auwerk.otus.arch.demoservice.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponse {
    ServiceResponseStatus status;
}
