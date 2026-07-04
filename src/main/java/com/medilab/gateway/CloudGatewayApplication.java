package com.medilab.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;



@SpringBootApplication
public class CloudGatewayApplication {

	@Value("${api.prefix}")
	private String apiPrefix;
	
	@Value("${service.infospatient.url}")
	private String serviceInfosPatientUrl;
	
	@Value("${service.notespatient.url}")
    private String serviceNotesPatientUrl;
	
	@Value("${service.riskpatient.url}")
    private String serviceRiskPatientUrl;
    
    public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder) {
	    return builder.routes()
	            .route("notesPatient", 
                        r-> r.path(apiPrefix + "/patient/note/**")
                        .filters(f -> f.addResponseHeader("powered-by", "notesPatient")
                                      .rewritePath(apiPrefix + "/(?<remainingPath>.*)", "/${remainingPath}"))
                        .uri(serviceNotesPatientUrl))
	            .route("riskPatients",
	                    r -> r.path(apiPrefix + "/patient/risk/**")
	                    .filters(f -> f.rewritePath(apiPrefix + "/(?<remainingPath>.*)", "/${remainingPath}")
	                                .addRequestHeader("powered-by", "riskPatients"))
	                    .uri(serviceRiskPatientUrl))
	            .route("infosPatients", 
	                    r -> r.path(apiPrefix + "/patient/**")
	                    .filters(f -> f.addResponseHeader("powered-by", "infosPatient")
	                                   .rewritePath(apiPrefix + "/(?<remainingPath>.*)", "/${remainingPath}"))
	                    
        	            .uri(serviceInfosPatientUrl))
	            
	            .build();
	                  
	}

}


