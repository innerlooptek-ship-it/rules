package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetDroolsService {
    
    private final DatasetFirstReadService datasetFirstReadService;
    
    @Autowired
    private KieBase kieBase;
    
    public Mono<String> executeRulesFromDataset(String flow, RulesDetails rulesDetails) {
        return datasetFirstReadService.getRulesByFlowFromDataset(flow)
            .flatMap(rules -> {
                if (rules.isEmpty()) {
                    log.warn("No rules found in dataset for flow: {}", flow);
                    return Mono.empty();
                }
                
                try {
                    InputStream templateInputStream = getClass().getClassLoader()
                        .getResourceAsStream("rules/getQuestionnaire-output.drl");
                    ObjectDataCompiler objectDataCompiler = new ObjectDataCompiler();
                    String compiledDRL = objectDataCompiler.compile(rules, templateInputStream);
                    
                    KieSession kieSession = kieBase.newKieSession();
                    kieSession.insert(rulesDetails);
                    kieSession.fireAllRules(1);
                    kieSession.dispose();
                    
                    String actionId = rulesDetails.getActionId();
                    log.debug("Drools execution from dataset determined actionId: {} for flow: {}", actionId, flow);
                    
                    return Mono.just(actionId);
                } catch (Exception e) {
                    log.error("Error executing Drools from dataset for flow: {}", flow, e);
                    return Mono.error(e);
                }
            });
    }
}
