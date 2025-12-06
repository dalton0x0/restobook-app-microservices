package com.restobook.authservice.services;

import com.restobook.authservice.dtos.request.ChangePasswordRequest;
import com.restobook.authservice.dtos.request.CreateUserRequest;
import com.restobook.authservice.dtos.request.UpdateRoleRequest;
import com.restobook.authservice.dtos.request.UpdateUserRequest;
import com.restobook.authservice.dtos.response.PageResponse;
import com.restobook.authservice.dtos.response.UserResponse;
import com.restobook.authservice.enums.RoleName;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Liste tous les utilisateurs avec pagination
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Récupère un utilisateur par son ID
     */
    UserResponse getUserById(Long id);

    /**
     * Récupère un utilisateur par son email
     */
    UserResponse getUserByEmail(String email);

    /**
     * Liste les utilisateurs par rôle
     */
    PageResponse<UserResponse> getUsersByRole(RoleName roleName, Pageable pageable);

    /**
     * Recherche des utilisateurs par mot-clé
     */
    PageResponse<UserResponse> searchUsers(String keyword, Pageable pageable);

    /**
     * Création d'un utilisateur par un admin (avec choix du rôle)
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Met à jour un utilisateur (admin)
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Met à jour le rôle d'un utilisateur (admin)
     */
    UserResponse updateUserRole(Long id, UpdateRoleRequest request);

    /**
     * Active un utilisateur
     */
    UserResponse enableUser(Long id);

    /**
     * Désactive un utilisateur
     */
    UserResponse disableUser(Long id);

    /**
     * Supprime un utilisateur
     */
    void deleteUser(Long id);

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    UserResponse getCurrentUser(Long userId);

    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    UserResponse updateCurrentUser(Long userId, UpdateUserRequest request);

    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Vérifie si un email existe
     */
    boolean existsByEmail(String email);
}
