package com.medilab.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;


@SpringBootApplication
public class CloudGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
	    return builder.routes()
	            .route("notesPatient", 
                        r-> r.path("/patient/note/**")
                        .filters(f -> f.addResponseHeader("powered-by", "notesPatient"))
                        .uri("http://localhost:9001"))
	            .route("infosPatients", 
	                    r -> r.path("/patient/**")
	                    .filters(f -> f.addResponseHeader("powered-by", "infosPatient"))
        	                            //.addResponseHeader("Access-Control-Allow-Origin", "http://localhost:5173")
        	                            //.addResponseHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
        	                            //.addResponseHeader("Access-Control-Allow-Headers", "*"))
	                    .uri("http://localhost:9000"))
	            .build();
	                  
	}

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}


