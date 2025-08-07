package com.cvshealth.digital.microservice.iqe.entity;

import com.cvshealth.digital.microservice.iqe.constants.DBConstants;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Frozen;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import java.util.List;

/**
 * The Class Actions
 *
 ** @author Mahesh Thakkilapati
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(ActionsEntity.TABLE_NAME)
public class ActionsEntity {

    public static final String TABLE_NAME = DBConstants.ACTIONS;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name="action_id", ordinal = 0)
    private String actionId;
    @Column("action_text")
    private String actionText;
    @Column("question_id")
    private List<String> questionId;
    @Column("detail_id")
    private List<String> detailId;
    
    @Column("audit")
    @Frozen
    private AuditEntity audit;
    
    @Column("is_active")
    @Builder.Default
    private boolean isActive = true;

}
