package com.restobook.authservice.repositories;

import com.restobook.authservice.entities.Role;
import com.restobook.authservice.enums.RoleName;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<@NonNull Role, @NonNull Long> {

    /**
     * Recherche un rôle par son nom.
     *
     * @param name le nom du rôle
     * @return un Optional contenant le rôle si trouvé
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Vérifie l'existence d'un rôle par son nom.
     *
     * @param name le nom du rôle à vérifier
     * @return true si le rôle existe, false sinon
     */
    boolean existsByName(RoleName name);
}
