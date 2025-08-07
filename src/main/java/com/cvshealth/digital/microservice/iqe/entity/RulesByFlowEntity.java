package com.cvshealth.digital.microservice.iqe.entity;

import com.cvshealth.digital.microservice.iqe.constants.DBConstants;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Frozen;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * The Class RulesByFlow
 *
 ** @author Mahesh Thakkilapati
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(RulesByFlowEntity.TABLE_NAME)
public class RulesByFlowEntity {
    public static final String TABLE_NAME = DBConstants.RULES_BY_FLOW;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name="flow", ordinal = 0)
    private String flow;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = DBConstants.RULEID, ordinal = 1 ,ordering = Ordering.ASCENDING)
    private String ruleId;
    @Column("rule_name")
    private String ruleName;
    @Column("action_id")
    private String actionId;
    @Column("condition")
    private String condition;
    @Column("lob")
    private String lob;
    @Column("salience")
    private int salience;
    @Column("is_active")
    private boolean isActive;
    @Column("audit")
    @Frozen
    private AuditEntity audit;
}