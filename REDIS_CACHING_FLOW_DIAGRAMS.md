# Redis Caching Strategy: Before vs After Flow Diagrams

## Current Implementation (BEFORE)

```mermaid
graph TB
    subgraph "Current Redis Caching Flow"
        User[User Request]:::user
        Controller[IQEController]:::controller
        Service[IQEService]:::service
        
        subgraph "Current Caching Logic"
            RedisCheck{Redis Cache Check}:::decision
            CassandraQuery[Cassandra Query]:::cassandra
            RedisStore[Store in Redis]:::redis
        end
        
        subgraph "Data Stores"
            RedisOld[Redis<br/>Single Key per ActionId<br/>ACT_001_VACCINE → Full Object]:::redis
            CassandraOld[Cassandra<br/>5 Tables<br/>actions, questions, etc.]:::cassandra
        end
    end
    
    User --> Controller
    Controller --> Service
    Service --> RedisCheck
    
    RedisCheck -->|Cache Hit| Service
    RedisCheck -->|Cache Miss| CassandraQuery
    CassandraQuery --> RedisStore
    RedisStore --> Service
    
    RedisCheck -.-> RedisOld
    CassandraQuery -.-> CassandraOld
    RedisStore -.-> RedisOld
    
    classDef user fill:#e1f5fe
    classDef controller fill:#f3e5f5
    classDef service fill:#e8f5e8
    classDef decision fill:#fff3e0
    classDef redis fill:#ffebee
    classDef cassandra fill:#e3f2fd
```

### Current Implementation Issues:
- ❌ **Limited Fallback**: Only caches specific actionIds that have been requested
- ❌ **No Complete Data**: If Cassandra is down, new actionIds cannot be retrieved
- ❌ **Single Point of Failure**: No circuit breaker or resilience patterns
- ❌ **Basic Caching**: Simple key-value with no optimization

---

## Proposed Implementation (AFTER)

```mermaid
graph TB
    subgraph "Enhanced Redis Caching Flow"
        User[User Request]:::user
        Controller[IQEController]:::controller
        
        subgraph "Enhanced Service Layer"
            ResilientService[ResilientCacheService<br/>Circuit Breaker]:::resilient
            StrategyFactory[CachingStrategyFactory]:::factory
            
            subgraph "Strategy Pattern"
                RedisFirst[RedisFirstStrategy<br/>Redis → Cassandra]:::strategy
                CassandraFirst[CassandraFirstStrategy<br/>Cassandra → Redis]:::strategy
            end
        end
        
        subgraph "Enhanced Caching Services"
            EnhancedRedis[EnhancedRedisCacheService<br/>Selective Table Caching]:::enhanced
            CassandraService[CassandraService<br/>Database Abstraction]:::cassandra
            WarmupService[CacheWarmupService<br/>Bulk Data Loading]:::warmup
        end
        
        subgraph "Data Stores with Selective Caching"
            RedisNew[Redis - Selective Keys<br/>questionnaire:action:ACT_001<br/>questionnaire:questions:ACT_001<br/>questionnaire:answers:ACT_001<br/>questionnaire:details:ACT_001]:::redis
            CassandraNew[Cassandra<br/>5 Tables<br/>actions, questions, etc.]:::cassandra
        end
        
        subgraph "Startup Process"
            AppStart[Application Startup]:::startup
            BulkLoad[Bulk Load ALL ActionIds<br/>from Cassandra to Redis]:::warmup
        end
    end
    
    User --> Controller
    Controller --> ResilientService
    ResilientService --> StrategyFactory
    
    StrategyFactory -->|Config: redis-first| RedisFirst
    StrategyFactory -->|Config: cassandra-first| CassandraFirst
    
    RedisFirst --> EnhancedRedis
    RedisFirst --> CassandraService
    CassandraFirst --> EnhancedRedis
    CassandraFirst --> CassandraService
    
    EnhancedRedis -.-> RedisNew
    CassandraService -.-> CassandraNew
    
    AppStart --> WarmupService
    WarmupService --> BulkLoad
    BulkLoad --> RedisNew
    BulkLoad -.-> CassandraNew
    
    classDef user fill:#e1f5fe
    classDef controller fill:#f3e5f5
    classDef resilient fill:#e8f5e8
    classDef factory fill:#fff3e0
    classDef strategy fill:#f1f8e9
    classDef enhanced fill:#fce4ec
    classDef cassandra fill:#e3f2fd
    classDef redis fill:#ffebee
    classDef warmup fill:#f3e5f5
    classDef startup fill:#e0f2f1
```

### Enhanced Implementation Benefits:
- ✅ **Complete Data Availability**: Cache warmup loads ALL actionIds into Redis
- ✅ **Seamless Fallback**: Circuit breaker automatically handles service outages
- ✅ **Configurable Strategies**: Feature flags switch between Redis-first/Cassandra-first
- ✅ **Memory Efficient**: Selective caching with optimized key patterns
- ✅ **High Availability**: Resilient patterns with automatic failover

---

## Detailed Read Flow Comparison

### BEFORE: Simple Cache-Aside Pattern
```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant S as Service
    participant R as Redis
    participant DB as Cassandra
    
    U->>C: GET /questionnaire/ACT_001
    C->>S: getQuestionnaire(ACT_001)
    S->>R: GET ACT_001
    
    alt Cache Hit
        R-->>S: Return cached data
        S-->>C: Questionnaire data
    else Cache Miss
        R-->>S: null
        S->>DB: Query all tables for ACT_001
        DB-->>S: Raw data
        S->>R: SET ACT_001 = processed data
        S-->>C: Questionnaire data
    end
    
    C-->>U: Response
    
    Note over S,DB: ❌ If Cassandra down + cache miss = FAILURE
```

### AFTER: Resilient Multi-Strategy Pattern
```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant RS as ResilientService
    participant SF as StrategyFactory
    participant RFS as RedisFirstStrategy
    participant ER as EnhancedRedisService
    participant CS as CassandraService
    participant R as Redis
    participant DB as Cassandra
    
    U->>C: GET /questionnaire/ACT_001
    C->>RS: getQuestionnaireWithResilience(ACT_001)
    RS->>SF: getStrategy(redis-first)
    SF-->>RS: RedisFirstStrategy
    RS->>RFS: getQuestionnaire(ACT_001)
    
    RFS->>ER: getFromTableCache(ACT_001)
    ER->>R: GET questionnaire:questions:ACT_001
    ER->>R: GET questionnaire:action:ACT_001
    ER->>R: GET questionnaire:answers:ACT_001
    
    alt Redis Cache Hit
        R-->>ER: All data found
        ER-->>RFS: Complete questionnaire
        RFS-->>RS: Success
    else Redis Cache Miss
        R-->>ER: Some/no data found
        ER-->>RFS: Incomplete/empty
        RFS->>CS: getQuestionnaire(ACT_001)
        CS->>DB: Query tables
        
        alt Cassandra Available
            DB-->>CS: Raw data
            CS-->>RFS: Questionnaire
            RFS->>ER: cacheQuestionnaire(data)
            ER->>R: SET selective keys
            RFS-->>RS: Success
        else Cassandra Down (Circuit Open)
            Note over CS,DB: Circuit breaker detects failure
            CS-->>RFS: ServiceUnavailableException
            RFS->>ER: getFromTableCache(ACT_001) [retry]
            ER-->>RFS: Cached data (from warmup)
            RFS-->>RS: Success with cached data
        end
    end
    
    RS-->>C: Questionnaire data
    C-->>U: Response
    
    Note over RS,R: ✅ Always serves data via warmup cache or fallback
```

---

## Cache Warmup Process Detail

```mermaid
graph TB
    subgraph "Cache Warmup Flow"
        Start[Application Startup]:::startup
        WarmupService[CacheWarmupService]:::service
        
        subgraph "Bulk Loading Process"
            GetAllActions[Get All ActionIds<br/>from Cassandra]:::query
            BatchProcess[Process in Batches<br/>Size: 50]:::batch
            LoadQuestionnaire[Load Each Questionnaire<br/>from Cassandra]:::load
            CacheSelective[Cache with Selective Keys<br/>questionnaire:*:{actionId}]:::cache
        end
        
        subgraph "Result"
            CompleteCache[Redis Contains<br/>ALL Questionnaire Data]:::result
        end
    end
    
    Start --> WarmupService
    WarmupService --> GetAllActions
    GetAllActions --> BatchProcess
    BatchProcess --> LoadQuestionnaire
    LoadQuestionnaire --> CacheSelective
    CacheSelective --> CompleteCache
    
    classDef startup fill:#e0f2f1
    classDef service fill:#e8f5e8
    classDef query fill:#e3f2fd
    classDef batch fill:#fff3e0
    classDef load fill:#f3e5f5
    classDef cache fill:#ffebee
    classDef result fill:#e1f5fe
```

---

## Key Differences Summary

| Aspect | BEFORE (Current) | AFTER (Proposed) |
|--------|------------------|------------------|
| **Data Availability** | ❌ Only requested actionIds cached | ✅ ALL actionIds cached via warmup |
| **Fallback Strategy** | ❌ Simple cache-aside pattern | ✅ Circuit breaker with resilient fallback |
| **Configuration** | ❌ Static Redis flag | ✅ Dynamic strategy switching |
| **Memory Usage** | ❌ Single large objects per key | ✅ Selective table-level caching |
| **High Availability** | ❌ Fails when Cassandra down | ✅ Serves from Redis cache always |
| **Performance** | ❌ Cold cache performance issues | ✅ Warm cache with sub-ms reads |
| **Monitoring** | ❌ Basic Redis connectivity | ✅ Comprehensive metrics & health checks |

## Configuration Example

```yaml
# BEFORE
service:
  redis-cache:
    redisFlag: false
    cacheType: genericcache_1h_noextend

# AFTER  
service:
  redis-cache:
    enabled: true
    strategy: "redis-first"  # or "cassandra-first"
    table-caching:
      warmup-enabled: true
      warmup-batch-size: 50
    read-optimization:
      prefer-redis: true
      seamless-fallback: true
```

This transformation ensures Redis serves as the primary data store for reads with complete data availability, even when Cassandra is unavailable.
