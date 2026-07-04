package com.medilab.gateway.configuration;


import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@OpenAPIDefinition(info = @Info(title = "Gateway API",
                                version = "1.0"),
                    security = @SecurityRequirement(name = "bearerAuth"))


@Configuration
public class OpenApiCustomConfiguration {
    
    /*@Value("${api.server.gateway.url}")
    private String urlServerGateway;
    
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url(urlServerGateway)
            ));
    }*/
}