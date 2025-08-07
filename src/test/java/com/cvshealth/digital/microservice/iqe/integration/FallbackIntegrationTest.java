package com.cvshealth.digital.microservice.iqe.integration;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.service.FallbackCacheService;
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cassandra.contact-points=localhost:9042",
    "spring.cassandra.local-datacenter=datacenter1",
    "service.fallback-cache.enabled=false",
    "logging.level.root=WARN"
})
class FallbackIntegrationTest {

    @Autowired
    private IQEService iqeService;

    @Autowired
    private FallbackCacheService fallbackCacheService;

    @MockBean
    private RulesByFlowRepository rulesByFlowRepo;

    @Test
    void testEndToEndFallbackFlow() {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setRequiredQuestionnaireContext("testContext");

        QuestionareRequest questionareRequest = new QuestionareRequest();

        when(rulesByFlowRepo.findByFlow(anyString()))
            .thenReturn(Flux.error(new RuntimeException("Simulated Cassandra outage")));

        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, questionareRequest, Map.of()))
            .expectNextMatches(result -> result != null)
            .verifyComplete();
    }

    @Test
    void testCacheWarmingOnStartup() {
        StepVerifier.create(fallbackCacheService.warmCache())
            .verifyComplete();
    }
}
