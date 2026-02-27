package com.restobook.authservice.configs;

import com.restobook.authservice.entities.Role;
import com.restobook.authservice.entities.User;
import com.restobook.authservice.enums.RoleName;
import com.restobook.authservice.repositories.RoleRepository;
import com.restobook.authservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    @NullMarked
    public void run(String... args) {
        log.info("Initialisation des données de base...");
        initializeRoles();
        initializeAdminUser();
        log.info("Initialisation des données terminée.");
    }

    private String getDescription(RoleName roleName) {
        return switch (roleName) {
            case ADMIN -> "Administrateur système RestoBook";
            case OWNER -> "Propriétaire/Gérant de restaurant";
            case STAFF -> "Employé de restaurant QuickEat";
            case CLIENT -> "Client de QuickEat";
        };
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getDescription(roleName))
                        .build();
                roleRepository.save(role);
                log.debug("Rôle créé: {}", roleName);
            } else {
                log.debug("Rôle déjà existant: {}", roleName);
            }
        }
    }

    private void initializeAdminUser() {

        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));

            User adminUser = User.builder()
                    .firstName("Admin")
                    .lastName("QuickEat")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone("+33123456789")
                    .role(adminRole)
                    .enabled(true)
                    .emailVerified(true)
                    .accountNonLocked(true)
                    .build();

            userRepository.save(adminUser);
            log.debug("Utilisateur admin créé: {} / Mot de passe {}: ", adminEmail, adminPassword);
        } else {
            log.debug("Utilisateur admin déjà existant: {}", adminEmail);
        }
    }
}
