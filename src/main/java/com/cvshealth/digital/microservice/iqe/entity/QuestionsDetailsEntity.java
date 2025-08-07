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

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(QuestionsDetailsEntity.TABLE_NAME)
public class QuestionsDetailsEntity {
    public static final String TABLE_NAME = DBConstants.QUESTIONS_DETAILS;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name="action_id", ordinal = 0)
    private String actionId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "detail_id", ordinal = 1 ,ordering = Ordering.ASCENDING)
    private String detailId;
    @Column("footer")
    private String footer;
    @Column("helper")
    private String helper;
    @Column("instructions")
    private String instructions;
    @Column("page_number")
    private int pageNumber;
    @Column("sequence_id")
    private int sequenceId;
    @Column("sub_context")
    private String subContext;
    @Column("title")
    private String title;
    
    @Column("audit")
    @Frozen
    private AuditEntity audit;
    
    @Column("is_active")
    @Builder.Default
    private boolean isActive = true;
}
