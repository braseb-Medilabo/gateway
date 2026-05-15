package com.medilab.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsConfigurationSource;



@SpringBootApplication
public class CloudGatewayApplication {

	@Value("${api.prefix}")
	private String apiPrefix;
    
    public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
	    return builder.routes()
	            .route("notesPatient", 
                        r-> r.path(apiPrefix + "/patient/note/**")
                        .filters(f -> f.addResponseHeader("powered-by", "notesPatient")
                                      .rewritePath(apiPrefix + "/patient/note/(?<remainingPath>.*)", "/${remainingPath}"))
                        .uri("http://localhost:9001"))
	            .route("riskPatients",
	                    r -> r.path(apiPrefix + "/patient/risk/**")
	                    .filters(f -> f.rewritePath(apiPrefix + "/patient/risk/(?<remainingPath>.*)", "/${remainingPath}")
	                                .addRequestHeader("powered-by", "riskPatients"))
	                    .uri("http://localhost:9002"))
	            .route("infosPatients", 
	                    r -> r.path(apiPrefix + "/patient/**")
	                    .filters(f -> f.addResponseHeader("powered-by", "infosPatient")
	                                   .rewritePath(apiPrefix + "/patient/(?<remainingPath>.*)", "/${remainingPath}"))
	                    
        	            .uri("http://localhost:9000"))
	            
	            .build();
	                  
	}

    /*@Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }*/
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}


