package com.cvshealth.digital.microservice.iqe.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    @Container
    static CassandraContainer<?> cassandraContainer = new CassandraContainer<>(DockerImageName.parse("cassandra:4.1"))
            .withExposedPorts(9042)
            .withInitScript("cassandra/test_schema.cql");
    
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", cassandraContainer::getHost);
        registry.add("spring.cassandra.port", cassandraContainer::getFirstMappedPort);
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "iqe_test");
    }
}
