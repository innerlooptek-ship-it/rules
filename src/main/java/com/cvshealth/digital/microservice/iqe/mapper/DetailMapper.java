package com.cvshealth.digital.microservice.iqe.mapper;


import com.cvshealth.digital.microservice.iqe.dto.Detail;
import com.cvshealth.digital.microservice.iqe.dto.IQEDetail;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetailMapper {
    // Map a single IQEDetail to Detail
    Detail toDetail(IQEDetail iqeDetail);

    // Map a list of IQEDetail to a list of Detail
    List<Detail> toDetailList(List<IQEDetail> iqeDetails);
}