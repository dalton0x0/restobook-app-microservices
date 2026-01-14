package com.restobook.restaurantservice.services;

import com.restobook.restaurantservice.dtos.request.CreateRestaurantRequest;
import com.restobook.restaurantservice.dtos.request.OpeningHoursRequest;
import com.restobook.restaurantservice.dtos.request.UpdateRestaurantRequest;
import com.restobook.restaurantservice.dtos.response.OpeningHoursResponse;
import com.restobook.restaurantservice.dtos.response.RestaurantResponse;
import com.restobook.restaurantservice.enums.DayOfWeek;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;

public interface RestaurantService {

    /**
     * Liste tous les restaurants actifs avec pagination
     */
    Page<@NonNull RestaurantResponse> getAllActiveRestaurants(Pageable pageable);

    /**
     * Liste tous les restaurants actifs et non actifs avec pagination
     */
    Page<@NonNull RestaurantResponse> getAllRestaurants(Pageable pageable);

    /**
     * Récupère un restaurant par son ID
     */
    RestaurantResponse getRestaurantById(Long id);

    /**
     * Liste les restaurants d'une ville avec pagination
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByCity(String city, Pageable pageable);

    /**
     * Liste les restaurants par type de cuisine avec pagination
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByCuisineType(String cuisineType, Pageable pageable);

    /**
     * Liste les restaurants les mieux notés avec pagination
     */
    Page<@NonNull RestaurantResponse> getTopRatedRestaurants(Pageable pageable);

    /**
     * Récupère les horaires d'ouverture d'un restaurant
     */
    List<OpeningHoursResponse> getOpeningHours(Long restaurantId);

    /**
     * Récupère les horaires d'ouverture d'un restaurant pour un jour spécifique.
     * Utilisé par le Booking Service pour générer les créneaux disponibles.
     */
    List<OpeningHoursResponse> getOpeningHoursByDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    /**
     * Liste toutes les villes où des restaurants sont présents
     */
    List<String> getAllCities();

    /**
     * Liste tous les types de cuisine disponibles
     */
    List<String> getAllCuisineTypes();

    /**
     * Recherche des restaurants par mot-clé
     */
    Page<@NonNull RestaurantResponse> searchRestaurants(String keyword, Pageable pageable);

    /**
     * Recherche avancée des restaurants avec filtres multiples
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByFilters(String city, String cuisineType, Double minRating, Pageable pageable);

    /**
     * Crée un nouveau restaurant
     */
    RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId);

    /**
     * Met à jour un restaurant
     */
    RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request, Long userId, String role);

    /**
     * Met à jour les horaires d'ouverture d'un restaurant
     */
    List<OpeningHoursResponse> updateOpeningHours(Long restaurantId, List<OpeningHoursRequest> requests, Long userId, String role);

    /**
     * Met à jour la note moyenne et le nombre d'avis d'un restaurant
     */
    void updateRestaurantRating(Long restaurantId, Double newRating, Integer totalReviews);

    /**
     * Supprime un restaurant
     */
    void deleteRestaurant(Long id, Long userId, String role);

    /**
     * Active un restaurant
     */
    RestaurantResponse activateRestaurant(Long id, Long userId, String role);

    /**
     * Désactive un restaurant
     */
    RestaurantResponse deactivateRestaurant(Long id, Long userId, String role);

    /**
     * Liste les restaurants d'un propriétaire avec pagination
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByOwner(Long ownerId, Pageable pageable);

    /**
     * Vérifie si un restaurant existe
     */
    boolean restaurantExists(Long id);

    /**
     * Récupère la capacité totale d'un restaurant
     */
    Integer getRestaurantCapacity(Long id);

    /**
     * Vérifie si un restaurant est ouvert à un jour et une heure donnés
     */
    boolean isRestaurantOpen(Long id, DayOfWeek dayOfWeek, LocalTime time);
}
