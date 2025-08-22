# JIRA Story: Consolidate IMZ Questionnaire API Calls into IQE

## Story Title
**Analysis and Design: Consolidate IMZ Questionnaire API Calls into IQE for Unified Questionnaire Management**

## Story Type
**Analysis/Epic**

## Priority
**High**

## Labels
- `architecture-analysis`
- `api-consolidation`
- `questionnaire-management`
- `iqe-enhancement`
- `imz-migration`
- `single-source-of-truth`

---

## Problem Statement

The current `SchedulingController` in the dhs-scheduling-iqe-app handles questionnaire requests through multiple pathways:
1. **Active IQE Integration**: Uses `schedulingService.getIQEQuestionnaire()` for most flows (TEST_TREAT, MHC, MC_CORE, EVC_B2B, VM, VACCINE)
2. **Commented IMZ Integration**: Contains commented-out IMZ service calls for RX_IMZ LOB questionnaire handling

This dual-pathway approach creates architectural complexity, maintenance overhead, and prevents IQE from being the true single source of truth for all questionnaire data across different Lines of Business (LOBs).

## Current Architecture Analysis

### SchedulingController Flow Analysis

The `getSchedulingQuestionnaire` method in `SchedulingController.java` handles multiple flow types:

#### Active IQE Flows
- **TEST_TREAT**: Direct IQE questionnaire retrieval
- **MHC/EVC_B2B**: MHC scheduling questions via IQE
- **MC_CORE**: Eligibility and legal questions via IQE with complex business logic
- **VM (Vaccine Management)**: IQE questionnaire integration
- **VACCINE**: Clinic-based vaccine questionnaires via IQE

#### Commented IMZ Integration Points
```java
// Lines 208-226: Generic IMZ questionnaire call when LOB is null
// if (Objects.isNull(questionnaireInput.getLob())) {
//     return schedulingService.getImzQuestionnarie(questionnaireInput, headerMap, eventMap)

// Lines 263-283: RX_IMZ LOB specific questionnaire handling
// } else if (questionnaireInput.getLob().equals(RX_IMZ)) {
//     String paddedNumber = String.format("%05d", Integer.parseInt(questionnaireInput.getStoreId()));
//     questionnaireInput.setStoreId(paddedNumber);
//     return schedulingService.getImzQuestionnarie(questionnaireInput, headerMap, eventMap)
```

### Current IQE Service Architecture

#### IQE Integration Pattern
- **Service Layer**: `SchedulingService.getIQEQuestionnaire()` orchestrates questionnaire retrieval
- **External API Calls**: `IQEMcCoreQuestionnarieService` uses WebClient to call external IQE APIs
- **Request Structure**: `IQEMcCoreQuestionnarieRequest` with flow, context, reasonId, state, age, modality
- **Response Processing**: Complex data transformation and question processing logic

#### Cassandra Data Model
Current IQE uses Cassandra repositories for questionnaire storage:
- **QuestionsRepository**: Stores question definitions by action_id
- **ActionsRepository**: Manages questionnaire actions and flows
- **AnswerOptionsRepository**: Handles answer option configurations
- **QuestionnaireDetailsRepository**: Additional questionnaire metadata
- **RulesByFlowRepository**: Flow-specific business rules

### IMZ Questionnaire Context Analysis

#### IMZ-Specific Questionnaire Contexts
From `QuestionnaireContextEnum.java`, IMZ supports these contexts:
- `IMZ_ELIGIBILITY_QUESTION`: Patient eligibility screening
- `IMZ_SCREENING_QUESTION`: Health screening questionnaires
- `IMZ_LEGAL_QUESTION`: Legal consent and authorization
- `IMZ_ADDITIONAL_QUESTION`: Supplementary health questions
- `IMZ_CANCEL_REASON`: Appointment cancellation reasons

#### IMZ Data Structure Analysis
`ImzQuestionnarieResponse.java` reveals IMZ's nested data structure:
```java
- ImmunizationQuestionsData
  - GetImmunizationQuestions[]
    - context: String
    - questions: Questions[]
      - id, text, answerType, errorMessage
      - vaccineRef: VaccineRef[]
      - answerOptions: AnswerOption[]
        - relatedQuestions: RelatedQuestions[] (nested structure)
```

This differs from IQE's normalized structure, requiring data transformation during consolidation.

## Proposed Solution: Unified IQE API Architecture

### Phase 1: IQE API Enhancement
1. **Extend IQE API** to support IMZ questionnaire contexts
2. **Add IMZ Context Support** in `IQEMcCoreQuestionnarieService`
3. **Data Model Extension** to handle IMZ-specific questionnaire types
4. **Business Rules Integration** for IMZ flow-specific logic

### Phase 2: Data Migration Strategy
1. **IMZ Questionnaire Data Migration** to IQE Cassandra storage
2. **Context Mapping** between IMZ and IQE questionnaire contexts
3. **Data Structure Normalization** from IMZ nested format to IQE format
4. **Validation and Testing** of migrated questionnaire data

### Phase 3: SchedulingController Consolidation
1. **Remove Commented IMZ Calls** from `SchedulingController.java`
2. **Extend IQE Integration** to handle RX_IMZ LOB flows
3. **Unified Request Routing** through `schedulingService.getIQEQuestionnaire()`
4. **Error Handling Consolidation** for all questionnaire flows

### Phase 4: dhs-scheduling-app Integration
1. **GraphQL Schema Updates** to support unified questionnaire API
2. **Client-Side Integration Testing** with consolidated API
3. **Performance Optimization** for unified questionnaire retrieval
4. **Monitoring and Alerting** for consolidated service

## Technical Implementation Details

### New IQE API Endpoints
```
POST /api/v1/iqe/questionnaire/unified
- Input: Enhanced IQEQuestionnaireRequest supporting IMZ contexts
- Output: Unified QuestionnaireResponse format
- Supports: All existing IQE contexts + IMZ contexts

GET /api/v1/iqe/questionnaire/contexts
- Returns: Available questionnaire contexts for all LOBs
- Includes: IQE contexts + IMZ contexts with metadata
```

### Enhanced Request Model
```java
IQEUnifiedQuestionnaireRequest {
    String flow;                    // TEST_TREAT, MHC, MC_CORE, etc.
    String lob;                     // CLINIC, RX_IMZ, etc.
    String context;                 // IQE or IMZ context enum
    Integer reasonId;
    Integer reasonMappingId;
    String state;
    Integer age;
    String modality;
    String storeId;                 // For RX_IMZ flows
    Map<String, Object> metadata;   // Extensible for future LOBs
}
```

### Data Transformation Strategy
1. **IMZ to IQE Mapping**: Transform nested IMZ structure to normalized IQE format
2. **Context Translation**: Map IMZ contexts to equivalent IQE contexts where possible
3. **Business Rule Preservation**: Maintain IMZ-specific business logic in IQE
4. **Backward Compatibility**: Ensure existing IQE clients continue to work

## Acceptance Criteria

### 1. IQE API Enhancement
- [ ] IQE API supports all IMZ questionnaire contexts (IMZ_ELIGIBILITY_QUESTION, IMZ_SCREENING_QUESTION, etc.)
- [ ] Enhanced `IQEMcCoreQuestionnarieService` handles unified questionnaire requests
- [ ] New unified request/response models support both IQE and IMZ data structures
- [ ] Cassandra data model extended to store IMZ questionnaire data

### 2. Data Migration and Consolidation
- [ ] IMZ questionnaire data successfully migrated to IQE Cassandra storage
- [ ] Data transformation logic converts IMZ nested format to IQE normalized format
- [ ] All IMZ questionnaire contexts properly mapped and functional
- [ ] Business rules for IMZ flows preserved and integrated into IQE

### 3. SchedulingController Refactoring
- [ ] Commented IMZ service calls removed from `SchedulingController.java`
- [ ] RX_IMZ LOB flow re-enabled through unified IQE API
- [ ] All questionnaire flows (IQE + IMZ) route through `schedulingService.getIQEQuestionnaire()`
- [ ] Error handling consolidated for unified questionnaire service

### 4. Integration and Testing
- [ ] dhs-scheduling-app successfully integrates with unified IQE API
- [ ] GraphQL schema updated to support consolidated questionnaire endpoints
- [ ] Comprehensive integration tests cover all flow types and LOBs
- [ ] Performance benchmarks meet or exceed current response times

### 5. Operational Requirements
- [ ] Monitoring and alerting configured for unified questionnaire service
- [ ] Documentation updated for new unified API endpoints
- [ ] Rollback strategy defined in case of integration issues
- [ ] Feature flags implemented for gradual rollout

## Benefits

### Architectural Benefits
1. **Single Source of Truth**: IQE becomes the unified questionnaire service for all LOBs
2. **Simplified Architecture**: Eliminates dual-pathway questionnaire handling
3. **Reduced Complexity**: Single API endpoint for all questionnaire flows
4. **Improved Maintainability**: Consolidated questionnaire logic and data management

### Operational Benefits
1. **Reduced Maintenance Overhead**: Single service to maintain instead of multiple integrations
2. **Improved Performance**: Eliminates redundant external service calls
3. **Enhanced Monitoring**: Unified metrics and alerting for all questionnaire flows
4. **Better Scalability**: Cassandra-based storage scales better than external API dependencies

### Development Benefits
1. **Unified Development Experience**: Single API for all questionnaire integrations
2. **Consistent Data Models**: Normalized questionnaire data structure across LOBs
3. **Simplified Testing**: Single integration point for all questionnaire flows
4. **Future Extensibility**: Easy to add new LOBs and questionnaire types

## Risks and Mitigation

### Technical Risks
- **Data Migration Complexity**: IMZ to IQE data structure transformation
  - *Mitigation*: Comprehensive data mapping and validation testing
- **Performance Impact**: Potential latency from data consolidation
  - *Mitigation*: Performance testing and optimization during migration
- **Integration Complexity**: Multiple client applications depend on questionnaire APIs
  - *Mitigation*: Phased rollout with feature flags and rollback capability

### Operational Risks
- **Service Downtime**: Risk during migration and consolidation
  - *Mitigation*: Blue-green deployment strategy with zero-downtime migration
- **Data Loss**: Risk during IMZ data migration to Cassandra
  - *Mitigation*: Complete data backup and validation before migration
- **Client Compatibility**: Existing dhs-scheduling-app integrations may break
  - *Mitigation*: Backward compatibility testing and gradual client migration

## Dependencies

### Technical Dependencies
- IQE service team for API enhancement and data model changes
- Cassandra infrastructure team for storage capacity planning
- dhs-scheduling-app team for client-side integration updates
- DevOps team for deployment pipeline and monitoring setup

### Business Dependencies
- Product team approval for unified questionnaire experience
- Compliance team review for IMZ data migration and storage
- QA team for comprehensive integration testing across all flows

## Story Points Estimation

**Story Points: 34**

### Breakdown:
- **IQE API Enhancement**: 13 points
  - New unified API endpoints (5 points)
  - Enhanced service layer for IMZ support (5 points)
  - Cassandra data model extension (3 points)

- **Data Migration Strategy**: 8 points
  - IMZ data migration to Cassandra (5 points)
  - Data transformation and validation (3 points)

- **SchedulingController Consolidation**: 5 points
  - Remove commented IMZ calls (2 points)
  - Unified request routing implementation (3 points)

- **Integration and Testing**: 8 points
  - dhs-scheduling-app integration updates (3 points)
  - Comprehensive testing across all flows (3 points)
  - Performance testing and optimization (2 points)

### Justification:
This is a large, complex architectural consolidation involving:
- Multiple service integrations (IQE + IMZ)
- Data migration from external APIs to Cassandra storage
- Complex data structure transformation (nested IMZ to normalized IQE)
- Multiple questionnaire contexts and flow types
- Critical dhs-scheduling-app integration dependencies
- Comprehensive testing across multiple LOBs and flow types
- Technology stack complexity (Spring Boot 3.4.7, Cassandra Reactive, GraphQL)

The high point value reflects the architectural significance, data migration complexity, and the need to maintain 100% backward compatibility while consolidating multiple questionnaire services into a single source of truth.

## Implementation Phases

### Phase 1: Analysis and Design (Sprint 1)
- Complete technical analysis of IMZ integration requirements
- Design unified IQE API specification
- Plan data migration strategy and validation approach
- Create detailed implementation roadmap

### Phase 2: IQE API Enhancement (Sprint 2-3)
- Implement unified questionnaire API endpoints
- Extend Cassandra data model for IMZ support
- Develop data transformation logic for IMZ to IQE format
- Create comprehensive unit and integration tests

### Phase 3: Data Migration (Sprint 4)
- Migrate IMZ questionnaire data to IQE Cassandra storage
- Validate data integrity and business rule preservation
- Performance testing of consolidated questionnaire service
- Rollback strategy testing and validation

### Phase 4: SchedulingController Integration (Sprint 5)
- Remove commented IMZ calls from SchedulingController
- Implement unified questionnaire routing through IQE
- Update error handling and logging for consolidated flows
- Integration testing with dhs-scheduling-app

### Phase 5: Production Rollout (Sprint 6)
- Feature flag implementation for gradual rollout
- Production deployment with monitoring and alerting
- Client migration and validation
- Performance monitoring and optimization

## Definition of Done

- [ ] IQE API enhanced to support all IMZ questionnaire contexts
- [ ] IMZ questionnaire data successfully migrated to IQE Cassandra storage
- [ ] SchedulingController consolidated to use unified IQE API for all flows
- [ ] dhs-scheduling-app successfully integrated with consolidated API
- [ ] All existing functionality preserved with no breaking changes
- [ ] Performance benchmarks meet or exceed current metrics
- [ ] Comprehensive test coverage (unit, integration, performance)
- [ ] Documentation updated (API docs, architecture diagrams, runbooks)
- [ ] Monitoring and alerting configured for unified service
- [ ] Code review completed and approved
- [ ] Architecture review board approval obtained
- [ ] Successful production deployment with feature flag rollout

---

**Created by**: @innerlooptek-ship-it  
**Date**: August 22, 2025  
**Epic**: Questionnaire Service Consolidation  
**Sprint**: TBD  
**Link to Devin run**: https://app.devin.ai/sessions/f067be736b084192b12fe36cca430fc4

## Technical Architecture Diagrams

### Current State Architecture
```
dhs-scheduling-app
    ↓ (GraphQL)
SchedulingController
    ├── schedulingService.getIQEQuestionnaire() → IQE API (Active)
    └── schedulingService.getImzQuestionnarie() → IMZ API (Commented)
```

### Proposed Future State Architecture
```
dhs-scheduling-app
    ↓ (GraphQL)
SchedulingController
    ↓
schedulingService.getIQEQuestionnaire()
    ↓
Unified IQE API
    ↓
Cassandra (IQE + IMZ Data)
```

This consolidation eliminates the dual-pathway complexity and establishes IQE as the single source of truth for all questionnaire data across different LOBs and flow types.
