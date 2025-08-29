# JIRA Story: Enhanced Redis Caching Strategy with Table-Level Caching and Feature Flags

## Story Title
**Implement Enhanced Redis Caching Strategy with Feature Flag-Based Configuration and Cassandra Synchronization**

## Story Type
**Epic/Feature**

## Priority
**High**

## Labels
- `performance`
- `caching`
- `redis`
- `cassandra`
- `feature-flags`
- `high-availability`
- `data-consistency`

---

## Problem Statement

The current Redis caching implementation has significant limitations that impact system availability and performance:

### Current Limitations
1. **Limited Scope**: Only caches specific `actionId` values and their corresponding questions
2. **No Fallback Strategy**: When Cassandra is unavailable, new `actionId` questions cannot be retrieved
3. **Cache Miss Dependency**: Every cache miss requires Cassandra access, creating single point of failure
4. **Static Configuration**: No dynamic switching between caching strategies
5. **Inconsistent Data**: No synchronization mechanism between Redis and Cassandra updates

### Business Impact
- **Service Unavailability**: Complete service failure when Cassandra is down
- **Performance Degradation**: High latency during Cassandra outages
- **Limited Scalability**: Cannot serve questionnaires without Cassandra access
- **Data Inconsistency**: Stale cache data when Cassandra is updated externally

## Current Architecture Analysis

### Cassandra Table Structure
Based on schema analysis from `cassandra_schema.cql`:

```sql
-- Core questionnaire tables
rules_by_flow (flow, rule_id, rule_name, action_id, condition, lob, salience, is_active)
actions (action_id, action_text, question_ids, detail_ids)  
questions (action_id, question_id, text, answer_type, sequence_id, required)
answer_options (action_id, question_id, answer_option_id, text, value)
questions_details (action_id, detail_id, title, instructions, page_number)
```

### Current Redis Implementation
- **Key Pattern**: Individual keys per `actionId` (e.g., `"ACT_001_VACCINE_ELIGIBILITY"`)
- **Cache Strategy**: Cache-aside pattern with Cassandra fallback
- **Data Structure**: `RedisCacheObject` wrapper with `QuestionareRequest` payload
- **TTL**: 1-hour expiration (`genericcache_1h_noextend`)
- **Status**: Currently disabled (`redisFlag: false`)

### Repository Layer
- **Reactive Repositories**: `QuestionsRepository`, `ActionsRepository`, `AnswerOptionsRepository`, `RulesByFlowRepository`
- **Orchestration**: `IQERepoOrchestrator` coordinates multi-table operations
- **Current Caching**: Limited to `questionnaireByActionId()` method in `IQEService`

## Proposed Enhanced Solution

### 1. Feature Flag-Based Caching Strategies

#### Strategy 1: Redis-First (Default)
```yaml
caching:
  strategy: "redis-first"
  fallback-enabled: true
  table-caching-enabled: true
```

**Flow**:
1. Query Redis for requested data
2. If cache hit → return data immediately
3. If cache miss → query Cassandra
4. Populate Redis with Cassandra result
5. Return data to client

#### Strategy 2: Cassandra-First (Fallback)
```yaml
caching:
  strategy: "cassandra-first"
  cache-population-enabled: true
```

**Flow**:
1. Query Cassandra directly
2. Populate/update Redis with result
3. Return data to client

### 2. Table-Level Caching Architecture

#### Selective Table Caching (Recommended Approach)
Based on Redis documentation research, **selective caching is preferred over full table caching**:

**Cache Structure**:
```json
{
  "questionnaire:tables:actions": {
    "ACT_001": { "actionText": "...", "questionIds": [...] },
    "ACT_002": { "actionText": "...", "questionIds": [...] }
  },
  "questionnaire:tables:questions:ACT_001": [
    { "questionId": "Q001", "text": "...", "answerType": "..." }
  ],
  "questionnaire:tables:rules_by_flow:VACCINE": [
    { "ruleId": "R001", "actionId": "ACT_001", "condition": "..." }
  ]
}
```

**Key Benefits**:
- **Granular Access**: Load only needed data partitions
- **Memory Efficiency**: Avoid caching unused questionnaire data
- **Scalable**: Aligns with Cassandra partition key patterns
- **Performance**: Faster access to specific data subsets

#### Alternative: Full Table Caching (Not Recommended)
**Redis Documentation Analysis**: Full table caching has significant drawbacks:
- **Memory Overhead**: Stores entire tables regardless of usage patterns
- **Update Complexity**: Requires full table refresh on any change
- **Network Overhead**: Large payloads for simple queries
- **Cache Invalidation**: Difficult to manage partial updates

### 3. Enhanced Configuration System

#### Application Configuration
```yaml
service:
  redis-cache:
    enabled: true
    strategy: "redis-first"  # redis-first | cassandra-first
    
    # Table-level caching configuration
    table-caching:
      enabled: true
      strategy: "selective"  # selective | full
      tables:
        actions:
          enabled: true
          ttl: "2h"
          key-pattern: "questionnaire:tables:actions"
        questions:
          enabled: true
          ttl: "2h"
          key-pattern: "questionnaire:tables:questions:{actionId}"
        rules_by_flow:
          enabled: true
          ttl: "4h"
          key-pattern: "questionnaire:tables:rules_by_flow:{flow}"
        answer_options:
          enabled: true
          ttl: "2h"
          key-pattern: "questionnaire:tables:answer_options:{actionId}"
        questions_details:
          enabled: true
          ttl: "2h"
          key-pattern: "questionnaire:tables:details:{actionId}"
    
    # Synchronization settings
    synchronization:
      enabled: true
      invalidation-strategy: "immediate"  # immediate | batch | scheduled
      write-through: false
      write-behind: true
      
    # Fallback configuration
    fallback:
      cassandra-timeout: "5s"
      redis-timeout: "2s"
      circuit-breaker:
        enabled: true
        failure-threshold: 5
        recovery-timeout: "30s"
```

#### Feature Flag Integration
```java
@Component
public class CachingStrategyConfig {
    
    @Value("${service.redis-cache.strategy:redis-first}")
    private String cachingStrategy;
    
    @Value("${service.redis-cache.table-caching.enabled:true}")
    private boolean tableCachingEnabled;
    
    public enum CachingStrategy {
        REDIS_FIRST, CASSANDRA_FIRST
    }
    
    public CachingStrategy getStrategy() {
        return CachingStrategy.valueOf(cachingStrategy.toUpperCase().replace("-", "_"));
    }
}
```

### 4. Synchronization Mechanisms

#### 4.1 Cache Invalidation on Cassandra Updates

**Immediate Invalidation Strategy**:
```java
@Component
public class CacheSynchronizationService {
    
    @EventListener
    public void handleCassandraUpdate(CassandraUpdateEvent event) {
        switch (event.getTable()) {
            case "actions":
                invalidateActionCache(event.getActionId());
                break;
            case "questions":
                invalidateQuestionCache(event.getActionId());
                break;
            case "rules_by_flow":
                invalidateRulesCache(event.getFlow());
                break;
        }
    }
    
    private void invalidateActionCache(String actionId) {
        redisCacheService.delete("questionnaire:tables:actions");
        // Optionally refresh cache immediately
        if (cacheConfig.isEagerRefresh()) {
            refreshActionCache();
        }
    }
}
```

#### 4.2 Write-Through and Write-Behind Patterns

**Write-Through (Immediate Consistency)**:
```java
public Mono<Void> updateAction(Actions action) {
    return cassandraRepository.save(action)
        .flatMap(savedAction -> {
            // Immediately update Redis
            return redisCacheService.updateActionInCache(savedAction);
        });
}
```

**Write-Behind (Eventual Consistency)**:
```java
@Scheduled(fixedDelay = 30000) // 30 seconds
public void syncPendingUpdates() {
    pendingUpdates.forEach(update -> {
        redisCacheService.updateCache(update);
    });
    pendingUpdates.clear();
}
```

### 5. Enhanced Service Layer Implementation

#### 5.1 Enhanced IQEService with Strategy Pattern
```java
@Service
public class EnhancedIQEService {
    
    private final CachingStrategyFactory strategyFactory;
    private final CachingStrategyConfig config;
    
    public Mono<QuestionareRequest> questionnaireByActionId(String actionId) {
        CachingStrategy strategy = strategyFactory.getStrategy(config.getStrategy());
        return strategy.getQuestionnaire(actionId);
    }
}

interface CachingStrategy {
    Mono<QuestionareRequest> getQuestionnaire(String actionId);
}

@Component
class RedisFirstStrategy implements CachingStrategy {
    public Mono<QuestionareRequest> getQuestionnaire(String actionId) {
        return redisCacheService.getFromTableCache(actionId)
            .switchIfEmpty(cassandraService.getQuestionnaire(actionId)
                .flatMap(result -> redisCacheService.cacheQuestionnaire(result)
                    .thenReturn(result)));
    }
}

@Component  
class CassandraFirstStrategy implements CachingStrategy {
    public Mono<QuestionareRequest> getQuestionnaire(String actionId) {
        return cassandraService.getQuestionnaire(actionId)
            .flatMap(result -> redisCacheService.cacheQuestionnaire(result)
                .thenReturn(result));
    }
}
```

#### 5.2 Enhanced Redis Cache Service
```java
@Service
public class EnhancedRedisCacheService {
    
    // Table-level caching methods
    public Mono<Actions> getActionFromTableCache(String actionId) {
        return getFromCache("questionnaire:tables:actions")
            .map(actionsMap -> actionsMap.get(actionId));
    }
    
    public Mono<List<Questions>> getQuestionsFromTableCache(String actionId) {
        return getFromCache("questionnaire:tables:questions:" + actionId);
    }
    
    public Mono<List<RulesByFlowEntity>> getRulesFromTableCache(String flow) {
        return getFromCache("questionnaire:tables:rules_by_flow:" + flow);
    }
    
    // Bulk caching operations
    public Mono<Void> cacheAllActions(List<Actions> actions) {
        Map<String, Actions> actionsMap = actions.stream()
            .collect(Collectors.toMap(Actions::getActionId, Function.identity()));
        return cacheObject("questionnaire:tables:actions", actionsMap);
    }
    
    // Cache invalidation
    public Mono<Void> invalidateTableCache(String table, String key) {
        String cacheKey = String.format("questionnaire:tables:%s:%s", table, key);
        return deleteFromCache(cacheKey);
    }
}
```

### 6. Circuit Breaker and Resilience Patterns

#### 6.1 Circuit Breaker Implementation
```java
@Component
public class ResilientCacheService {
    
    private final CircuitBreaker cassandraCircuitBreaker;
    private final CircuitBreaker redisCircuitBreaker;
    
    public Mono<QuestionareRequest> getQuestionnaireWithResilience(String actionId) {
        return Mono.defer(() -> {
            if (cassandraCircuitBreaker.getState() == CircuitBreaker.State.OPEN) {
                // Cassandra is down, serve from Redis only
                return redisOnlyFallback(actionId);
            }
            
            return normalFlow(actionId)
                .onErrorResume(throwable -> {
                    if (isTimeoutException(throwable)) {
                        return redisOnlyFallback(actionId);
                    }
                    return Mono.error(throwable);
                });
        });
    }
    
    private Mono<QuestionareRequest> redisOnlyFallback(String actionId) {
        return redisCacheService.getFromTableCache(actionId)
            .switchIfEmpty(Mono.error(new ServiceUnavailableException(
                "Both Cassandra and Redis cache unavailable for actionId: " + actionId)));
    }
}
```

### 7. Monitoring and Observability

#### 7.1 Cache Metrics
```java
@Component
public class CacheMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void recordCacheHit(CacheHitEvent event) {
        Counter.builder("cache.hits")
            .tag("strategy", event.getStrategy())
            .tag("table", event.getTable())
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void recordCacheMiss(CacheMissEvent event) {
        Counter.builder("cache.misses")
            .tag("strategy", event.getStrategy())
            .tag("table", event.getTable())
            .register(meterRegistry)
            .increment();
    }
}
```

#### 7.2 Health Checks
```java
@Component
public class CacheHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Test Redis connectivity
            redisCacheService.ping().block(Duration.ofSeconds(2));
            
            // Test Cassandra connectivity  
            cassandraTemplate.select("SELECT now() FROM system.local", Object.class)
                .blockFirst(Duration.ofSeconds(2));
                
            return Health.up()
                .withDetail("redis", "UP")
                .withDetail("cassandra", "UP")
                .withDetail("strategy", config.getStrategy())
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Redis Documentation Analysis & Recommendations

### Based on Official Redis Documentation Research

#### Memory Usage Considerations
- **Per-Key Overhead**: Each Redis key requires ~35+ bytes overhead
- **JSON Storage**: Redis JSON stores data efficiently as binary with sub-element access
- **String Reuse**: Redis optimizes repeated strings across documents

#### Recommended Approach: Selective Table Caching
**Why Selective Over Full Table Caching**:

1. **Access Pattern Alignment**: Questionnaires accessed by specific identifiers (actionId, flow)
2. **Memory Efficiency**: Only cache actively used data partitions
3. **Performance**: Faster access to specific data subsets vs large table scans
4. **Scalability**: Aligns with Cassandra partition key patterns
5. **Redis Best Practice**: Individual keys for distinct data entities

#### Key Design Patterns (from Redis.io)
```
questionnaire:tables:actions           # All actions (small dataset)
questionnaire:tables:questions:{actionId}    # Questions per action
questionnaire:tables:rules_by_flow:{flow}    # Rules per flow
questionnaire:tables:answer_options:{actionId} # Answer options per action
questionnaire:tables:details:{actionId}      # Details per action
```

#### Performance Optimization
- **TTL Strategy**: Longer TTL for stable data (rules: 4h), shorter for dynamic data (questions: 2h)
- **Compression**: Use Redis JSON for complex objects, strings for simple values
- **Batch Operations**: Use Redis pipelines for bulk cache operations
- **Memory Monitoring**: Track memory usage per key pattern

## Acceptance Criteria

### 1. Feature Flag Configuration
- [ ] Implement dynamic switching between Redis-first and Cassandra-first strategies
- [ ] Configuration changes take effect without service restart
- [ ] Default strategy is Redis-first with Cassandra fallback
- [ ] Feature flags are externally configurable (environment variables, config service)

### 2. Table-Level Caching Implementation
- [ ] Implement selective table caching for all 5 core tables
- [ ] Cache keys follow Redis naming conventions
- [ ] TTL configuration per table type
- [ ] Memory usage monitoring and alerting
- [ ] Cache hit/miss ratio tracking per table

### 3. Synchronization Mechanisms
- [ ] Immediate cache invalidation on Cassandra updates
- [ ] Write-through pattern for critical updates
- [ ] Write-behind pattern for bulk operations
- [ ] Conflict resolution for concurrent updates
- [ ] Data consistency validation between Redis and Cassandra

### 4. Resilience and Fallback
- [ ] Circuit breaker pattern for Cassandra failures
- [ ] Redis-only operation when Cassandra is unavailable
- [ ] Graceful degradation with appropriate error messages
- [ ] Automatic recovery when services come back online
- [ ] Timeout configuration for both Redis and Cassandra

### 5. Enhanced Service Layer
- [ ] Strategy pattern implementation for caching strategies
- [ ] Enhanced IQEService with table-level caching support
- [ ] Backward compatibility with existing API contracts
- [ ] Performance improvement over current implementation
- [ ] Comprehensive error handling and logging

### 6. Monitoring and Observability
- [ ] Cache performance metrics (hit/miss ratios, latency)
- [ ] Health checks for Redis and Cassandra connectivity
- [ ] Alerting for cache failures and degraded performance
- [ ] Dashboard for cache strategy monitoring
- [ ] Log correlation for debugging cache issues

### 7. Testing Strategy
- [ ] Unit tests for all caching strategies
- [ ] Integration tests with Redis and Cassandra
- [ ] Performance tests comparing strategies
- [ ] Chaos engineering tests (service failures)
- [ ] Load testing with cache enabled/disabled

## Benefits

### 1. High Availability
- **Service Continuity**: Serve questionnaires even when Cassandra is down
- **Reduced Downtime**: Eliminate single point of failure
- **Graceful Degradation**: Maintain core functionality during outages

### 2. Performance Improvements
- **Faster Response Times**: Redis-first strategy reduces latency
- **Reduced Database Load**: Offload read traffic from Cassandra
- **Scalable Architecture**: Independent scaling of cache and database layers

### 3. Operational Excellence
- **Dynamic Configuration**: Change strategies without deployment
- **Better Monitoring**: Comprehensive observability into cache performance
- **Simplified Operations**: Automated synchronization and recovery

### 4. Data Consistency
- **Synchronized Updates**: Automatic cache invalidation on data changes
- **Conflict Resolution**: Handle concurrent updates gracefully
- **Audit Trail**: Track cache operations for debugging

## Risks and Mitigation

### Risks
1. **Increased Complexity**: More moving parts and failure modes
2. **Memory Usage**: Higher Redis memory consumption
3. **Data Inconsistency**: Potential cache staleness
4. **Network Overhead**: Additional Redis calls

### Mitigation Strategies
1. **Comprehensive Testing**: Extensive integration and chaos testing
2. **Memory Monitoring**: Proactive alerting and capacity planning
3. **Synchronization Mechanisms**: Immediate invalidation and write-through patterns
4. **Circuit Breakers**: Automatic fallback and recovery mechanisms

## Implementation Phases

### Phase 1: Foundation (Sprint 1-2)
- [ ] Feature flag configuration system
- [ ] Enhanced Redis cache service with table-level support
- [ ] Basic synchronization mechanisms
- [ ] Unit and integration tests

### Phase 2: Strategy Implementation (Sprint 3-4)
- [ ] Redis-first and Cassandra-first strategy implementations
- [ ] Circuit breaker and resilience patterns
- [ ] Enhanced IQEService with strategy pattern
- [ ] Performance testing and optimization

### Phase 3: Monitoring and Operations (Sprint 5)
- [ ] Comprehensive monitoring and alerting
- [ ] Health checks and observability
- [ ] Documentation and runbooks
- [ ] Production deployment and validation

### Phase 4: Optimization (Sprint 6)
- [ ] Performance tuning based on production metrics
- [ ] Memory optimization and cache eviction policies
- [ ] Advanced synchronization patterns
- [ ] Capacity planning and scaling guidelines

## Story Points Estimation

**Story Points: 55**

### Breakdown:
- **Feature Flag System**: 8 points
- **Table-Level Caching Implementation**: 13 points (most complex)
- **Synchronization Mechanisms**: 10 points
- **Strategy Pattern and Service Enhancement**: 8 points
- **Resilience and Circuit Breakers**: 8 points
- **Monitoring and Observability**: 5 points
- **Testing Strategy**: 3 points

### Justification:
This is a large, complex enhancement involving:
- Complete redesign of caching architecture
- Multiple caching strategies with dynamic switching
- Complex synchronization between Redis and Cassandra
- Circuit breaker and resilience patterns
- Comprehensive monitoring and observability
- Backward compatibility requirements
- Performance optimization and memory management
- Extensive testing across multiple failure scenarios

The high point value reflects the architectural significance, the need for zero-downtime deployment, and the complexity of maintaining data consistency across distributed cache and database systems.

## Dependencies

- **Infrastructure Team**: Redis cluster setup and configuration
- **DevOps Team**: Feature flag service integration
- **Database Team**: Cassandra change data capture setup
- **Monitoring Team**: Metrics and alerting configuration
- **Architecture Review**: Approval for caching strategy changes

## Definition of Done

- [ ] All acceptance criteria met with comprehensive testing
- [ ] Feature flags operational with dynamic configuration
- [ ] Table-level caching implemented for all core tables
- [ ] Synchronization mechanisms working correctly
- [ ] Circuit breakers and resilience patterns operational
- [ ] Monitoring and alerting configured and validated
- [ ] Performance benchmarks meet or exceed current metrics
- [ ] Documentation updated (architecture, operations, troubleshooting)
- [ ] Code review completed and approved
- [ ] Production deployment successful with gradual rollout
- [ ] Post-deployment validation and performance monitoring

---

**Created by**: @innerlooptek-ship-it  
**Date**: August 29, 2025  
**Epic**: Enhanced Caching Architecture  
**Sprint**: TBD  
**Link to Devin run**: https://app.devin.ai/sessions/f067be736b084192b12fe36cca430fc4
