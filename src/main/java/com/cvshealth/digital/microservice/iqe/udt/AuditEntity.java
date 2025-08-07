
package com.cvshealth.digital.microservice.iqe.udt;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
 
/**
* The Class RulesByFlowRepository
*
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@UserDefinedType("audit")
public class AuditEntity {
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
}