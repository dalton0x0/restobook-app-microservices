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
     * Liste tous les utilisateurs avec pagination.
     *
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return les informations de l'utilisateur
     */
    UserResponse getUserById(Long id);

    /**
     * Récupère un utilisateur par son email.
     *
     * @param email l'adresse email de l'utilisateur
     * @return les informations de l'utilisateur
     */
    UserResponse getUserByEmail(String email);

    /**
     * Liste les utilisateurs par rôle avec pagination.
     *
     * @param roleName le nom du rôle
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs ayant le rôle spécifié
     */
    PageResponse<UserResponse> getUsersByRole(RoleName roleName, Pageable pageable);

    /**
     * Recherche des utilisateurs par mot-clé.
     *
     * @param keyword le mot-clé de recherche
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs correspondant à la recherche
     */
    PageResponse<UserResponse> searchUsers(String keyword, Pageable pageable);

    /**
     * Création d'un utilisateur par un administrateur avec choix du rôle.
     *
     * @param request les informations de l'utilisateur à créer
     * @return les informations de l'utilisateur créé
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Met à jour les informations d'un utilisateur (administrateur).
     *
     * @param id l'identifiant de l'utilisateur à modifier
     * @param request les nouvelles informations de l'utilisateur
     * @return les informations de l'utilisateur mis à jour
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Met à jour le rôle d'un utilisateur (administrateur).
     *
     * @param id l'identifiant de l'utilisateur
     * @param request la requête contenant le nouveau rôle
     * @return les informations de l'utilisateur mis à jour
     */
    UserResponse updateUserRole(Long id, UpdateRoleRequest request);

    /**
     * Active un compte utilisateur.
     *
     * @param id l'identifiant de l'utilisateur à activer
     * @return les informations de l'utilisateur activé
     */
    UserResponse enableUser(Long id);

    /**
     * Désactive un compte utilisateur.
     *
     * @param id l'identifiant de l'utilisateur à désactiver
     * @return les informations de l'utilisateur désactivé
     */
    UserResponse disableUser(Long id);

    /**
     * Supprime un utilisateur de façon définitive.
     *
     * @param id l'identifiant de l'utilisateur à supprimer
     */
    void deleteUser(Long id);

    /**
     * Récupère le profil de l'utilisateur actuellement connecté.
     *
     * @param userId l'identifiant de l'utilisateur connecté
     * @return les informations du profil
     */
    UserResponse getCurrentUser(Long userId);

    /**
     * Met à jour le profil de l'utilisateur actuellement connecté.
     *
     * @param userId l'identifiant de l'utilisateur connecté
     * @param request les nouvelles informations du profil
     * @return les informations du profil mis à jour
     */
    UserResponse updateCurrentUser(Long userId, UpdateUserRequest request);

    /**
     * Change le mot de passe de l'utilisateur actuellement connecté.
     *
     * @param userId l'identifiant de l'utilisateur connecté
     * @param request la requête contenant l'ancien et le nouveau mot de passe
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Vérifie si un email existe déjà dans la base de données.
     *
     * @param email l'adresse email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);
}
