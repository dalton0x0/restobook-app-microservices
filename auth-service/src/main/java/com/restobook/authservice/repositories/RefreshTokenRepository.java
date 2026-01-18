package com.restobook.authservice.repositories;

import com.restobook.authservice.entities.RefreshToken;
import com.restobook.authservice.entities.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshToken, @NonNull Long> {

    /**
     * Recherche un token de rafraîchissement par sa valeur.
     *
     * @param token la valeur du token
     * @return un Optional contenant le token si trouvé
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Recherche un token de rafraîchissement actif (non révoqué) par sa valeur.
     *
     * @param token la valeur du token
     * @return un Optional contenant le token si trouvé et non révoqué
     */
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    /**
     * Révoque tous les tokens de rafraîchissement d'un utilisateur.
     * Cette méthode modifie la base de données.
     *
     * @param user l'utilisateur dont les tokens doivent être révoqués
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(@Param("user") User user);

    /**
     * Révoque tous les tokens de rafraîchissement d'un utilisateur par son identifiant.
     * Cette méthode modifie la base de données et met à jour la date d'expiration.
     *
     * @param userId l'identifiant de l'utilisateur
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.expiryDate = CURRENT_TIMESTAMP WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Supprime tous les tokens de rafraîchissement expirés.
     * Cette méthode modifie la base de données.
     *
     * @param now l'instant actuel pour comparaison avec la date d'expiration
     * @return le nombre de tokens supprimés
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Compte le nombre de tokens actifs (non révoqués et non expirés) pour un utilisateur.
     *
     * @param user l'utilisateur concerné
     * @param now l'instant actuel pour vérifier l'expiration
     * @return le nombre de tokens actifs
     */
    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.user = :user AND r.revoked = false AND r.expiryDate > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") Instant now);
}
