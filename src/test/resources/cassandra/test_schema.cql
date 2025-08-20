CREATE KEYSPACE IF NOT EXISTS iqe_test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE iqe_test;

CREATE TABLE IF NOT EXISTS rules_by_flow (
    flow text,
    rule_id text,
    action_id text,
    condition text,
    rule_name text,
    salience int,
    lob text,
    is_active boolean,
    created_ts timestamp,
    created_by text,
    updated_ts timestamp,
    updated_by text,
    PRIMARY KEY ((flow), rule_id)
);

CREATE TABLE IF NOT EXISTS actions (
    action_id text PRIMARY KEY,
    action_text text,
    question_ids list<text>,
    created_ts timestamp,
    created_by text,
    updated_ts timestamp,
    updated_by text,
    is_active boolean
);

CREATE TABLE IF NOT EXISTS questions (
    action_id text,
    question_id text,
    text text,
    answer_type text,
    answer_option_ids list<text>,
    help_text text,
    character_limit int,
    is_stocked boolean,
    sequence_id int,
    created_ts timestamp,
    created_by text,
    updated_ts timestamp,
    updated_by text,
    is_active boolean,
    PRIMARY KEY ((action_id), question_id)
);

CREATE TABLE IF NOT EXISTS answer_options (
    action_id text,
    question_id text,
    answer_option_id text,
    text text,
    value text,
    sequence_id int,
    related_question_ids list<text>,
    created_ts timestamp,
    created_by text,
    updated_ts timestamp,
    updated_by text,
    is_active boolean,
    PRIMARY KEY ((action_id, question_id), answer_option_id)
);

CREATE TABLE IF NOT EXISTS questionnaire_details (
    action_id text,
    question_id text,
    answer_option_id text,
    related_question_id text,
    created_ts timestamp,
    created_by text,
    updated_ts timestamp,
    updated_by text,
    is_active boolean,
    PRIMARY KEY ((action_id), question_id, answer_option_id, related_question_id)
);
