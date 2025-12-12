package com.restobook.reviewservice.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BookingServiceClient {

    private final WebClient webClient;

    public BookingServiceClient(@Value("${services.booking-service.url}") String bookingServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(bookingServiceUrl)
                .build();
    }

    public boolean hasCompletedBooking(Long userId, Long restaurantId) {
        log.debug("Vérification de réservation terminée - Utilisateur ID: {}, Restaurant ID: {}", userId, restaurantId);

        try {
            Boolean hasCompleted = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/bookings/completed")
                            .queryParam("userId", userId)
                            .queryParam("restaurantId", restaurantId)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Erreur lors de la vérification de la réservation");
                        return Mono.just(false).then(Mono.empty());
                    })
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(hasCompleted);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de la réservation pour utilisateur ID: {} et restaurant ID: {}: {}",
                    userId, restaurantId, e.getMessage());
            return false;
        }
    }
}
