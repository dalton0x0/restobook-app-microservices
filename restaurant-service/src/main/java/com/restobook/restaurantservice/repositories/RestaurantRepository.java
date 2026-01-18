package com.restobook.restaurantservice.repositories;

import com.restobook.restaurantservice.entities.Restaurant;
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
public interface RestaurantRepository extends JpaRepository<@NonNull Restaurant, @NonNull Long> {

    /**
     * Récupère une page de restaurants actifs d'une ville spécifique.
     * La recherche ignore la casse.
     *
     * @param city le nom de la ville
     * @param pageable les informations de pagination
     * @return une page de restaurants actifs de la ville
     */
    Page<@NonNull Restaurant> findByCityIgnoreCaseAndActiveTrue(String city, Pageable pageable);

    /**
     * Récupère tous les restaurants d'un propriétaire.
     *
     * @param ownerId l'identifiant du propriétaire
     * @return une liste de restaurants
     */
    List<@NonNull Restaurant> findByOwnerId(Long ownerId);

    /**
     * Récupère une page de restaurants d'un propriétaire.
     *
     * @param ownerId l'identifiant du propriétaire
     * @param pageable les informations de pagination
     * @return une page de restaurants du propriétaire
     */
    Page<@NonNull Restaurant> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Récupère une page de tous les restaurants actifs.
     *
     * @param pageable les informations de pagination
     * @return une page de restaurants actifs
     */
    Page<@NonNull Restaurant> findByActiveTrue(Pageable pageable);

    /**
     * Recherche des restaurants actifs par mot-clé dans le nom, la ville ou le type de cuisine.
     * La recherche ignore la casse.
     *
     * @param keyword le mot-clé de recherche
     * @param pageable les informations de pagination
     * @return une page de restaurants correspondant à la recherche
     */
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<@NonNull Restaurant> searchRestaurants(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Récupère une page de restaurants actifs par type de cuisine.
     * La recherche ignore la casse.
     *
     * @param cuisineType le type de cuisine
     * @param pageable les informations de pagination
     * @return une page de restaurants du type de cuisine spécifié
     */
    Page<@NonNull Restaurant> findByCuisineTypeIgnoreCaseAndActiveTrue(String cuisineType, Pageable pageable);

    /**
     * Récupère une page de restaurants actifs ayant un nombre minimum d'avis, triés par note décroissante.
     *
     * @param totalReviews le nombre minimum d'avis requis
     * @param pageable les informations de pagination
     * @return une page de restaurants triés par note décroissante
     */
    Page<@NonNull Restaurant> findByActiveTrueAndTotalReviewsGreaterThanOrderByAverageRatingDesc(Integer totalReviews, Pageable pageable);

    /**
     * Vérifie l'existence d'un restaurant pour un propriétaire spécifique.
     *
     * @param id l'identifiant du restaurant
     * @param ownerId l'identifiant du propriétaire
     * @return true si le restaurant existe et appartient au propriétaire, false sinon
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Compte le nombre de restaurants d'un propriétaire.
     *
     * @param ownerId l'identifiant du propriétaire
     * @return le nombre de restaurants
     */
    long countByOwnerId(Long ownerId);

    /**
     * Recherche un restaurant actif par son identifiant.
     *
     * @param id l'identifiant du restaurant
     * @return un Optional contenant le restaurant si trouvé et actif
     */
    Optional<Restaurant> findByIdAndActiveTrue(Long id);

    /**
     * Recherche des restaurants actifs avec filtres multiples.
     * Les paramètres null sont ignorés dans le filtre.
     *
     * @param city le nom de la ville (optionnel)
     * @param cuisineType le type de cuisine (optionnel)
     * @param minRating la note minimale (optionnel)
     * @param pageable les informations de pagination
     * @return une page de restaurants correspondant aux filtres
     */
    @Query("SELECT r FROM Restaurant r WHERE r.active = true " +
            "AND (:city IS NULL OR LOWER(r.city) = LOWER(:city)) " +
            "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisineType)) " +
            "AND (:minRating IS NULL OR r.averageRating >= :minRating)")
    Page<@NonNull Restaurant> findByFilters(
            @Param("city") String city,
            @Param("cuisineType") String cuisineType,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    /**
     * Récupère la liste distincte de toutes les villes avec lesquelles des restaurants actifs sont présents.
     * Les résultats sont triés par ordre alphabétique.
     *
     * @return une liste de noms de villes
     */
    @Query("SELECT DISTINCT r.city FROM Restaurant r WHERE r.active = true ORDER BY r.city")
    List<String> findDistinctCities();

    /**
     * Récupère la liste distincte de tous les types de cuisine disponibles pour les restaurants actifs.
     * Les résultats sont triés par ordre alphabétique.
     *
     * @return une liste de types de cuisine
     */
    @Query("SELECT DISTINCT r.cuisineType FROM Restaurant r WHERE r.active = true AND r.cuisineType IS NOT NULL ORDER BY r.cuisineType")
    List<String> findDistinctCuisineTypes();
}
