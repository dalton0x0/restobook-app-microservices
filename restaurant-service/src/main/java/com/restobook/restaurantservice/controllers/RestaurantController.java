package com.restobook.restaurantservice.controllers;

import com.restobook.restaurantservice.clients.AuthServiceClient;
import com.restobook.restaurantservice.dtos.request.CreateRestaurantRequest;
import com.restobook.restaurantservice.dtos.request.OpeningHoursRequest;
import com.restobook.restaurantservice.dtos.request.UpdateRestaurantRequest;
import com.restobook.restaurantservice.dtos.response.*;
import com.restobook.restaurantservice.services.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Gestion des restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final AuthServiceClient authServiceClient;

    // Endpoints publiques

    @GetMapping
    @Operation(summary = "Lister les restaurants", description = "Récupère la liste paginée des restaurants actifs")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> getAllRestaurants(
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Requête HTTP GET /restaurants - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un restaurant", description = "Récupère les détails d'un restaurant par son ID")
    public ResponseEntity<@NonNull ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        log.debug("Requête HTTP GET /restaurants/{}", id);
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Recherche des restaurants par ville")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> getRestaurantsByCity(
            @PathVariable String city,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /restaurants/city/{}", city);
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getRestaurantsByCity(city, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    @GetMapping("/cuisine/{cuisineType}")
    @Operation(summary = "Restaurants par type de cuisine")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> getRestaurantsByCuisineType(
            @PathVariable String cuisineType,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /restaurants/cuisine/{}", cuisineType);
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getRestaurantsByCuisineType(cuisineType, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Restaurants les mieux notés")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> getTopRatedRestaurants(@PageableDefault Pageable pageable) {
        log.debug("Requête HTTP GET /restaurants/top-rated");
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getTopRatedRestaurants(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    @GetMapping("/{id}/opening-hours")
    @Operation(summary = "Horaires d'ouverture")
    public ResponseEntity<@NonNull ApiResponse<List<OpeningHoursResponse>>> getOpeningHours(@PathVariable Long id) {
        log.debug("Requête HTTP GET /restaurants/{}/opening-hours", id);
        List<OpeningHoursResponse> hours = restaurantService.getOpeningHours(id);
        return ResponseEntity.ok(ApiResponse.success(hours));
    }

    @GetMapping("/cities")
    @Operation(summary = "Liste des villes")
    public ResponseEntity<@NonNull ApiResponse<List<String>>> getAllCities() {
        log.debug("Requête HTTP GET /restaurants/cities");
        List<String> cities = restaurantService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/cuisine-types")
    @Operation(summary = "Liste des types de cuisine")
    public ResponseEntity<@NonNull ApiResponse<List<String>>> getAllCuisineTypes() {
        log.debug("Requête HTTP GET /restaurants/cuisine-types");
        List<String> types = restaurantService.getAllCuisineTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche des restaurants par mot-clé", description = "Recherche par nom, ville ou type de cuisine")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> searchRestaurants(
            @RequestParam String keyword,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /restaurants/search - Mot-clé: {}", keyword);
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.searchRestaurants(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer les restaurants", description = "Recherche avancée avec filtres")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> filterRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Double minRating,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /restaurants/filter - Ville: {}, Cuisine: {}, Note min: {}", city, cuisineType, minRating);
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getRestaurantsByFilters(city, cuisineType, minRating, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    // Endpoints authentifiés

    @PostMapping
    @Operation(summary = "Créer un restaurant", description = "Réservé aux OWNER et ADMIN")
    public ResponseEntity<@NonNull ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP POST /restaurants - Utilisateur: {}", tokenInfo.getEmail());
        RestaurantResponse restaurant = restaurantService.createRestaurant(request, tokenInfo.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant créé avec succès", restaurant));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un restaurant")
    public ResponseEntity<@NonNull ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PUT /restaurants/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        RestaurantResponse restaurant = restaurantService.updateRestaurant(id, request, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Restaurant mis à jour", restaurant));
    }

    @PutMapping("/{id}/opening-hours")
    @Operation(summary = "Modifier les horaires d'ouverture")
    public ResponseEntity<@NonNull ApiResponse<List<OpeningHoursResponse>>> updateOpeningHours(
            @PathVariable Long id,
            @Valid @RequestBody List<OpeningHoursRequest> requests,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PUT /restaurants/{}/opening-hours - Utilisateur: {}", id, tokenInfo.getEmail());
        List<OpeningHoursResponse> hours = restaurantService.updateOpeningHours(id, requests, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Horaires mis à jour", hours));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer un restaurant")
    public ResponseEntity<@NonNull ApiResponse<RestaurantResponse>> activateRestaurant(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /restaurants/{}/activate - Utilisateur: {}", id, tokenInfo.getEmail());
        RestaurantResponse restaurant = restaurantService.activateRestaurant(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Restaurant activé", restaurant));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver un restaurant")
    public ResponseEntity<@NonNull ApiResponse<RestaurantResponse>> deactivateRestaurant(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /restaurants/{}/deactivate - Utilisateur: {}", id, tokenInfo.getEmail());
        RestaurantResponse restaurant = restaurantService.deactivateRestaurant(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Restaurant désactivé", restaurant));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un restaurant")
    public ResponseEntity<@NonNull ApiResponse<Void>> deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP DELETE /restaurants/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        restaurantService.deleteRestaurant(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Restaurant supprimé"));
    }

    @GetMapping("/my-restaurants")
    @Operation(summary = "Mes restaurants", description = "Liste des restaurants du propriétaire connecté")
    public ResponseEntity<@NonNull ApiResponse<PageResponse<RestaurantResponse>>> getMyRestaurants(
            @RequestHeader("Authorization") String authHeader,
            @PageableDefault Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /restaurants/my-restaurants - Utilisateur: {}", tokenInfo.getEmail());
        Page<@NonNull RestaurantResponse> restaurants = restaurantService.getRestaurantsByOwner(tokenInfo.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(restaurants)));
    }

    private TokenValidationResponse validateToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return authServiceClient.validateToken(token);
    }
}
