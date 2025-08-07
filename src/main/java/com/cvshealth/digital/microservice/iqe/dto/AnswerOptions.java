package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
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
public class AnswerOptions {

    @Column("actionId")
    private String actionId;
    @Column("questionId")
    private String questionId;
    @Column("answerOptionId")
    private String answerOptionId;
    @Column("text")
    @NotBlank(message = "text is null or empty")
    private String text;
    @Column("value")
    @NotBlank(message = "value is null or empty")
    private String value;
    @Column("questionIds")
    private List<String> relatedQuestionIds;
    @Column("questions")
    @Valid
    private List<Questions> relatedQuestions;
    @Column("sequenceId")
    private Integer sequenceId;
    @Column("additionalDetailText")
    private String additionalDetailText;
}