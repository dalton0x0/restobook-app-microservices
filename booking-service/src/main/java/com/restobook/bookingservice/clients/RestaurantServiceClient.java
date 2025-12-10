package com.restobook.bookingservice.clients;

import com.restobook.bookingservice.exceptions.ResourceNotFoundException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Slf4j
@Component
public class RestaurantServiceClient {

    private final WebClient webClient;

    public RestaurantServiceClient(@Value("${services.restaurant-service.url}") String restaurantServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(restaurantServiceUrl)
                .build();
    }

    public boolean restaurantExists(Long restaurantId) {
        log.debug("Vérification de l'existence du restaurant ID: {}", restaurantId);

        try {
            Boolean exists = webClient.get()
                    .uri("/api/v1/internal/restaurants/{id}/exists", restaurantId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new RuntimeException("Erreur lors de la vérification du restaurant")))
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du restaurant ID: {}: {}", restaurantId, e.getMessage());
            return false;
        }
    }

    public Integer getRestaurantCapacity(Long restaurantId) {
        log.debug("Récupération de la capacité du restaurant ID: {}", restaurantId);

        try {
            return webClient.get()
                    .uri("/api/v1/internal/restaurants/{id}/capacity", restaurantId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new ResourceNotFoundException("Restaurant", "id", restaurantId)))
                    .bodyToMono(Integer.class)
                    .block();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la capacité du restaurant ID: {}: {}", restaurantId, e.getMessage());
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }
    }

    public boolean isRestaurantOpen(Long restaurantId, DayOfWeek dayOfWeek, LocalTime time) {
        log.debug("Vérification de l'ouverture du restaurant ID: {} - {} à {}", restaurantId, dayOfWeek, time);

        try {
            Boolean isOpen = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/restaurants/{id}/is-open")
                            .queryParam("dayOfWeek", dayOfWeek.name())
                            .queryParam("time", time.toString())
                            .build(restaurantId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new RuntimeException("Erreur lors de la vérification des horaires")))
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(isOpen);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des horaires du restaurant ID: {}: {}", restaurantId, e.getMessage());
            return false;
        }
    }

    public RestaurantInfo getRestaurantInfo(Long restaurantId) {
        log.debug("Récupération des informations du restaurant ID: {}", restaurantId);

        try {
            return webClient.get()
                    .uri("/api/v1/internal/restaurants/{id}", restaurantId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new ResourceNotFoundException("Restaurant", "id", restaurantId)))
                    .bodyToMono(RestaurantInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du restaurant ID: {}: {}", restaurantId, e.getMessage());
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }
    }

    @Data
    @NoArgsConstructor
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private String address;
        private String city;
        private Integer totalCapacity;
        private Long ownerId;
        private Boolean active;
    }
}
