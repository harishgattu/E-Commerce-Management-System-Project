package com.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Management System REST API")
                        .version("1.0.0")
                        .description("Production-ready E-Commerce Backend API featuring Product Listings, User Authentication, Shopping Cart, Order Processing with Atomic Inventory Deductions, and Admin Dashboard Metrics.")
                        .contact(new Contact()
                                .name("Engineering Team")
                                .email("engineering@eshop.com"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")));
    }
}
