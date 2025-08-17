package com.cvshealth.digital.microservice.iqe.mapper;


import com.cvshealth.digital.microservice.iqe.config.ConsentConfig;
import com.cvshealth.digital.microservice.iqe.model.ConsentData;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GetConsentMapper {

    @Mapping(target = "consents", expression = "java(toConsents(consents.getConsents()))")
    @Mapping(target = "patientReferenceId", source = "patientReferenceId")
    ConsentData toGetConsent(ConsentConfig consents, String patientReferenceId);

    List<ConsentData.Consent> toConsents(List<ConsentConfig.Consent> consent);
}