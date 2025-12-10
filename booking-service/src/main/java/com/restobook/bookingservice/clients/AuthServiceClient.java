package com.restobook.bookingservice.clients;

import com.restobook.bookingservice.dtos.response.TokenValidationResponse;
import com.restobook.bookingservice.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Value("${services.auth-service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public TokenValidationResponse validateToken(String token) {
        log.debug("Validation du token auprès de l'Auth Service");

        try {
            TokenValidationResponse response = webClient.get()
                    .uri("/api/v1/internal/validate")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.debug("Token invalide ou expiré: {}", clientResponse.statusCode());
                        return Mono.error(new UnauthorizedException("Token invalide ou expiré"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("Erreur du service d'authentification: {}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Service d'authentification indisponible"));
                    })
                    .bodyToMono(TokenValidationResponse.class)
                    .block();

            if (response == null || !response.isValid()) {
                log.debug("Token non valide");
                throw new UnauthorizedException("Token invalide");
            }

            log.debug("Token validé pour l'utilisateur: {}", response.getEmail());
            return response;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage());
            throw new UnauthorizedException("Impossible de valider le token");
        }
    }
}
