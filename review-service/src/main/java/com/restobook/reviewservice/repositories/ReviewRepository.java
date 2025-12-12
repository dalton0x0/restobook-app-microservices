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

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    Optional<Review> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    Page<Review> findByRestaurantIdAndIsVisibleTrueOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    // Tous les avis (owner/admin)
    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Review> findByRestaurantIdAndRatingAndIsVisibleTrueOrderByCreatedAtDesc(
            Long restaurantId, Integer rating, Pageable pageable);

    Page<Review> findByRestaurantIdAndIsVerifiedTrueAndIsVisibleTrueOrderByCreatedAtDesc(
            Long restaurantId, Pageable pageable);

    // Stats
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true")
    Double calculateAverageRating(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true")
    Long countByRestaurantIdAndVisible(@Param("restaurantId") Long restaurantId);

    // Distribution des notes
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("restaurantId") Long restaurantId);

    @Query("SELECT r FROM Review r WHERE r.isVisible = true ORDER BY r.createdAt DESC")
    Page<Review> findRecentReviews(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId AND r.ownerResponse IS NULL ORDER BY r.createdAt DESC")
    List<Review> findUnansweredReviews(@Param("restaurantId") Long restaurantId);

    // Moyenne et count pour mise à jour du restaurant
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true")
    Object[] getAverageAndCount(@Param("restaurantId") Long restaurantId);

    // Avis par note
    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurantId = :restaurantId AND r.rating = :rating AND r.isVisible = true")
    Long countByRestaurantIdAndRating(@Param("restaurantId") Long restaurantId, @Param("rating") Integer rating);

    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId AND r.isVisible = true " +
            "AND LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY r.createdAt DESC")
    Page<Review> searchByKeyword(@Param("restaurantId") Long restaurantId, @Param("keyword") String keyword, Pageable pageable);
}
