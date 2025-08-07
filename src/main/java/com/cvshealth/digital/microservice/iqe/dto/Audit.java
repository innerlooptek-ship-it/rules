package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Audit {

    @Column("created_ts")
    private String createdTs;
    @Column("created_by")
    private String createdBy;
    @Column("modified_ts")
    private String modifiedTs;
    @Column("modified_by")
    private String modifiedBy;
    @Column("remarks")
    private String remarks;
    @Schema(description = "questionIdList")
    private List<String> questionIdList;

}