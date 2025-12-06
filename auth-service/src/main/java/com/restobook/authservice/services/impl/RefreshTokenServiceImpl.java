package com.restobook.authservice.services.impl;

import com.restobook.authservice.entities.RefreshToken;
import com.restobook.authservice.entities.User;
import com.restobook.authservice.exceptions.InvalidTokenException;
import com.restobook.authservice.repositories.RefreshTokenRepository;
import com.restobook.authservice.security.JwtTokenProvider;
import com.restobook.authservice.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.debug("Création d'un refresh token pour l'utilisateur: {}", user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration()))
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token créé avec succès pour: {}", user.getEmail());

        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        log.debug("Validation du refresh token");

        if (token == null || token.isBlank()) {
            log.debug("Token null ou vide");
            throw new InvalidTokenException("Refresh token requis");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.debug("Refresh token non trouvé dans la base de données");
                    return new InvalidTokenException("Refresh token invalide");
                });

        // Vérifier si le token est encore valide
        if (!refreshToken.isValid()) {
            log.debug("Refresh token invalide (révoqué et expiré) pour l'utilisateur: {}", refreshToken.getUser().getEmail());
            throw new InvalidTokenException();
        }

        // Vérifier si le token est révoqué
        if (refreshToken.getRevoked()) {
            log.warn("Tentative d'utilisation d'un refresh token révoqué pour l'utilisateur: {}",
                    refreshToken.getUser().getEmail());
            throw new InvalidTokenException("Ce refresh token a été révoqué. Veuillez vous reconnecter.");
        }

        // Vérifier si le token est expiré
        if (refreshToken.isExpired()) {
            log.debug("Refresh token expiré pour l'utilisateur: {}", refreshToken.getUser().getEmail());
            throw new InvalidTokenException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        log.debug("Refresh token valide pour: {}", refreshToken.getUser().getEmail());

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        log.debug("Révocation du refresh token");

        if (token == null || token.isBlank()) {
            log.debug("Tentative de révocation avec un token null ou vide");
            throw new InvalidTokenException("Refresh token requis");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.debug("Tentative de révocation d'un token inexistant");
                    return new InvalidTokenException("Refresh token non trouvé");
                });

        // Vérifier si déjà révoqué
        if (refreshToken.getRevoked()) {
            log.debug("Token déjà révoqué, aucune action nécessaire");
            return;
        }

        refreshToken.setRevoked(true);
        refreshToken.setExpiryDate(Instant.now());
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token révoqué avec succès pour l'utilisateur: {}",
                refreshToken.getUser().getEmail());
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        log.info("Révocation de tous les tokens pour l'utilisateur ID: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Tous les tokens révoqués pour l'utilisateur ID: {}", userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "${token.cleanup.cron:0 0 2 * * ?}")
    public int deleteExpiredTokens() {
        log.info("Nettoyage des tokens expirés");
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("{} tokens expirés supprimés", deletedCount);

        return deletedCount;
    }
}
