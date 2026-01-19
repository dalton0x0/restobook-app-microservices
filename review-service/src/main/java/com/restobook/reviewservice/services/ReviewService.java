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
     * Crée un nouvel avis pour un restaurant.
     *
     * @param request les informations de l'avis à créer
     * @param userId l'identifiant de l'utilisateur créant l'avis
     * @param userName le nom de l'utilisateur
     * @return les informations de l'avis créé
     */
    ReviewResponse createReview(CreateReviewRequest request, Long userId, String userName);

    /**
     * Récupère un avis par son identifiant.
     *
     * @param id l'identifiant de l'avis
     * @return les informations de l'avis
     */
    ReviewResponse getReviewById(Long id);

    /**
     * Met à jour les informations d'un avis existant.
     *
     * @param id l'identifiant de l'avis
     * @param request les nouvelles informations de l'avis
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de l'avis mis à jour
     */
    ReviewResponse updateReview(Long id, UpdateReviewRequest request, Long userId, String role);

    /**
     * Supprime un avis de façon définitive.
     *
     * @param id l'identifiant de l'avis à supprimer
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     */
    void deleteReview(Long id, Long userId, String role);

    /**
     * Liste les avis visibles d'un restaurant avec pagination.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page d'avis visibles
     */
    Page<ReviewResponse> getReviewsByRestaurant(Long restaurantId, Pageable pageable);

    /**
     * Liste tous les avis d'un restaurant avec pagination, incluant les avis masqués.
     * Accessible uniquement aux administrateurs et propriétaires.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page de tous les avis
     */
    Page<ReviewResponse> getAllReviewsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable);

    /**
     * Liste tous les avis rédigés par un utilisateur avec pagination.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page d'avis de l'utilisateur
     */
    Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable);

    /**
     * Liste les avis d'un restaurant filtrés par note avec pagination.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param rating la note à filtrer (1 à 5)
     * @param pageable les informations de pagination
     * @return une page d'avis ayant la note spécifiée
     */
    Page<ReviewResponse> getReviewsByRestaurantAndRating(Long restaurantId, Integer rating, Pageable pageable);

    /**
     * Liste les avis vérifiés d'un restaurant avec pagination.
     * Les avis vérifiés proviennent de réservations confirmées.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page d'avis vérifiés
     */
    Page<ReviewResponse> getVerifiedReviews(Long restaurantId, Pageable pageable);

    /**
     * Recherche des avis par mot-clé dans les commentaires d'un restaurant avec pagination.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param keyword le mot-clé de recherche
     * @param pageable les informations de pagination
     * @return une page d'avis correspondant à la recherche
     */
    Page<ReviewResponse> searchReviews(Long restaurantId, String keyword, Pageable pageable);

    /**
     * Récupère les statistiques des avis d'un restaurant.
     * Inclut la note moyenne, le nombre total d'avis et la distribution des notes.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return les statistiques des avis
     */
    ReviewStatsResponse getReviewStats(Long restaurantId);

    /**
     * Ajoute une réponse du propriétaire à un avis.
     *
     * @param reviewId l'identifiant de l'avis
     * @param request les informations de la réponse
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de l'avis avec la réponse ajoutée
     */
    ReviewResponse addOwnerResponse(Long reviewId, OwnerResponseRequest request, Long userId, String role);

    /**
     * Supprime la réponse du propriétaire d'un avis.
     *
     * @param reviewId l'identifiant de l'avis
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de l'avis sans réponse
     */
    ReviewResponse deleteOwnerResponse(Long reviewId, Long userId, String role);

    /**
     * Liste les avis sans réponse du propriétaire pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @return une liste d'avis sans réponse
     */
    List<ReviewResponse> getUnansweredReviews(Long restaurantId, Long userId, String role);

    /**
     * Active ou désactive la visibilité d'un avis.
     *
     * @param id l'identifiant de l'avis
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de l'avis avec le statut de visibilité mis à jour
     */
    ReviewResponse toggleVisibility(Long id, Long userId, String role);

    /**
     * Récupère l'avis d'un utilisateur pour un restaurant spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur
     * @return les informations de l'avis de l'utilisateur pour ce restaurant
     */
    ReviewResponse getMyReviewForRestaurant(Long restaurantId, Long userId);

    /**
     * Vérifie si un utilisateur a déjà évalué un restaurant.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @return true si l'utilisateur a déjà laissé un avis, false sinon
     */
    boolean hasReviewedRestaurant(Long userId, Long restaurantId);

    /**
     * Vérifie si un utilisateur peut évaluer un restaurant.
     * L'utilisateur doit avoir une réservation terminée et ne pas avoir déjà laissé d'avis.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @return true si l'utilisateur peut laisser un avis, false sinon
     */
    boolean canReviewRestaurant(Long userId, Long restaurantId);
}
