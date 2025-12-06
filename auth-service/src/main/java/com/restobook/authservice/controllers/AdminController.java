package com.restobook.authservice.controllers;

import com.restobook.authservice.dtos.request.CreateUserRequest;
import com.restobook.authservice.dtos.request.UpdateRoleRequest;
import com.restobook.authservice.dtos.request.UpdateUserRequest;
import com.restobook.authservice.dtos.response.ApiResponse;
import com.restobook.authservice.dtos.response.PageResponse;
import com.restobook.authservice.dtos.response.UserResponse;
import com.restobook.authservice.enums.RoleName;
import com.restobook.authservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Endpoints d'administration des utilisateurs (ADMIN uniquement)")
public class AdminController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Liste des utilisateurs", description = "Récupère la liste paginée de tous les utilisateurs")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Requête HTTP GET /admin/users - Page: {}, Taille: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des utilisateurs", description = "Recherche des utilisateurs par mot-clé")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Requête HTTP GET /admin/users/search avec mot-clé: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserResponse> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/role/{roleName}")
    @Operation(summary = "Utilisateurs par rôle", description = "Récupère les utilisateurs ayant un rôle spécifique")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<UserResponse>>> getUsersByRole(
            @PathVariable RoleName roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Requête HTTP GET /admin/users/role/{} - Page: {}, Taille: {}", roleName, page, size);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserResponse> users = userService.getUsersByRole(roleName, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un utilisateur", description = "Récupère les détails d'un utilisateur par son ID")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.debug("Requête HTTP GET /admin/users/{}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur avec un rôle spécifique")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.debug("Requête HTTP POST /admin/users - Email: {}, Rôle: {}", request.getEmail(), request.getRoleName());
        UserResponse user = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Utilisateur créé avec succès", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.debug("Requête HTTP PUT /admin/users/{}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour avec succès", user));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Modifier le rôle", description = "Change le rôle d'un utilisateur")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.debug("Requête HTTP PATCH /admin/users/{}/role vers: {}", id, request.getRoleName());
        UserResponse user = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Rôle mis à jour avec succès", user));
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Activer un utilisateur", description = "Active le compte d'un utilisateur")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> enableUser(@PathVariable Long id) {
        log.debug("Requête HTTP PATCH /admin/users/{}/enable", id);
        UserResponse user = userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur activé avec succès", user));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Désactiver un utilisateur", description = "Désactive le compte d'un utilisateur")
    public ResponseEntity<@NonNull ApiResponse<UserResponse>> disableUser(@PathVariable Long id) {
        log.debug("Requête HTTP PATCH /admin/users/{}/disable", id);
        UserResponse user = userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur désactivé avec succès", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime définitivement un utilisateur")
    public ResponseEntity<@NonNull ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.debug("Requête HTTP DELETE /admin/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès"));
    }
}
