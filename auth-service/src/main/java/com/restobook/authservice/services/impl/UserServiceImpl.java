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
    public UserResponse getUserById(Long id) {
        log.debug("Recherche de l'utilisateur par ID: {}", id);

        User user = findUserByIdOrThrow(id);
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
                    return new ResourceNotFoundException("email", email);
                });

        log.debug("Utilisateur trouvé par email: {}", user.getFullName());

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByRole(RoleName roleName, Pageable pageable) {
        log.debug("Récupération des utilisateurs avec le rôle: {}", roleName);

        Page<@NonNull User> userPage = userRepository.findByRole_Name(roleName, pageable);
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

        validateEmailUniqueness(request.getEmail());

        if (request.getPhone() != null) {
            validatePhoneUniqueness(request.getPhone(), null);
        }

        Role role = findRoleByNameOrThrow(request.getRoleName());
        User user = buildNewUser(request, role);
        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {} avec le rôle: {}", savedUser.getEmail(), role.getName());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        User user = findUserByIdOrThrow(id);
        updateUserFields(user, request);
        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, UpdateRoleRequest request) {
        log.info("Mise à jour du rôle de l'utilisateur ID: {} vers: {}", id, request.getRoleName());

        User user = findUserByIdOrThrow(id);
        Role newRole = findRoleByNameOrThrow(request.getRoleName());
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(id);
        log.info("Rôle de l'utilisateur {} mis à jour vers: {}", user.getEmail(), newRole.getName());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse enableUser(Long id) {
        log.info("Activation de l'utilisateur ID: {}", id);

        User user = findUserByIdOrThrow(id);
        user.setEnabled(true);
        User updatedUser = userRepository.save(user);
        log.info("Utilisateur activé: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse disableUser(Long id) {
        log.info("Désactivation de l'utilisateur ID: {}", id);

        User user = findUserByIdOrThrow(id);
        user.setEnabled(false);
        User updatedUser = userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(id);
        log.info("Utilisateur désactivé: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        User user = findUserByIdOrThrow(id);
        refreshTokenService.revokeAllUserTokens(id);
        int deletedTokensCount = refreshTokenService.deleteExpiredTokens();
        log.debug("{} tokens expirés supprimés", deletedTokensCount);
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

        User user = findUserByIdOrThrow(userId);
        updateUserFields(user, request);
        User updatedUser = userRepository.save(user);
        log.info("Profil mis à jour avec succès pour: {}", user.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);

        User user = findUserByIdOrThrow(userId);
        validateOldPassword(request.getOldPassword(), user.getPassword(), user.getEmail());
        validatePasswordMatch(request.getNewPassword(), request.getConfirmPassword());
        validatePasswordDifferent(request.getNewPassword(), user.getPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Mot de passe changé avec succès pour: {}", user.getEmail());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Recherche un utilisateur par ID ou lève une exception si non trouvée.
     *
     * @param id l'identifiant de l'utilisateur
     * @return l'utilisateur trouvé
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Utilisateur non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("id", id);
                });
    }

    /**
     * Recherche un role par nom ou lève une exception si non trouvée.
     * Methode privée pour éviter la duplication de code.
     *
     * @param roleName le nom du role
     * @return le role trouvé
     * @throws ResourceNotFoundException si le role n'existe pas
     */
    private Role findRoleByNameOrThrow(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "nom", roleName));
    }

    /**
     * Valide que l'email n'est pas déjà utilisé.
     *
     * @param email l'email a valider
     * @throws DuplicateResourceException si l'email existe déjà
     */
    private void validateEmailUniqueness(String email) {
        if (existsByEmail(email)) {
            log.warn("Email déjà existant: {}", email);
            throw new DuplicateResourceException("Utilisateur", "email", email);
        }
    }

    /**
     * Valide que le numéro de télephone n'est pas déjà utilisé par un autre utilisateur.
     *
     * @param phone le numéro de télephone a valider
     * @param currentUserPhone le télephone actuel de l'utilisateur (peut être null pour une creation)
     * @throws DuplicateResourceException si le télephone existe déjà
     */
    private void validatePhoneUniqueness(String phone, String currentUserPhone) {
        // Si le télephone est vide, pas de validation nécessaire
        if (phone == null || phone.isBlank()) {
            return;
        }

        // Si le télephone est identique à celui actuel de l'utilisateur, pas de validation
        if (phone.equals(currentUserPhone)) {
            return;
        }

        // Verifier si le télephone est déjà utilisé
        if (userRepository.existsByPhone(phone)) {
            log.warn("télephone déjà existant: {}", phone);
            throw new DuplicateResourceException("Utilisateur", "télephone", phone);
        }
    }

    /**
     * Construit un nouvel utilisateur à partir de la requête de création.
     *
     * @param request la requête de création
     * @param role le rôle à assigner
     * @return le nouvel utilisateur construit
     */
    private User buildNewUser(CreateUserRequest request, Role role) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un utilisateur.
     *
     * @param user l'utilisateur à modifier
     * @param request la requête de mise à jour contenant les nouveaux champs
     */
    private void updateUserFields(User user, UpdateUserRequest request) {
        // Mise a jour du prenom si fourni et non vide
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        // Mise à jour du nom si fourni et non vide
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        // Mise à jour du télephone avec validation d'unicité
        if (request.getPhone() != null) {
            validatePhoneUniqueness(request.getPhone(), user.getPhone());
            user.setPhone(request.getPhone());
        }
    }

    /**
     * Valide que l'ancien mot de passe correspond au mot de passe actuel.
     *
     * @param oldPassword l'ancien mot de passe fourni
     * @param currentPassword le mot de passe actuel encode
     * @param userEmail l'email de l'utilisateur pour le logging
     * @throws BusinessException si l'ancien mot de passe est incorrect
     */
    private void validateOldPassword(String oldPassword, String currentPassword, String userEmail) {
        if (!passwordEncoder.matches(oldPassword, currentPassword)) {
            log.debug("Ancien mot de passe incorrect pour: {}", userEmail);
            throw new BusinessException(
                    "L'ancien mot de passe est incorrect",
                    HttpStatus.BAD_REQUEST,
                    "INVALID_OLD_PASSWORD"
            );
        }
    }

    /**
     * Valide que le nouveau mot de passe correspond à sa confirmation.
     *
     * @param newPassword le nouveau mot de passe
     * @param confirmPassword la confirmation du nouveau mot de passe
     * @throws BusinessException si les mots de passe ne correspondent pas
     */
    private void validatePasswordMatch(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            log.debug("Le nouveau mot de passe et la confirmation ne correspondent pas");
            throw new BusinessException(
                    "Le nouveau mot de passe et la confirmation ne correspondent pas",
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_MISMATCH"
            );
        }
    }

    /**
     * Valide que le nouveau mot de passe est different de l'ancien.
     *
     * @param newPassword le nouveau mot de passe
     * @param currentPassword le mot de passe actuel encodé
     * @throws BusinessException si les mots de passe sont identiques
     */
    private void validatePasswordDifferent(String newPassword, String currentPassword) {
        if (passwordEncoder.matches(newPassword, currentPassword)) {
            log.debug("Le nouveau mot de passe doit être different de l'ancien");
            throw new BusinessException(
                    "Le nouveau mot de passe doit être different de l'ancien",
                    HttpStatus.BAD_REQUEST,
                    "SAME_PASSWORD"
            );
        }
    }
}
