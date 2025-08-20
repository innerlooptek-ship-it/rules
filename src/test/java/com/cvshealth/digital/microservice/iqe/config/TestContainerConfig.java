package com.cvshealth.digital.microservice.iqe.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    @Bean
    @ServiceConnection
    public CassandraContainer<?> cassandraContainer() {
        return new CassandraContainer<>(DockerImageName.parse("cassandra:4.1"))
                .withExposedPorts(9042)
                .withInitScript("cassandra/test_schema.cql");
    }

}
