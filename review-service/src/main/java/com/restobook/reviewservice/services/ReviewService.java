package com.restobook.reviewservice.services;

import com.restobook.reviewservice.dtos.request.CreateReviewRequest;
import com.restobook.reviewservice.dtos.request.OwnerResponseRequest;
import com.restobook.reviewservice.dtos.request.UpdateReviewRequest;
import com.restobook.reviewservice.dtos.response.ReviewResponse;
import com.restobook.reviewservice.dtos.response.ReviewStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    /**
     * Crée un nouvel avis pour un restaurant
     */
    ReviewResponse createReview(CreateReviewRequest request, Long userId, String userName);

    /**
     * Récupère un avis par son ID
     */
    ReviewResponse getReviewById(Long id);

    /**
     * Met à jour un avis
     */
    ReviewResponse updateReview(Long id, UpdateReviewRequest request, Long userId, String role);

    /**
     * Supprime un avis
     */
    void deleteReview(Long id, Long userId, String role);

    /**
     * Liste les avis visibles d'un restaurant avec pagination
     */
    Page<ReviewResponse> getReviewsByRestaurant(Long restaurantId, Pageable pageable);

    /**
     * Liste tous les avis d'un restaurant avec pagination (inclut les avis masqués)
     */
    Page<ReviewResponse> getAllReviewsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable);

    /**
     * Liste les avis d'un utilisateur avec pagination
     */
    Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable);

    /**
     * Liste les avis d'un restaurant par note avec pagination
     */
    Page<ReviewResponse> getReviewsByRestaurantAndRating(Long restaurantId, Integer rating, Pageable pageable);

    /**
     * Liste les avis vérifiés d'un restaurant avec pagination
     */
    Page<ReviewResponse> getVerifiedReviews(Long restaurantId, Pageable pageable);

    /**
     * Recherche des avis par mot-clé dans un restaurant avec pagination
     */
    Page<ReviewResponse> searchReviews(Long restaurantId, String keyword, Pageable pageable);

    /**
     * Récupère les statistiques d'avis d'un restaurant
     */
    ReviewStatsResponse getReviewStats(Long restaurantId);

    /**
     * Ajoute une réponse du propriétaire à un avis
     */
    ReviewResponse addOwnerResponse(Long reviewId, OwnerResponseRequest request, Long userId, String role);

    /**
     * Supprime la réponse du propriétaire d'un avis
     */
    ReviewResponse deleteOwnerResponse(Long reviewId, Long userId, String role);

    /**
     * Liste les avis sans réponse d'un restaurant
     */
    List<ReviewResponse> getUnansweredReviews(Long restaurantId, Long userId, String role);

    /**
     * Active ou désactive la visibilité d'un avis
     */
    ReviewResponse toggleVisibility(Long id, Long userId, String role);

    /**
     * Récupère l'avis d'un utilisateur pour un restaurant spécifique
     */
    ReviewResponse getMyReviewForRestaurant(Long restaurantId, Long userId);

    /**
     * Vérifie si un utilisateur a déjà évalué un restaurant
     */
    boolean hasReviewedRestaurant(Long userId, Long restaurantId);

    /**
     * Vérifie si un utilisateur peut évaluer un restaurant
     */
    boolean canReviewRestaurant(Long userId, Long restaurantId);
}
