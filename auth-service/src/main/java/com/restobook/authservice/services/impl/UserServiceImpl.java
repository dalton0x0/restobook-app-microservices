package com.restobook.authservice.services.impl;

import com.restobook.authservice.dtos.request.ChangePasswordRequest;
import com.restobook.authservice.dtos.request.CreateUserRequest;
import com.restobook.authservice.dtos.request.UpdateRoleRequest;
import com.restobook.authservice.dtos.request.UpdateUserRequest;
import com.restobook.authservice.dtos.response.PageResponse;
import com.restobook.authservice.dtos.response.UserResponse;
import com.restobook.authservice.entities.Role;
import com.restobook.authservice.entities.User;
import com.restobook.authservice.enums.RoleName;
import com.restobook.authservice.exceptions.BusinessException;
import com.restobook.authservice.exceptions.DuplicateResourceException;
import com.restobook.authservice.exceptions.ResourceNotFoundException;
import com.restobook.authservice.repositories.RoleRepository;
import com.restobook.authservice.repositories.UserRepository;
import com.restobook.authservice.services.RefreshTokenService;
import com.restobook.authservice.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Récupération de tous les utilisateurs - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<@NonNull User> userPage = userRepository.findAll(pageable);
        Page<@NonNull UserResponse> responsePage = userPage.map(UserResponse::fromEntity);
        log.debug("Nombre d'utilisateurs obtenus: {}", userPage.getTotalElements());

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Recherche de l'utilisateur par ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Utilisateur non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("Utilisateur", "id", id);
                });

        log.debug("Utilisateur trouvé par ID: {}", user.getFullName());

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Recherche de l'utilisateur par email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.debug("Utilisateur non trouvé avec l'email: {}", email);
                    return new ResourceNotFoundException("Utilisateur", "email", email);
                });

        log.debug("Utilisateur trouvé par email: {}", user.getFullName());

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByRole(RoleName roleName, Pageable pageable) {
        log.debug("Récupération des utilisateurs avec le rôle: {}", roleName);

        Page<@NonNull User> userPage = userRepository.findByRoleName(roleName, pageable);
        Page<@NonNull UserResponse> responsePage = userPage.map(UserResponse::fromEntity);
        log.debug("Nombre d'utilisateurs avec le rôle {}: {}", roleName, userPage.getTotalElements());

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.debug("Recherche d'utilisateurs avec le mot-clé: {}", keyword);

        Page<@NonNull User> userPage = userRepository.searchUsers(keyword, pageable);
        Page<@NonNull UserResponse> responsePage = userPage.map(UserResponse::fromEntity);
        log.debug("Nombre d'utilisateurs trouvés: {}", userPage.getTotalElements());

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (existsByEmail(request.getEmail())) {
            log.warn("Email déjà existant: {}", request.getEmail());
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Vérifier si le téléphone existe déjà
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            log.warn("Téléphone déjà existant: {}", request.getPhone());
            throw new DuplicateResourceException("Utilisateur", "téléphone", request.getPhone());
        }

        // Récupérer le rôle
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "nom", request.getRoleName()));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .emailVerified(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {} avec le rôle: {}", savedUser.getEmail(), role.getName());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            if (!request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
                if (!request.getPhone().equals(user.getPhone())) {
                    throw new DuplicateResourceException("Utilisateur", "téléphone", request.getPhone());
                }
            }
            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, UpdateRoleRequest request) {
        log.info("Mise à jour du rôle de l'utilisateur ID: {} vers: {}", id, request.getRoleName());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        Role newRole = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle", "nom", request.getRoleName()));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        // Révoquer les tokens existants
        refreshTokenService.revokeAllUserTokens(id);
        log.info("Rôle de l'utilisateur {} mis à jour vers: {}", user.getEmail(), newRole.getName());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse enableUser(Long id) {
        log.info("Activation de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        user.setEnabled(true);
        User updatedUser = userRepository.save(user);
        log.info("Utilisateur activé: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse disableUser(Long id) {
        log.info("Désactivation de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        user.setEnabled(false);
        User updatedUser = userRepository.save(user);

        // Révoquer tous les tokens
        refreshTokenService.revokeAllUserTokens(id);
        log.info("Utilisateur désactivé: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        // Révoquer tous les tokens avant la suppression
        refreshTokenService.revokeAllUserTokens(id);

        // Supprimer tous les tokens expirés
        int deleteTokenExpired = refreshTokenService.deleteExpiredTokens();
        log.debug("{} tokens expirés supprimés", deleteTokenExpired);
        userRepository.delete(user);
        log.info("Utilisateur supprimé: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        log.debug("Récupération du profil de l'utilisateur connecté ID: {}", userId);
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(Long userId, UpdateUserRequest request) {
        log.info("Mise à jour du profil utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            // Vérifier si le téléphone est déjà utilisé par un autre utilisateur
            if (!request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
                User existingUser = userRepository.findByEmail(user.getEmail()).orElse(null);
                if (existingUser == null || !existingUser.getPhone().equals(request.getPhone())) {
                    throw new DuplicateResourceException("Utilisateur", "téléphone", request.getPhone());
                }
            }
            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profil mis à jour avec succès pour: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.debug("Ancien mot de passe incorrect pour: {}", user.getEmail());
            throw new BusinessException("L'ancien mot de passe est incorrect", HttpStatus.BAD_REQUEST, "INVALID_OLD_PASSWORD");
        }

        // Vérifier que le nouveau mot de passe correspond à la confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.debug("Le nouveau mot de passe et la confirmation ne correspondent pas");
            throw new BusinessException("Le nouveau mot de passe et la confirmation ne correspondent pas", HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.debug("Le nouveau mot de passe doit être différent de l'ancien");
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien", HttpStatus.BAD_REQUEST, "SAME_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Révoquer tous les refresh tokens existants
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("Mot de passe changé avec succès pour: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
