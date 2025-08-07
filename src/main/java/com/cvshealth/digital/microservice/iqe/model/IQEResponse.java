package com.cvshealth.digital.microservice.iqe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IQEResponse {

    @Column("statusCode")
    private String statusCode;
    @Column("statusDescription")
    private String statusDescription;
    @Column("actionId")
    private String actionId;

}