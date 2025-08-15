package com.cvshealth.digital.microservice.iqe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class GetConsent extends ApiResponse {
    public List<ConsentData> consentsData;
}