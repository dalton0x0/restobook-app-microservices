package com.restobook.authservice.services;

import com.restobook.authservice.dtos.request.LoginRequest;
import com.restobook.authservice.dtos.request.RefreshTokenRequest;
import com.restobook.authservice.dtos.request.RegisterRequest;
import com.restobook.authservice.dtos.response.AuthResponse;
import com.restobook.authservice.dtos.response.TokenValidationResponse;
import com.restobook.authservice.dtos.response.UserResponse;

public interface AuthService {

    /**
     * Inscription d'un nouvel utilisateur avec le rôle CLIENT par défaut.
     *
     * @param request les informations d'inscription de l'utilisateur
     * @return les informations de l'utilisateur créé
     */
    UserResponse register(RegisterRequest request);

    /**
     * Connexion d'un utilisateur.
     *
     * @param request les identifiants de connexion (email et mot de passe)
     * @return la réponse d'authentification contenant les tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Rafraîchissement du token d'accès.
     *
     * @param request la requête contenant le refresh token
     * @return la réponse d'authentification avec les nouveaux tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Déconnexion d'un utilisateur, révocation du refresh token.
     *
     * @param refreshToken le refresh token à révoquer
     */
    void logout(String refreshToken);

    /**
     * Déconnexion de toutes les sessions d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     */
    void logoutAll(Long userId);

    /**
     * Validation d'un token pour les appels inter-services.
     *
     * @param token le token JWT à valider
     * @return la réponse de validation contenant les informations du token
     */
    TokenValidationResponse validateToken(String token);
}
