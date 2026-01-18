package com.restobook.restaurantservice.clients;

import com.restobook.restaurantservice.constants.AuthenticationConst;
import com.restobook.restaurantservice.dtos.response.TokenValidationResponse;
import com.restobook.restaurantservice.exceptions.ResourceNotFoundException;
import com.restobook.restaurantservice.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(@Value("${services.auth-service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public TokenValidationResponse validateToken(String token) {
        log.debug("Validation du token auprès de l'auth service");

        try {
            TokenValidationResponse response = webClient.get()
                    .uri("/api/v1/internal/validate")
                    .header(AuthenticationConst.AUTH_HEADER, AuthenticationConst.TOKEN_PREFIX + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.debug("Token invalide ou expiré:{}", clientResponse.statusCode());
                        return Mono.error(new UnauthorizedException("Token invalide ou expiré"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("Erreur du service d'authentification:{}", clientResponse.statusCode());
                        return Mono.error(new UnauthorizedException("Service d'authentification indisponible"));
                    })
                    .bodyToMono(TokenValidationResponse.class)
                    .block();

            if (response == null || !response.isValid()) {
                log.debug("Token non valide");
                throw new UnauthorizedException("Token non valide");
            }

            log.debug("Token valide pour l'utilisateur: {}", response.getEmail());
            return response;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage());
            throw new UnauthorizedException("Impossible de valider le token.");
        }
    }

    public TokenValidationResponse.UserInfo getUserById(String userId, String token) {
        log.debug("Récupération de l'utilisateur {} depuis l'auth service", userId);

        try {
            return webClient.get()
                    .uri("api/v1/internal/users/{id}", userId)
                    .header(AuthenticationConst.AUTH_HEADER, AuthenticationConst.TOKEN_PREFIX + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.debug("Utilisateur non trouvé:{}", clientResponse.statusCode());
                        return Mono.error(new ResourceNotFoundException("Utilisateur inexistant"));
                    })
                    .bodyToMono(TokenValidationResponse.UserInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean userExists(String userId, String token) {
        log.debug("Vérification de l'existence de l'utilisateur {} dans l'auth service", userId);

        try {
            Boolean exists = webClient.get()
                    .uri("api/v1/internal/users/{id}/exists", userId)
                    .header(AuthenticationConst.AUTH_HEADER, AuthenticationConst.TOKEN_PREFIX + token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'utilisateur {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
