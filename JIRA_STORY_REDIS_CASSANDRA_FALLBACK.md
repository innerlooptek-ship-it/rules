# JIRA Story: Implement Redis Fallback Logic for Cassandra Outages

## Story Title
**Implement Redis Cache Fallback Logic to Serve Questionnaires During Cassandra Database Outages**

## Story Type
**Feature/Enhancement**

## Priority
**High**

## Labels
- `resilience-improvement`
- `cache-fallback`
- `database-availability`
- `redis-enhancement`
- `questionnaire-service`
- `high-availability`

---

## Problem Statement

The current IQE service implements a cache-first pattern where Redis is checked first, then falls back to Cassandra when cache misses occur. However, there is no reverse fallback mechanism to serve questionnaire data from Redis cache when Cassandra database becomes unavailable. This creates a single point of failure where questionnaire services become completely unavailable during Cassandra outages, impacting user experience and appointment scheduling flows.

## Current Redis Implementation Analysis

### Existing Cache Architecture
- **Redis Configuration**: `RedisConfigProperties` with baseUrl, timeouts, and cache type settings
- **Cache Service**: `RedisCacheService` handles GET/POST/DELETE operations via HTTP connector
- **Integration Pattern**: `IQEService.questionnaireByActionId()` implements cache-first pattern
- **Cache Population**: `IQERepoOrchestrator.insertQuestionsIntoDB()` writes to Redis after successful Cassandra operations

### Current Flow Pattern
```
1. Check Redis cache first (RedisCacheService.getDataFromRedis)
2. If cache miss → Query Cassandra repositories
3. If Cassandra success → Write to Redis cache
4. Return questionnaire data to client
```

### Current Limitations
- **No Cassandra Failure Handling**: When Cassandra is down, service fails completely
- **Cache Not Utilized During Outages**: Redis cache data is ignored when Cassandra is unavailable
- **Single Point of Failure**: Questionnaire service depends entirely on Cassandra availability
- **Poor User Experience**: Complete service unavailability during database maintenance or outages

## Proposed Solution: Redis Fallback Logic

### Enhanced Flow Pattern
```
1. Check Redis cache first (existing behavior)
2. If cache hit → Return cached data (existing behavior)
3. If cache miss → Attempt Cassandra query
4. If Cassandra success → Write to Redis and return data (existing behavior)
5. **NEW**: If Cassandra failure → Attempt Redis fallback with extended TTL search
6. **NEW**: If Redis fallback success → Return cached data with staleness indicator
7. **NEW**: If both Redis and Cassandra fail → Return graceful error with retry guidance
```

### Key Enhancement Areas

#### 1. Cassandra Failure Detection
- **Connection Timeout Handling**: Detect Cassandra connection failures
- **Query Timeout Management**: Handle slow/hanging Cassandra queries
- **Exception Classification**: Distinguish between temporary and permanent failures
- **Circuit Breaker Pattern**: Implement circuit breaker for Cassandra connections

#### 2. Redis Fallback Strategy
- **Extended Cache Search**: Search Redis with relaxed TTL constraints during outages
- **Stale Data Serving**: Serve slightly stale data with appropriate headers/indicators
- **Cache Warming**: Proactively refresh critical questionnaire data in Redis
- **Fallback Prioritization**: Prioritize most recent and frequently accessed questionnaires

#### 3. Data Freshness Management
- **Staleness Indicators**: Add metadata to indicate data freshness in responses
- **TTL Extension**: Temporarily extend Redis TTL during Cassandra outages
- **Cache Versioning**: Implement versioning to track data currency
- **Graceful Degradation**: Clearly communicate service limitations during fallback mode

## Technical Implementation Details

### Enhanced IQEService.questionnaireByActionId() Method

```java
public Mono<QuestionareRequest> questionnaireByActionId(String actionId, QuestionareRequest iqeOutPut) {
    return Mono.deferContextual(ctx -> 
        // Step 1: Check Redis cache (existing)
        redisCacheService.getDataFromRedis(IQE_QUESTIONNAIRE, actionId, eventMap)
            .flatMap(this::processRedisHit)
            .switchIfEmpty(
                // Step 2: Attempt Cassandra query with fallback
                queryCassandraWithFallback(actionId, iqeOutPut)
            )
    );
}

private Mono<QuestionareRequest> queryCassandraWithFallback(String actionId, QuestionareRequest iqeOutPut) {
    return queryCassandraRepositories(actionId, iqeOutPut)
        .onErrorResume(CassandraException.class, ex -> {
            log.warn("Cassandra unavailable for actionId: {}, attempting Redis fallback", actionId, ex);
            return attemptRedisFallback(actionId, iqeOutPut, ex);
        });
}

private Mono<QuestionareRequest> attemptRedisFallback(String actionId, QuestionareRequest iqeOutPut, Throwable cassandraError) {
    return redisCacheService.getDataFromRedisWithExtendedTTL(IQE_QUESTIONNAIRE, actionId, eventMap)
        .flatMap(cachedData -> {
            QuestionareRequest response = processRedisData(cachedData);
            response.setDataFreshness("STALE_FALLBACK");
            response.setFallbackReason("CASSANDRA_UNAVAILABLE");
            response.setLastUpdated(extractCacheTimestamp(cachedData));
            return Mono.just(response);
        })
        .switchIfEmpty(
            createGracefulErrorResponse(actionId, iqeOutPut, cassandraError)
        );
}
```

### Enhanced RedisCacheService Methods

```java
// New method for fallback with extended TTL search
public Mono<JsonNode> getDataFromRedisWithExtendedTTL(String type, String key, Map<String, String> eventMap) {
    // Implementation to search Redis with relaxed TTL constraints
    // Include stale data that might still be useful during outages
}

// Enhanced cache writing with extended TTL during outages
public Mono<Void> setDataToRedisWithExtendedTTL(String key, Object cacheObject, Map<String, String> eventMap, Duration extendedTTL) {
    // Implementation to write cache data with longer TTL during Cassandra outages
}

// Circuit breaker status checking
public boolean isCassandraCircuitOpen() {
    // Check if Cassandra circuit breaker is open
}
```

### Circuit Breaker Implementation

```java
@Component
public class CassandraCircuitBreaker {
    private final CircuitBreaker circuitBreaker;
    
    public <T> Mono<T> executeWithCircuitBreaker(Supplier<Mono<T>> cassandraOperation) {
        return Mono.fromCallable(() -> circuitBreaker.executeSupplier(cassandraOperation::get))
            .flatMap(result -> result)
            .onErrorResume(CircuitBreakerOpenException.class, ex -> 
                Mono.error(new CassandraUnavailableException("Circuit breaker open", ex))
            );
    }
}
```

## Acceptance Criteria

### 1. Cassandra Failure Detection
- [ ] Service detects Cassandra connection timeouts within 5 seconds
- [ ] Service distinguishes between temporary and permanent Cassandra failures
- [ ] Circuit breaker opens after 3 consecutive Cassandra failures
- [ ] Circuit breaker closes automatically after successful health check
- [ ] Proper logging and monitoring for Cassandra failure events

### 2. Redis Fallback Implementation
- [ ] Service attempts Redis fallback when Cassandra queries fail
- [ ] Redis fallback searches with extended TTL constraints (up to 24 hours stale)
- [ ] Fallback responses include data freshness indicators
- [ ] Service serves stale data with appropriate HTTP headers
- [ ] Fallback mode activates within 100ms of Cassandra failure detection

### 3. Data Freshness Management
- [ ] Responses include `dataFreshness` field indicating cache status
- [ ] Stale data responses include `lastUpdated` timestamp
- [ ] Cache entries include versioning for data currency tracking
- [ ] TTL extension mechanism during Cassandra outages
- [ ] Automatic cache refresh when Cassandra becomes available

### 4. Error Handling and Graceful Degradation
- [ ] Graceful error responses when both Redis and Cassandra fail
- [ ] Clear error messages indicating service limitations during outages
- [ ] Retry guidance for clients during fallback mode
- [ ] Proper HTTP status codes for different failure scenarios
- [ ] Fallback mode indicators in response headers

### 5. Performance and Monitoring
- [ ] Fallback responses served within 200ms
- [ ] Monitoring dashboards for cache hit/miss rates during outages
- [ ] Alerting for Cassandra outages and fallback mode activation
- [ ] Metrics for data staleness and fallback success rates
- [ ] Performance benchmarks maintained during fallback operations

### 6. Configuration and Feature Flags
- [ ] Feature flag to enable/disable Redis fallback functionality
- [ ] Configurable TTL extension periods for different questionnaire types
- [ ] Configurable circuit breaker thresholds and timeouts
- [ ] Environment-specific fallback behavior configuration
- [ ] Runtime configuration updates without service restart

### 7. Testing and Validation
- [ ] Unit tests for all fallback scenarios and edge cases
- [ ] Integration tests simulating Cassandra outages
- [ ] Performance tests validating fallback response times
- [ ] Chaos engineering tests for resilience validation
- [ ] End-to-end testing with dhs-scheduling-app integration

## Benefits

### Availability Improvements
1. **High Availability**: Service remains operational during Cassandra outages
2. **Reduced Downtime**: Questionnaire services available even during database maintenance
3. **Improved User Experience**: Seamless questionnaire access during infrastructure issues
4. **Business Continuity**: Appointment scheduling flows continue during outages

### Operational Benefits
1. **Reduced Support Burden**: Fewer user complaints during database maintenance
2. **Maintenance Flexibility**: Database maintenance can be performed with minimal impact
3. **Incident Response**: Faster recovery from database-related incidents
4. **Cost Optimization**: Reduced need for expensive high-availability database setups

### Technical Benefits
1. **Resilience**: Service becomes more fault-tolerant and robust
2. **Performance**: Faster responses during peak load when serving from cache
3. **Scalability**: Reduced database load through intelligent caching
4. **Monitoring**: Better visibility into service health and performance

## Risks and Mitigation

### Technical Risks
- **Stale Data Serving**: Risk of serving outdated questionnaire information
  - *Mitigation*: Clear staleness indicators and configurable TTL limits
- **Cache Inconsistency**: Risk of cache and database getting out of sync
  - *Mitigation*: Automatic cache invalidation and refresh mechanisms
- **Increased Complexity**: Additional code paths and error handling
  - *Mitigation*: Comprehensive testing and gradual rollout with feature flags

### Operational Risks
- **False Positives**: Circuit breaker triggering unnecessarily
  - *Mitigation*: Careful tuning of circuit breaker thresholds and monitoring
- **Cache Pollution**: Stale data remaining in cache after outages
  - *Mitigation*: Automatic cache cleanup and refresh procedures
- **Monitoring Overhead**: Additional metrics and alerting complexity
  - *Mitigation*: Streamlined monitoring dashboards and alert consolidation

## Dependencies

### Technical Dependencies
- Circuit breaker library (Resilience4j or similar)
- Enhanced Redis client capabilities for TTL management
- Monitoring and alerting infrastructure updates
- Feature flag management system

### Team Dependencies
- Infrastructure team for Redis capacity planning
- DevOps team for monitoring and alerting setup
- QA team for comprehensive testing strategy
- Architecture team for design review and approval

## Story Points Estimation

**Story Points: 21**

### Breakdown:
- **Cassandra Failure Detection and Circuit Breaker**: 5 points
  - Connection timeout handling (2 points)
  - Circuit breaker implementation (3 points)

- **Redis Fallback Logic Implementation**: 8 points
  - Enhanced IQEService fallback methods (4 points)
  - RedisCacheService extended TTL methods (2 points)
  - Data freshness and staleness management (2 points)

- **Error Handling and Graceful Degradation**: 3 points
  - Graceful error responses (1 point)
  - Response headers and indicators (2 points)

- **Configuration and Feature Flags**: 2 points
  - Feature flag implementation (1 point)
  - Configuration management (1 point)

- **Testing and Validation**: 3 points
  - Unit and integration tests (2 points)
  - Performance and chaos testing (1 point)

### Justification:
This is a significant resilience enhancement involving:
- Complex error handling and fallback logic
- Circuit breaker pattern implementation
- Cache management with TTL extensions
- Data freshness tracking and staleness indicators
- Comprehensive testing across multiple failure scenarios
- Integration with existing Redis and Cassandra infrastructure
- Performance optimization during fallback operations

The high point value reflects the critical nature of availability improvements and the complexity of implementing robust fallback mechanisms while maintaining data integrity and performance.

## Implementation Phases

### Phase 1: Foundation (Sprint 1)
- Implement Cassandra failure detection and classification
- Add circuit breaker pattern for Cassandra connections
- Create basic Redis fallback structure
- Unit tests for failure detection logic

### Phase 2: Core Fallback Logic (Sprint 2)
- Implement enhanced IQEService fallback methods
- Add RedisCacheService extended TTL capabilities
- Implement data freshness and staleness management
- Integration tests for fallback scenarios

### Phase 3: Error Handling and UX (Sprint 3)
- Implement graceful error responses and indicators
- Add response headers for fallback mode
- Create monitoring and alerting for fallback events
- Performance testing and optimization

### Phase 4: Configuration and Rollout (Sprint 4)
- Implement feature flags and configuration management
- Comprehensive testing including chaos engineering
- Production rollout with gradual feature flag activation
- Documentation and operational runbooks

## Monitoring and Alerting Requirements

### Key Metrics
- **Cache Hit Rate**: Redis cache effectiveness during normal and fallback operations
- **Fallback Activation Rate**: Frequency of Redis fallback usage
- **Data Staleness**: Age of data served during fallback mode
- **Response Times**: Performance during normal vs fallback operations
- **Error Rates**: Failure rates for different scenarios

### Critical Alerts
- **Cassandra Outage**: Immediate alert when Cassandra becomes unavailable
- **Fallback Mode Activation**: Alert when service enters Redis fallback mode
- **Cache Miss During Outage**: Alert when both Redis and Cassandra fail
- **Extended Fallback Duration**: Alert for prolonged fallback operations
- **Performance Degradation**: Alert for response time increases during fallback

## Definition of Done

- [ ] Cassandra failure detection implemented with circuit breaker pattern
- [ ] Redis fallback logic serves questionnaires during Cassandra outages
- [ ] Data freshness indicators included in all fallback responses
- [ ] Graceful error handling for all failure scenarios
- [ ] Feature flags and configuration management implemented
- [ ] Comprehensive test coverage (unit, integration, performance, chaos)
- [ ] Monitoring and alerting configured for all fallback scenarios
- [ ] Performance benchmarks maintained during fallback operations
- [ ] Documentation updated (API docs, operational runbooks, troubleshooting guides)
- [ ] Code review completed and approved
- [ ] Architecture review board approval obtained
- [ ] Successful production deployment with feature flag rollout
- [ ] Validation of seamless user experience during simulated outages

---

**Created by**: @innerlooptek-ship-it  
**Date**: August 22, 2025  
**Epic**: High Availability and Resilience Improvements  
**Sprint**: TBD  
**Link to Devin run**: https://app.devin.ai/sessions/f067be736b084192b12fe36cca430fc4

## Technical Architecture Diagrams

### Current State: Cache-First with Single Fallback
```
Client Request
    ↓
IQEService.questionnaireByActionId()
    ↓
1. Check Redis Cache
    ├── Cache Hit → Return Data
    └── Cache Miss
        ↓
2. Query Cassandra
    ├── Success → Write to Redis → Return Data
    └── Failure → ERROR (Service Unavailable)
```

### Proposed Future State: Bidirectional Fallback
```
Client Request
    ↓
IQEService.questionnaireByActionId()
    ↓
1. Check Redis Cache
    ├── Cache Hit → Return Fresh Data
    └── Cache Miss
        ↓
2. Query Cassandra (with Circuit Breaker)
    ├── Success → Write to Redis → Return Data
    └── Failure (Circuit Open)
        ↓
3. Redis Fallback (Extended TTL)
    ├── Stale Data Found → Return with Staleness Indicators
    └── No Data Found → Graceful Error Response
```

This enhancement transforms the IQE service from a single-point-of-failure architecture to a resilient, high-availability system that can serve questionnaire data even during database outages, ensuring seamless user experience and business continuity.
