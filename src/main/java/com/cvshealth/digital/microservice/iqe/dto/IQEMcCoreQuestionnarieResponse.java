package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Data;

import java.util.List;

@Data
public class IQEMcCoreQuestionnarieResponse {

        public List<QuestionnaireUIResponse.Question> questions;
        private List<IQEDetail> details;

}