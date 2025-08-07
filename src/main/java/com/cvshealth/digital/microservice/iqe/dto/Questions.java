package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.cassandra.core.mapping.Column;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Questions {

    @Schema(description = "actionId")
    private String actionId;
    @Schema(description = "questionId")
    private String questionId;
    @Schema(description = "text")
//    @NotBlank(message = "text is null or empty")
    private String text;
    @Schema(description = "errorMessage")
    private String errorMessage;
    @Schema(description = "answerType")
    @NotBlank(message = "answerType is null or empty")
    private String answerType;
    @Schema(description = "answerOptionIds")
    private List<String> answerOptionIds;
    @Schema(description = "answerOptions")
    @Valid
    private List<AnswerOptions> answerOptions;
    @Schema(description = "helpText")
    private String helpText;
    @Schema(description = "characterLimit")
    private Integer characterLimit;
    @Schema(description = "stacked")
    private boolean stacked;
    @Column("sequenceId")
    private Integer sequenceId;
    @Schema(description = "required")
    private boolean required;
    @Schema(description = "linkText")
    private String linkText;
    @Schema(description = "questionNumber")
    private Integer questionNumber;
    @Schema(description = "skipLegend")
    private String skipLegend;
    @Schema(description = "subContext")
    private String subContext;

}