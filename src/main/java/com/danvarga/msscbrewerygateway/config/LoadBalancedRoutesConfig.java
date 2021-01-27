package com.danvarga.msscbrewerygateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local-discovery")
@Configuration
public class LoadBalancedRoutesConfig {

    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/api/v1/beer*", "/api/v1/beer/*", "/api/v1/beerUpc/*", "/api/v1/beerUpc*")
                        // 'lb' as in Load Balancer - in case multiple instances are running.
                        .uri("lb://beer-service")
                        .id("beer-service")
                )
                .route(r -> r.path("/api/v1/customers/**")
                        .uri("lb://beer-order-service")
                        .id("beer-order-service")
                )
                .route(r -> r.path("/api/v1/beer/*/inventory")
                        .filters(f -> f.circuitBreaker(c -> c.setName("inventoryBC")
                                .setFallbackUri("forward:/inventory-failover")
                                .setRouteId("inv-failover")))
                        .uri("lb://beer-inventory-service")
                        .id("beer-inventory-service")
                )
                .route(r -> r.path("/inventory-failover/**")
                        .uri("lb://beer-inventory-failover")
                        .id("beer-inventory-failover")
                )
                .build();
    }
}
