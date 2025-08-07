package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RulesByFlow {

    @Schema(description = "flow")
    @NotBlank(message = "Flow is null or empty")
    private String flow;
    @Schema(description = "ruleId")
    private String ruleId;
    @Schema(description = "ruleName")
    @NotBlank(message = "RuleName is null or empty")
    private String ruleName;
    @Schema(description = "actionId")
    private String actionId;
    @Schema(description = "condition")
    @NotBlank(message = "Condition is null or empty")
    private String condition;
    @Schema(description = "lob")
    @NotBlank(message = "LOB is null or empty")
    private String lob;
    @Schema(description = "salience")
    private int salience;
    @Schema(description = "isActive")
    private boolean isActive;
    @Schema(description = "audit")
    private Audit audit;
    @Schema(description = "isUpdate")
    private boolean isUpdate;

}