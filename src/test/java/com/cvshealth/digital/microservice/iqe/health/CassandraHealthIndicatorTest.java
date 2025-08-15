package com.cvshealth.digital.microservice.iqe.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.Status;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlOperations;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CassandraHealthIndicatorTest {

    @Mock
    private CassandraTemplate cassandraTemplate;
    
    @Mock
    private CqlOperations cqlOperations;
    
    private CassandraHealthIndicator healthIndicator;
    
    @BeforeEach
    void setUp() {
        healthIndicator = new CassandraHealthIndicator(cassandraTemplate);
        when(cassandraTemplate.getCqlOperations()).thenReturn(cqlOperations);
    }
    
    @Test
    void health_WhenCassandraAccessible_ShouldReturnUp() {
        when(cqlOperations.execute(anyString())).thenReturn(null);
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("status"));
        assertTrue(health.getDetails().containsKey("lastCheck"));
        verify(cqlOperations).execute("SELECT now() FROM system.local");
    }
    
    @Test
    void health_WhenCassandraNotAccessible_ShouldReturnDown() {
        when(cqlOperations.execute(anyString())).thenThrow(new RuntimeException("Connection failed"));
        
        Health health = healthIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
        assertTrue(health.getDetails().containsKey("lastCheck"));
        assertTrue(health.getDetails().containsKey("errorClass"));
        verify(cqlOperations).execute("SELECT now() FROM system.local");
    }
    
    @Test
    void isHealthy_WhenCassandraAccessible_ShouldReturnTrue() {
        when(cqlOperations.execute(anyString())).thenReturn(null);
        
        StepVerifier.create(healthIndicator.isHealthy())
            .expectNext(true)
            .verifyComplete();
    }
    
    @Test
    void isHealthy_WhenCassandraNotAccessible_ShouldReturnFalse() {
        when(cqlOperations.execute(anyString())).thenThrow(new RuntimeException("Connection failed"));
        
        StepVerifier.create(healthIndicator.isHealthy())
            .expectNext(false)
            .verifyComplete();
    }
    
    @Test
    void forceHealthCheck_ShouldResetLastCheckTime() {
        healthIndicator.forceHealthCheck();
        
        when(cqlOperations.execute(anyString())).thenReturn(null);
        
        StepVerifier.create(healthIndicator.isHealthy())
            .expectNext(true)
            .verifyComplete();
        
        verify(cqlOperations).execute("SELECT now() FROM system.local");
    }
}
