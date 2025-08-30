
package com.cvshealth.digital.microservice.iqe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(basePackages = { "com.cvshealth.*" })
@EntityScan("com.cvshealth.*")
@SpringBootApplication
public class IQEApplication {

    public static void main(String[] args) {
        SpringApplication.run(IQEApplication.class, args);
    }

}
