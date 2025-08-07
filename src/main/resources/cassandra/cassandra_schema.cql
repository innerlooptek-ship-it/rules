CREATE KEYSPACE IF NOT EXISTS iqe WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

use iqe;

CREATE TABLE IF NOT EXISTS iqe.questionnaire_rules
(
    flow      text,
    rule_id   text,
    rule_name text,
    action    text,
    condition text,
    lob       text,
    salience  int,
    primary key ((flow), rule_id)
)
    WITH COMPRESSION = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
     AND SPECULATIVE_RETRY = '99PERCENTILE';



CREATE TYPE iqe.audit (

                          created_ts text,

                          created_by text,

                          modified_ts text,

                          modified_by text,

                          remarks text

                      );

CREATE TABLE iqe.rules_by_flow (

                                   flow text,

                                   rule_id text,

                                   action_id text,

                                   audit frozen<audit>,

                                   condition text,

                                   is_active boolean,

                                   lob text,

                                   rule_name text,

                                   salience int,

                                   PRIMARY KEY ((flow), rule_id)

);

CREATE TABLE iqe.actions (

                             action_id text,

                             action_text text,

                             question_id frozen<list<text>>,

                             PRIMARY KEY (action_id)

);


CREATE TABLE iqe.questions (

                               action_id text,

                               question_id text,

                               answer_option_id frozen<list<text>>,

                               answer_type text,

                               character_limit int,

                               error_message text,

                               help_text text,

                               is_stacked boolean,

                               question_text text,

                               sequence_id int,

                               required boolean,

                               PRIMARY KEY ((action_id), question_id)

);

CREATE TABLE iqe.answer_options (

                                    action_id text,

                                    question_id text,

                                    answer_option_id text,

                                    answer_text text,

                                    answer_value text,

                                    related_questions frozen<list<text>>,

                                    sequence_id int,

                                    PRIMARY KEY ((action_id), question_id, answer_option_id)

);

ALTER TABLE iqe.questions ADD link_text text;

ALTER TABLE iqe.answer_options ADD additional_detail_text text;


ALTER TABLE iqe.questions ADD sub_context text;

ALTER TABLE iqe.questions ADD skip_legend text;

ALTER TABLE iqe.questions ADD question_number int;

AlTER TABLE iqe.actions ADD detail_id frozen<list<text>>;

CREATE TABLE iqe.questions_details (
                                       action_id text,
                                       detail_id text,
                                       title text,
                                       instructions text,
                                       helper text,
                                       sub_context text,
                                       page_number int,
                                       footer text,
                                       sequence_id int,
                                       PRIMARY KEY ((action_id), detail_id)
);