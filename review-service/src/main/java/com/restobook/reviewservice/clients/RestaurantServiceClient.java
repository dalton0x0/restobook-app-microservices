package com.restobook.reviewservice.clients;

import com.restobook.reviewservice.exceptions.ResourceNotFoundException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du restaurant ID: {}: {}", restaurantId, e.getMessage());
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }
    }

    public void updateRestaurantRating(Long restaurantId, Double averageRating, Long totalReviews) {
        log.debug("Mise à jour de la note du restaurant ID: {} - Note: {}, Total avis: {}",
                restaurantId, averageRating, totalReviews);

        try {
            webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/restaurants/{id}/rating")
                            .queryParam("rating", averageRating)
                            .queryParam("totalReviews", totalReviews)
                            .build(restaurantId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Erreur lors de la mise à jour de la note du restaurant ID: {}", restaurantId);
                        return Mono.empty();
                    })
                    .bodyToMono(Void.class)
                    .block();
            log.debug("Note du restaurant ID: {} mise à jour avec succès: {} ({} avis)", restaurantId, averageRating, totalReviews);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la note du restaurant ID: {}: {}", restaurantId, e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private String address;
        private String city;
        private Long ownerId;
        private Boolean active;
        private Double averageRating;
        private Integer totalReviews;
    }
}
