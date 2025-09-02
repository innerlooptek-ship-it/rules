# Updated Redis Fallback Flow Diagram

## Enhanced Flow with Redis Fallback for Cassandra Outages

```mermaid
graph TD
    A[User Request] --> B{Check Cassandra Status}
    
    %% Primary Flow - Cassandra Available
    B -->|Cassandra UP| C[Query Cassandra RulesByFlow Table<br/>Get Rules by Flow<br/><i>Retrieves: ruleId, condition, actionId, salience<br/>Compiles dynamic DRL rules with template<br/>Rules match request data to set actionId</i>]
    C --> D[Drools Execution<br/>Compile & Execute Rules]
    D --> E[Action ID Returned]
    E --> F[Check Redis for ActionId]
    F -->|ActionId Present| G[Return Cached Data]
    F -->|ActionId Not Present| H[Query Cassandra Multiple Tables<br/>actions, questions, answer_options, details]
    H --> I[Build Complete Response]
    I --> J[Cache in Redis by ActionId]
    J --> K[Return to User]
    
    %% Fallback Flow - Cassandra Down
    B -->|Cassandra DOWN| L[Query Redis Rules Cache<br/><i>Get cached rules list from Redis<br/>Data Type: List of RulesByFlow objects<br/>Loaded at application startup</i>]
    L --> M[Drools Execution on Cached Rules<br/>Compile & Execute Rules from Redis]
    M --> N[Action ID Determined from Cache]
    N --> O[Lookup Redis by ActionId<br/><i>Get complete questionnaire response<br/>Pre-cached during normal operations</i>]
    O -->|ActionId Found| P[Return Cached Response]
    O -->|ActionId Not Found| Q[Return Error: Data Not Available]
    
    %% Application Startup Cache Warming
    subgraph "Application Startup"
        R[Application Start] --> S[Load All Rules from Cassandra]
        S --> T[Cache Rules as List in Redis<br/><i>Key: rules_by_flow_list<br/>Value: List of all RulesByFlow objects</i>]
        T --> U[Load All ActionIds from Cassandra]
        U --> V[For Each ActionId: Build Complete Response]
        V --> W[Cache Complete Responses in Redis<br/><i>Key: questionnaire:actionId<br/>Value: Complete questionnaire object</i>]
    end
    
    %% Styling
    classDef primaryFlow fill:#e1f5fe
    classDef fallbackFlow fill:#fff3e0
    classDef cacheWarmup fill:#f3e5f5
    classDef errorFlow fill:#ffebee
    
    class C,D,E,F,G,H,I,J,K primaryFlow
    class L,M,N,O,P fallbackFlow
    class R,S,T,U,V,W cacheWarmup
    class Q errorFlow
```

## Key Implementation Changes Required

### 1. Application Startup Cache Warming
```java
@EventListener(ApplicationReadyEvent.class)
public void warmupRedisCache() {
    // Load all rules into Redis as a list
    List<RulesByFlowEntity> allRules = rulesByFlowRepo.findAll().collectList().block();
    redisTemplate.opsForValue().set("rules_by_flow_list", allRules);
    
    // Load all complete questionnaire responses by actionId
    List<String> allActionIds = actionsRepo.findAllActionIds().collectList().block();
    for (String actionId : allActionIds) {
        QuestionareRequest completeResponse = buildCompleteQuestionnaire(actionId);
        redisTemplate.opsForValue().set("questionnaire:" + actionId, completeResponse);
    }
}
```

### 2. Enhanced Rules Query with Fallback
```java
public Mono<List<RulesByFlowEntity>> getRulesByFlow(String flow) {
    return cassandraHealthCheck()
        .flatMap(isHealthy -> {
            if (isHealthy) {
                // Primary: Query Cassandra
                return rulesByFlowRepo.findByFlow(flow).collectList();
            } else {
                // Fallback: Query Redis cached rules
                List<RulesByFlowEntity> cachedRules = redisTemplate.opsForValue()
                    .get("rules_by_flow_list");
                return Mono.just(cachedRules.stream()
                    .filter(rule -> rule.getFlow().equals(flow))
                    .collect(Collectors.toList()));
            }
        });
}
```

### 3. Complete Response Lookup with Fallback
```java
public Mono<QuestionareRequest> getQuestionnaireByActionId(String actionId) {
    return cassandraHealthCheck()
        .flatMap(isHealthy -> {
            if (isHealthy) {
                // Primary: Build from Cassandra + cache result
                return buildFromCassandra(actionId)
                    .doOnNext(response -> 
                        redisTemplate.opsForValue().set("questionnaire:" + actionId, response));
            } else {
                // Fallback: Get from Redis cache
                QuestionareRequest cachedResponse = redisTemplate.opsForValue()
                    .get("questionnaire:" + actionId);
                return cachedResponse != null ? 
                    Mono.just(cachedResponse) : 
                    Mono.error(new ServiceUnavailableException("Data not available"));
            }
        });
}
```

## Redis Data Structure

### Rules Cache
```
Key: "rules_by_flow_list"
Type: List
Value: [
  {
    "ruleId": "rule1",
    "flow": "VACCINE", 
    "condition": "requiredQuestionnaireContext==\"MC_VACCINE_PATIENT_PROFILE_INFO\"",
    "actionId": "15829236-712a-4b0f-ae71-efeccc1ed924",
    "salience": 100
  },
  // ... more rules
]
```

### Complete Questionnaire Cache
```
Key: "questionnaire:15829236-712a-4b0f-ae71-efeccc1ed924"
Type: Object
Value: {
  "actions": { "questionIds": ["race", "ethnicity"] },
  "questions": [
    {
      "questionId": "race",
      "text": "What's your race?",
      "answerOptions": [...]
    }
  ],
  "details": {...}
}
```

## Benefits of This Approach

1. **Complete Fallback**: When Cassandra is down, entire flow works from Redis
2. **Rules Engine Compatibility**: Drools can execute rules from cached data
3. **Performance**: Pre-cached complete responses for instant serving
4. **Startup Warming**: All data loaded into Redis at application start
5. **Graceful Degradation**: Clear error handling when data unavailable

## Implementation Priority

1. **Phase 1**: Implement cache warming at application startup
2. **Phase 2**: Add Cassandra health checks and fallback logic
3. **Phase 3**: Update existing services to use fallback pattern
4. **Phase 4**: Add monitoring and alerting for fallback scenarios
