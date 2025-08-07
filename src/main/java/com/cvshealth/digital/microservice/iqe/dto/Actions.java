package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Actions {

    @Schema(description = "actionId")
    private String actionId;
    @Schema(description = "actionText")
    private String actionText;
    @Schema(description = "questionIdList")
    private List<String> questionIds;
    @Schema(description = "detailIds")
    private List<String> detailIds;
}