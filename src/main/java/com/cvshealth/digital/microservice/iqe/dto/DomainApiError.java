package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class DomainApiError extends Exception {
    public String statusCode;
    public String statusDescription;
    public Fault fault;

    @Data
    public static class Fault {
        private String type;
        private String title;
        private String detail;
        private String moreInfo;
        private List<Error> errors;

        @Data
        public static class Error {
            private String type;
            private String title;
            private String field;
        }
    }
}