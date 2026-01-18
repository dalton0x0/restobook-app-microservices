package com.restobook.authservice.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Classe de configuration pour les propriétés JWT.
 * Mappe les propriétés du fichier de configuration avec le préfixe "jwt".
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clé secrète utilisée pour signer les tokens JWT.
     */
    private String secret;

    /**
     * Durée de validité d'un access token en millisecondes.
     */
    private Long accessTokenExpiration;

    /**
     * Durée de validité d'un refresh token en millisecondes.
     */
    private Long refreshTokenExpiration;
}
