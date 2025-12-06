package com.restobook.authservice.controllers;

import com.restobook.authservice.dtos.request.LoginRequest;
import com.restobook.authservice.dtos.request.RefreshTokenRequest;
import com.restobook.authservice.dtos.request.RegisterRequest;
import com.restobook.authservice.dtos.response.ApiResponse;
import com.restobook.authservice.dtos.response.AuthResponse;
import com.restobook.authservice.dtos.response.UserResponse;
import com.restobook.authservice.security.UserDetailsImpl;
import com.restobook.authservice.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints pour l'authentification des utilisateurs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Inscription",
            description = "Crée un nouveau compte utilisateur avec le rôle CLIENT par défaut. " +
                    "L'utilisateur doit se connecter séparément après l'inscription."
    )
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Requête HTTP POST /auth/register - Email: {}", request.getEmail());
        UserResponse userResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie. Veuillez vous connecter.", userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne les tokens JWT")
    public ResponseEntity<@NonNull ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Requête HTTP POST /auth/login - Email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Rafraîchir le token",
            description = "Génère un nouveau access token et un nouveau refresh token (rotation). " +
                    "L'ancien refresh token est révoqué pour des raisons de sécurité. " +
                    "Le client DOIT stocker le nouveau refresh token retourné."
    )
    public ResponseEntity<@NonNull ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Requête HTTP POST /auth/refresh");
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token rafraîchi avec succès", authResponse));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Déconnexion",
            description = "Révoque le refresh token. Le token ne pourra plus être utilisé pour " +
                    "générer de nouveaux access tokens."
    )
    public ResponseEntity<@NonNull ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Requête HTTP POST /auth/logout");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Déconnexion réussie"));
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Déconnexion de toutes les sessions",
            description = "Révoque tous les refresh tokens de l'utilisateur connecté. " +
                    "Toutes les sessions actives seront invalidées."
    )
    public ResponseEntity<@NonNull ApiResponse<Void>> logoutAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.debug("Requête HTTP POST /auth/logout-all - Utilisateur: {}", userDetails.getEmail());
        authService.logoutAll(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Toutes les sessions ont été déconnectées"));
    }
}
