package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.scheduling.model.types.FaultSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cvshealth.digital.microservice.scheduling.dto.response.ConsentData;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VMGetConsentsResponse {

    private String statusCode;
    private String statusDescription;
    private String appointmentId;
    private List<ConsentData> consentsData;
    private FaultSection fault;
}