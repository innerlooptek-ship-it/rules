package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Details {
    @Schema(description = "actionId")
    private String actionId;
    @Schema(description = "detailId")
    private String detailId;
    @Schema(description = "footer")
    private String footer;
    @Schema(description = "helper")
    private String helper;
    @Schema(description = "instructions")
    private String instructions;
    @Schema(description = "pageNumber")
    private Integer pageNumber;
    @Schema(description = "sequenceId")
    private Integer sequenceId;
    @Schema(description = "subContext")
    private String subContext;
    @Schema(description = "title")
    private String title;

}
