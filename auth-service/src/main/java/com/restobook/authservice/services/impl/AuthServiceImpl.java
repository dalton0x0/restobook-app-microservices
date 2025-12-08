package com.restobook.authservice.services.impl;

import com.restobook.authservice.dtos.request.LoginRequest;
import com.restobook.authservice.dtos.request.RefreshTokenRequest;
import com.restobook.authservice.dtos.request.RegisterRequest;
import com.restobook.authservice.dtos.response.AuthResponse;
import com.restobook.authservice.dtos.response.TokenValidationResponse;
import com.restobook.authservice.dtos.response.UserResponse;
import com.restobook.authservice.entities.RefreshToken;
import com.restobook.authservice.entities.Role;
import com.restobook.authservice.entities.User;
import com.restobook.authservice.enums.RoleName;
import com.restobook.authservice.exceptions.*;
import com.restobook.authservice.repositories.RoleRepository;
import com.restobook.authservice.repositories.UserRepository;
import com.restobook.authservice.security.JwtTokenProvider;
import com.restobook.authservice.security.UserDetailsImpl;
import com.restobook.authservice.services.AuthService;
import com.restobook.authservice.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Inscription d'un nouvel utilisateur: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            log.warn("Tentative d'inscription avec un email déjà existant: {}", request.getEmail());
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Vérifier si le téléphone existe déjà (si fourni)
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            log.warn("Tentative d'inscription avec un téléphone déjà existant: {}", request.getPhone());
            throw new DuplicateResourceException("Utilisateur", "téléphone", request.getPhone());
        }

        // Récupérer le rôle CLIENT par défaut
        Role clientRole = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() -> {
                    log.error("Rôle CLIENT non trouvé dans la base de données");
                    return new ResourceNotFoundException("Rôle CLIENT non trouvé");
                });

        // Créer l'utilisateur
        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(clientRole)
                .enabled(true)
                .emailVerified(false)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Connexion de l'utilisateur: {}", email);

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Récupérer l'utilisateur pour mise à jour
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

            // Mettre à jour la date de dernière connexion
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Générer les tokens
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("Connexion réussie pour: {}", email);
            return AuthResponse.of(
                    accessToken,
                    refreshToken.getToken(),
                    jwtTokenProvider.getAccessTokenExpiration(),
                    UserResponse.fromEntity(user)
            );

        } catch (DisabledException _) {
            log.warn("Tentative de connexion sur un compte désactivé: {}", email);
            throw new AccountDisabledException();
        } catch (LockedException _) {
            log.warn("Tentative de connexion sur un compte verrouillé: {}", email);
            throw new AccountLockedException();
        } catch (BadCredentialsException _) {
            log.warn("Échec d'authentification pour: {}", email);
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Rafraîchissement de token");

        // Valider le refresh token
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = oldRefreshToken.getUser();

        // Vérifier que l'utilisateur est toujours actif
        if (Boolean.FALSE.equals(user.getEnabled())) {
            log.warn("Tentative de rafraîchissement pour un compte désactivé: {}", user.getEmail());
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            throw new AccountDisabledException();
        }

        if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
            log.warn("Tentative de rafraîchissement pour un compte verrouillé: {}", user.getEmail());
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            throw new AccountLockedException();
        }

        // Générer un nouveau access token
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        // Révoquer l'ancien refresh token et en créer un nouveau
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        log.info("Token rafraîchi avec succès pour: {}", user.getEmail());

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpiration(),
                UserResponse.fromEntity(user)
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Déconnexion d'un utilisateur");

        if (refreshToken == null || refreshToken.isBlank()) {
            log.debug("Tentative de déconnexion sans refresh token");
            throw new InvalidTokenException("Refresh token requis pour la déconnexion");
        }

        refreshTokenService.validateRefreshToken(refreshToken);
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Déconnexion réussie");
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        log.info("Déconnexion de toutes les sessions pour l'utilisateur ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Utilisateur", "id", userId);
        }

        refreshTokenService.revokeAllUserTokens(userId);
        log.info("Toutes les sessions révoquées pour l'utilisateur ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        log.debug("Validation de token inter-service");

        if (token == null || token.isBlank()) {
            log.debug("Token vide ou null");
            return TokenValidationResponse.invalid("Token manquant");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            log.debug("Token invalide ou expiré");
            return TokenValidationResponse.invalid("Token invalide ou expiré");
        }

        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Vérifier que l'utilisateur existe toujours et est actif
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.debug("Utilisateur du token non trouvé: {}", userId);
                return TokenValidationResponse.invalid("Utilisateur non trouvé");
            }

            if (Boolean.FALSE.equals(user.getEnabled())) {
                log.debug("Utilisateur désactivé: {}", email);
                return TokenValidationResponse.invalid("Compte désactivé");
            }

            if (Boolean.FALSE.equals(user.getAccountNonLocked())) {
                log.debug("Utilisateur verrouillé: {}", email);
                return TokenValidationResponse.invalid("Compte verrouillé");
            }

            log.debug("Token valide pour l'utilisateur: {} (ID: {})", email, userId);
            return TokenValidationResponse.valid(userId, email, role);

        } catch (Exception ex) {
            log.error("Erreur lors de l'extraction des informations du token: {}", ex.getMessage());
            return TokenValidationResponse.invalid("Erreur lors de la validation du token");
        }
    }
}
