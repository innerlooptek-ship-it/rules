package com.cvshealth.digital.microservice.iqe.drools;

import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.drools.template.ObjectDataCompiler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DroolsRulesEngineTest {
    
    @Test
    void shouldCompileAndExecuteDroolsRules() {
        List<RulesByFlowEntity> rules = List.of(TestDataBuilder.createTestRule());
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        
        String generatedDRL = compiler.compile(rules, 
                getClass().getClassLoader().getResourceAsStream("rules/getQuestionnaire-output.drl"));
        
        assertThat(generatedDRL).isNotNull();
        assertThat(generatedDRL).contains("rule \"rule-1\"");
        assertThat(generatedDRL).contains("requiredQuestionnaireContext==\"test\"");
    }
    
    @Test
    void shouldHandleEmptyRulesList() {
        List<RulesByFlowEntity> rules = List.of();
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        
        String generatedDRL = compiler.compile(rules, 
                getClass().getClassLoader().getResourceAsStream("rules/getQuestionnaire-output.drl"));
        
        assertThat(generatedDRL).isNotNull();
    }
    
    @Test
    void shouldCompileMultipleRules() {
        RulesByFlowEntity rule1 = TestDataBuilder.createTestRule();
        RulesByFlowEntity rule2 = TestDataBuilder.createTestRule();
        rule2.setRuleId("rule-2");
        rule2.setCondition("requiredQuestionnaireContext==\"test2\"");
        
        List<RulesByFlowEntity> rules = List.of(rule1, rule2);
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        
        String generatedDRL = compiler.compile(rules, 
                getClass().getClassLoader().getResourceAsStream("rules/getQuestionnaire-output.drl"));
        
        assertThat(generatedDRL).isNotNull();
        assertThat(generatedDRL).contains("rule \"rule-1\"");
        assertThat(generatedDRL).contains("rule \"rule-2\"");
    }
    
    @Test
    void shouldHandleRuleWithHighSalience() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        rule.setSalience(1000);
        
        List<RulesByFlowEntity> rules = List.of(rule);
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        
        String generatedDRL = compiler.compile(rules, 
                getClass().getClassLoader().getResourceAsStream("rules/getQuestionnaire-output.drl"));
        
        assertThat(generatedDRL).isNotNull();
        assertThat(generatedDRL).contains("salience 1000");
    }
    
    @Test
    void shouldHandleRuleWithComplexCondition() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        rule.setCondition("requiredQuestionnaireContext==\"test\" && age > 18");
        
        List<RulesByFlowEntity> rules = List.of(rule);
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        
        String generatedDRL = compiler.compile(rules, 
                getClass().getClassLoader().getResourceAsStream("rules/getQuestionnaire-output.drl"));
        
        assertThat(generatedDRL).isNotNull();
        assertThat(generatedDRL).contains("requiredQuestionnaireContext==\"test\" && age > 18");
    }
}
