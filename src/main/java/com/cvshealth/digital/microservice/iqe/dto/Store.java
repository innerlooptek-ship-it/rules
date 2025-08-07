package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Store {
    public String storeId;
    public String clinicId;
}