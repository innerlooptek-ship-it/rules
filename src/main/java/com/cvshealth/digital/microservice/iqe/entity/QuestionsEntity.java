package com.cvshealth.digital.microservice.iqe.entity;

import com.cvshealth.digital.microservice.iqe.constants.DBConstants;
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
@Table(QuestionsEntity.TABLE_NAME)
public class QuestionsEntity {
    public static final String TABLE_NAME = DBConstants.QUESTIONS;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name="action_id", ordinal = 0)
    private String actionId;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "question_id", ordinal = 1 ,ordering = Ordering.ASCENDING)
    private String questionId;
    @Column("question_text")
    private String questionText;
    @Column("error_message")
    private String errorMessage;
    @Column("answer_type")
    private String answerType;
    @Column("answer_option_id")
    @Frozen
    private List<String> answerOptionId;
    @Column("help_text")
    private String helpText;
    @Column("character_limit")
    private int characterLimit;
    @Column("is_stacked")
    private boolean stacked;
    @Column("sequence_id")
    private Integer sequence_id;
    @Column("required")
    private boolean required;
    @Column("link_text")
    private String linkText;
    @Column("question_number")
    private Integer questionnumber;
    @Column("skip_legend")
    private String skiplegend;
    @Column("sub_context")
    private String subcontext;

}