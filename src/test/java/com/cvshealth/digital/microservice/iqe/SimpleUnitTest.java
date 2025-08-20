package com.cvshealth.digital.microservice.iqe;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleUnitTest {
    
    @Test
    void shouldPassBasicTest() {
        String result = "test";
        assertThat(result).isEqualTo("test");
    }
    
    @Test
    void shouldPerformBasicCalculation() {
        int sum = 2 + 2;
        assertThat(sum).isEqualTo(4);
    }
}
