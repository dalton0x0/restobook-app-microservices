package com.restobook.authservice.repositories;

import com.restobook.authservice.entities.User;
import com.restobook.authservice.enums.RoleName;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Long> {

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email l'adresse email de l'utilisateur
     * @return un Optional contenant l'utilisateur si trouve
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie l'existence d'un utilisateur par email.
     *
     * @param email l'adresse email a verifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie l'existence d'un utilisateur par numero de telephone.
     *
     * @param phone le numero de telephone a verifier
     * @return true si le telephone existe, false sinon
     */
    boolean existsByPhone(String phone);

    /**
     * Recherche un utilisateur par son token de verification.
     *
     * @param token le token de verification
     * @return un Optional contenant l'utilisateur si trouve
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Recherche un utilisateur par son token de reinitialisation de mot de passe.
     *
     * @param token le token de reinitialisation
     * @return un Optional contenant l'utilisateur si trouve
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Récupère tous les utilisateurs ayant un role spécifique.
     *
     * @param roleName le nom du role
     * @return une liste d'utilisateurs
     */
    List<User> findByRole_Name(RoleName roleName);

    /**
     * Récupère une page d'utilisateurs ayant un role spécifique.
     *
     * @param roleName le nom du role
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs
     */
    Page<User> findByRole_Name(RoleName roleName, Pageable pageable);

    /**
     * Recherche des utilisateurs par mot-cle dans le prenom, nom ou email.
     *
     * @param keyword le mot-cle de recherche
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs correspondant à la recherche
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Récupère une page d'utilisateurs filtres par leur statut d'activation.
     *
     * @param enabled le statut d'activation (true ou false)
     * @param pageable les informations de pagination
     * @return une page d'utilisateurs
     */
    Page<User> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * Compte le nombre d'utilisateurs ayant un role spécifique.
     * Utilise la notation Spring Data JPA pour naviguer dans les relations (role_name).
     *
     * @param roleName le nom du role
     * @return le nombre d'utilisateurs avec ce role
     */
    long countByRole_Name(RoleName roleName);
}
