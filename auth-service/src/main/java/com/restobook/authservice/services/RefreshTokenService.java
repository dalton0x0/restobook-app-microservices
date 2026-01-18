package com.restobook.authservice.services;

import com.restobook.authservice.entities.RefreshToken;
import com.restobook.authservice.entities.User;

public interface RefreshTokenService {

    /**
     * Crée un nouveau refresh token pour un utilisateur.
     *
     * @param user l'utilisateur pour lequel créer le token
     * @return le refresh token créé
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Valide et retourne le refresh token.
     *
     * @param token la valeur du token à valider
     * @return le refresh token validé
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Révoque un refresh token spécifique.
     *
     * @param token la valeur du token à révoquer
     */
    void revokeRefreshToken(String token);

    /**
     * Révoque tous les refresh tokens d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     */
    void revokeAllUserTokens(Long userId);

    /**
     * Supprime les tokens expirés de la base de données.
     *
     * @return le nombre de tokens supprimés
     */
    int deleteExpiredTokens();
}
