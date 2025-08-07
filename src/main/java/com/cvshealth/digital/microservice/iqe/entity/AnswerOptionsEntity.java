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

/**
 * The Class RulesByFlowRepository
 *
 ** @author Mahesh Thakkilapati
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(AnswerOptionsEntity.TABLE_NAME)
public class AnswerOptionsEntity {
    public static final String TABLE_NAME = DBConstants.ANSWER_OPTIONS;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name="action_id", ordinal = 0)
    private String actionId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name="question_id", ordinal = 1)
    private String questionId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "answer_option_id", ordinal = 2 ,ordering = Ordering.ASCENDING)
    private String answerOptionId;
    @Column("answer_text")
    private String answerText;
    @Column("answer_value")
    private String answerValue;
    @Column("related_questions")
    @Frozen
    private List<String> relatedQuestions;
    @Column("sequence_id")
    private Integer sequence_id;
    @Column("additional_detail_text")
    private String additionalDetailText;
    
    @Column("audit")
    @Frozen
    private AuditEntity audit;
    
    @Column("is_active")
    @Builder.Default
    private boolean isActive = true;
}
