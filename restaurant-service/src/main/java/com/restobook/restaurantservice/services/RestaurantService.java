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
     * Liste tous les restaurants actifs avec pagination.
     *
     * @param pageable les informations de pagination
     * @return une page de restaurants actifs
     */
    Page<@NonNull RestaurantResponse> getAllActiveRestaurants(Pageable pageable);

    /**
     * Liste tous les restaurants actifs et non actifs avec pagination.
     *
     * @param pageable les informations de pagination
     * @return une page de tous les restaurants
     */
    Page<@NonNull RestaurantResponse> getAllRestaurants(Pageable pageable);

    /**
     * Récupère un restaurant par son identifiant.
     *
     * @param id l'identifiant du restaurant
     * @return les informations du restaurant
     */
    RestaurantResponse getRestaurantById(Long id);

    /**
     * Liste les restaurants d'une ville avec pagination.
     *
     * @param city le nom de la ville
     * @param pageable les informations de pagination
     * @return une page de restaurants de la ville spécifiée
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByCity(String city, Pageable pageable);

    /**
     * Liste les restaurants par type de cuisine avec pagination.
     *
     * @param cuisineType le type de cuisine
     * @param pageable les informations de pagination
     * @return une page de restaurants du type de cuisine spécifié
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByCuisineType(String cuisineType, Pageable pageable);

    /**
     * Liste les restaurants les mieux notés avec pagination.
     *
     * @param pageable les informations de pagination
     * @return une page de restaurants triés par note décroissante
     */
    Page<@NonNull RestaurantResponse> getTopRatedRestaurants(Pageable pageable);

    /**
     * Récupère les horaires d'ouverture d'un restaurant pour tous les jours.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste d'horaires d'ouverture
     */
    List<OpeningHoursResponse> getOpeningHours(Long restaurantId);

    /**
     * Récupère les horaires d'ouverture d'un restaurant pour un jour spécifique.
     * Utilisé par le Booking Service pour générer les créneaux disponibles.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dayOfWeek le jour de la semaine
     * @return une liste d'horaires d'ouverture pour le jour spécifié
     */
    List<OpeningHoursResponse> getOpeningHoursByDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    /**
     * Liste toutes les villes où des restaurants sont présents.
     *
     * @return une liste de noms de villes
     */
    List<String> getAllCities();

    /**
     * Liste tous les types de cuisine disponibles.
     *
     * @return une liste de types de cuisine
     */
    List<String> getAllCuisineTypes();

    /**
     * Recherche des restaurants par mot-clé avec pagination.
     *
     * @param keyword le mot-clé de recherche
     * @param pageable les informations de pagination
     * @return une page de restaurants correspondant à la recherche
     */
    Page<@NonNull RestaurantResponse> searchRestaurants(String keyword, Pageable pageable);

    /**
     * Recherche avancée des restaurants avec filtres multiples.
     *
     * @param city le nom de la ville (optionnel)
     * @param cuisineType le type de cuisine (optionnel)
     * @param minRating la note minimale (optionnel)
     * @param pageable les informations de pagination
     * @return une page de restaurants correspondant aux filtres
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByFilters(String city, String cuisineType, Double minRating, Pageable pageable);

    /**
     * Crée un nouveau restaurant.
     *
     * @param request les informations du restaurant à créer
     * @param ownerId l'identifiant du propriétaire
     * @return les informations du restaurant créé
     */
    RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId);

    /**
     * Met à jour les informations d'un restaurant.
     *
     * @param id l'identifiant du restaurant
     * @param request les nouvelles informations du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du restaurant mis à jour
     */
    RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request, Long userId, String role);

    /**
     * Met à jour les horaires d'ouverture d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param requests la liste des nouveaux horaires d'ouverture
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return la liste des horaires d'ouverture mis à jour
     */
    List<OpeningHoursResponse> updateOpeningHours(Long restaurantId, List<OpeningHoursRequest> requests, Long userId, String role);

    /**
     * Met à jour la note moyenne et le nombre d'avis d'un restaurant.
     * Utilisé par le Review Service après l'ajout ou la suppression d'un avis.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param newRating la nouvelle note moyenne
     * @param totalReviews le nombre total d'avis
     */
    void updateRestaurantRating(Long restaurantId, Double newRating, Integer totalReviews);

    /**
     * Supprime un restaurant de façon définitive.
     *
     * @param id l'identifiant du restaurant à supprimer
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     */
    void deleteRestaurant(Long id, Long userId, String role);

    /**
     * Active un restaurant.
     *
     * @param id l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du restaurant activé
     */
    RestaurantResponse activateRestaurant(Long id, Long userId, String role);

    /**
     * Désactive un restaurant.
     *
     * @param id l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du restaurant désactivé
     */
    RestaurantResponse deactivateRestaurant(Long id, Long userId, String role);

    /**
     * Liste les restaurants d'un propriétaire avec pagination.
     *
     * @param ownerId l'identifiant du propriétaire
     * @param pageable les informations de pagination
     * @return une page de restaurants du propriétaire
     */
    Page<@NonNull RestaurantResponse> getRestaurantsByOwner(Long ownerId, Pageable pageable);

    /**
     * Vérifie si un restaurant existe.
     *
     * @param id l'identifiant du restaurant
     * @return true si le restaurant existe, false sinon
     */
    boolean restaurantExists(Long id);

    /**
     * Récupère la capacité totale d'un restaurant.
     *
     * @param id l'identifiant du restaurant
     * @return la capacité totale en nombre de couverts
     */
    Integer getRestaurantCapacity(Long id);

    /**
     * Vérifie si un restaurant est ouvert à un jour et une heure donnés.
     *
     * @param id l'identifiant du restaurant
     * @param dayOfWeek le jour de la semaine
     * @param time l'heure à vérifier
     * @return true si le restaurant est ouvert, false sinon
     */
    boolean isRestaurantOpen(Long id, DayOfWeek dayOfWeek, LocalTime time);
}
