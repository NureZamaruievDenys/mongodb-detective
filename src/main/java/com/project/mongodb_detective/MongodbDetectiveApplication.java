package com.project.mongodb_detective;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Student API",
                version = "1.0",
                description = "API documentation"
        )
)

@SpringBootApplication
public class MongodbDetectiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongodbDetectiveApplication.class, args);
    }

}
