package com.restobook.reviewservice.repositories;

import com.restobook.reviewservice.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Vérifie si un utilisateur a déjà évalué un restaurant.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @return true si un avis existe, false sinon
     */
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    /**
     * Recherche l'avis d'un utilisateur pour un restaurant spécifique.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @return un Optional contenant l'avis si trouvé
     */
    Optional<Review> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    /**
     * Récupère une page d'avis visibles d'un restaurant triés par date décroissante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page d'avis visibles
     */
    Page<Review> findByRestaurantIdAndIsVisibleTrueOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    /**
     * Récupère une page de tous les avis d'un restaurant triés par date décroissante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page d'avis incluant les avis masqués
     */
    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    /**
     * Récupère une page d'avis d'un utilisateur triés par date décroissante.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page d'avis de l'utilisateur
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Récupère une page d'avis visibles d'un restaurant filtrés par note, triés par date décroissante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param rating la note à filtrer
     * @param pageable les informations de pagination
     * @return une page d'avis avec la note spécifiée
     */
    Page<Review> findByRestaurantIdAndRatingAndIsVisibleTrueOrderByCreatedAtDesc(
            Long restaurantId, Integer rating, Pageable pageable);

    /**
     * Récupère une page d'avis vérifiés et visibles d'un restaurant triés par date décroissante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page d'avis vérifiés
     */
    Page<Review> findByRestaurantIdAndIsVerifiedTrueAndIsVisibleTrueOrderByCreatedAtDesc(
            Long restaurantId, Pageable pageable);

    /**
     * Calcule la note moyenne des avis visibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return la note moyenne ou null si aucun avis visible
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true")
    Double calculateAverageRating(@Param("restaurantId") Long restaurantId);

    /**
     * Compte le nombre d'avis visibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return le nombre d'avis visibles
     */
    long countByRestaurantIdAndIsVisibleTrue(Long restaurantId);

    /**
     * Récupère la distribution des notes pour un restaurant.
     * Retourne le nombre d'avis pour chaque note (1 à 5 étoiles).
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste d'objets contenant la note et le nombre d'avis
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("restaurantId") Long restaurantId);

    /**
     * Récupère une page de tous les avis visibles triés par date décroissante.
     *
     * @param pageable les informations de pagination
     * @return une page d'avis visibles
     */
    Page<Review> findByIsVisibleTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Récupère les avis d'un restaurant sans réponse du propriétaire, triés par date décroissante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste d'avis sans réponse
     */
    List<Review> findByRestaurantIdAndOwnerResponseIsNullOrderByCreatedAtDesc(Long restaurantId);

    /**
     * Récupère la note moyenne et le nombre total d'avis visibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return un tableau contenant la note moyenne et le nombre d'avis
     */
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true")
    Object[] getAverageAndCount(@Param("restaurantId") Long restaurantId);

    /**
     * Compte le nombre d'avis visibles d'un restaurant pour une note spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param rating la note à compter
     * @return le nombre d'avis avec la note spécifiée
     */
    long countByRestaurantIdAndRatingAndIsVisibleTrue(Long restaurantId, Integer rating);

    /**
     * Recherche des avis visibles par mot-clé dans les commentaires pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param keyword le mot-clé de recherche
     * @param pageable les informations de pagination
     * @return une page d'avis correspondant à la recherche
     */
    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true " +
            "AND LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY r.createdAt Desc")
    Page<Review> searchByKeyword(@Param("restaurantId") Long restaurantId, @Param("keyword") String keyword, Pageable pageable);
}
