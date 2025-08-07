package com.cvshealth.digital.microservice.iqe.entity;

import com.cvshealth.digital.microservice.iqe.constants.DBConstants;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table(QuestionnaireRules.TABLE_NAME)
public class QuestionnaireRules {
    public static final String TABLE_NAME = DBConstants.QUESTIONNAIRE_RULES;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = DBConstants.FLOW, ordinal = 0)
    private String flow ;
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = DBConstants.RULEID, ordinal = 1 ,ordering = Ordering.ASCENDING)
    private String id;
    @Column("lob")
    private String lob;
    @Column("salience")
    private int salience;
    @Column("condition")
    private String condition;
    @Column("action")
    private String action;
    @Column("rule_name")
    private String ruleName;
}