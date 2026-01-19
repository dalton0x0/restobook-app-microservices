package com.restobook.reviewservice.services.impl;

import com.restobook.reviewservice.clients.BookingServiceClient;
import com.restobook.reviewservice.clients.RestaurantServiceClient;
import com.restobook.reviewservice.configs.ReviewProperties;
import com.restobook.reviewservice.constants.ExceptionConst;
import com.restobook.reviewservice.dtos.request.CreateReviewRequest;
import com.restobook.reviewservice.dtos.request.OwnerResponseRequest;
import com.restobook.reviewservice.dtos.request.UpdateReviewRequest;
import com.restobook.reviewservice.dtos.response.ReviewResponse;
import com.restobook.reviewservice.dtos.response.ReviewStatsResponse;
import com.restobook.reviewservice.entities.Review;
import com.restobook.reviewservice.exceptions.BusinessException;
import com.restobook.reviewservice.exceptions.ForbiddenException;
import com.restobook.reviewservice.exceptions.ResourceNotFoundException;
import com.restobook.reviewservice.exceptions.ReviewException;
import com.restobook.reviewservice.repositories.ReviewRepository;
import com.restobook.reviewservice.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantServiceClient restaurantServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final ReviewProperties reviewProperties;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, Long userId, String userName) {
        log.info("Création d'un avis par l'utilisateur ID: {} pour le restaurant ID: {}", userId, request.getRestaurantId());

        if (!restaurantServiceClient.restaurantExists(request.getRestaurantId())) {
            throw new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId());
        }

        if (hasReviewedRestaurant(userId, request.getRestaurantId())) {
            throw ReviewException.alreadyReviewed();
        }

        boolean hasCompletedBooking = bookingServiceClient.hasCompletedBooking(userId, request.getRestaurantId());
        if (!hasCompletedBooking) {
            throw ReviewException.noCompletedBooking();
        }

        validateRating(request.getRating());
        Review review = buildNewReview(request, userId, userName);
        Review savedReview = reviewRepository.save(review);
        updateRestaurantRating(request.getRestaurantId());
        log.info("Avis crée avec succès: ID {}", savedReview.getId());

        return enrichReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        log.debug("Récuperation de l'avis ID: {}", id);
        Review review = findReviewByIdOrThrow(id);
        return enrichReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, UpdateReviewRequest request, Long userId, String role) {
        log.info("Modification de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = findReviewByIdOrThrow(id);

        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez modifier que vos propres avis");
        }

        updateReviewFields(review, request);
        Review updatedReview = reviewRepository.save(review);
        updateRestaurantRating(review.getRestaurantId());
        log.info("Avis ID: {} mis à jour", id);

        return enrichReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId, String role) {
        log.info("Suppression de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = findReviewByIdOrThrow(id);

        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez supprimer que vos propres avis");
        }

        Long restaurantId = review.getRestaurantId();
        reviewRepository.delete(review);
        updateRestaurantRating(restaurantId);
        log.info("Avis ID: {} supprimé", id);
    }

    // RECHERCHE

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        log.debug("Récuperation des avis du restaurant ID: {}", restaurantId);
        return reviewRepository.findByRestaurantIdAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable) {
        log.debug("Récuperation de tous les avis du restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        log.debug("Récuperation des avis de l'utilisateur ID: {}", userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurantAndRating(Long restaurantId, Integer rating, Pageable pageable) {
        log.debug("Récuperation des avis du restaurant ID: {} avec la note: {}", restaurantId, rating);
        return reviewRepository.findByRestaurantIdAndRatingAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, rating, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getVerifiedReviews(Long restaurantId, Pageable pageable) {
        log.debug("Récuperation des avis vérifiés du restaurant ID: {}", restaurantId);
        return reviewRepository.findByRestaurantIdAndIsVerifiedTrueAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> searchReviews(Long restaurantId, String keyword, Pageable pageable) {
        log.debug("Recherche d'avis pour le restaurant ID: {} avec le mot-clé: {}", restaurantId, keyword);
        return reviewRepository.searchByKeyword(restaurantId, keyword, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsResponse getReviewStats(Long restaurantId) {
        log.debug("Calcul des statistiques d'avis pour le restaurant ID: {}", restaurantId);

        Double averageRating = reviewRepository.calculateAverageRating(restaurantId);
        Long totalReviews = reviewRepository.countByRestaurantIdAndIsVisibleTrue(restaurantId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(restaurantId);

        // Initialiser la distribution avec toutes les notes (1-5)
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }

        // Remplir avec les donnees réelles
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }

        return ReviewStatsResponse.builder()
                .restaurantId(restaurantId)
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews)
                .ratingDistribution(ratingDistribution)
                .build();
    }

    // OWNER

    @Override
    @Transactional
    public ReviewResponse addOwnerResponse(Long reviewId, OwnerResponseRequest request, Long userId, String role) {
        log.info("Ajout d'une réponse propriétaire a l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = findReviewByIdOrThrow(reviewId);
        checkRestaurantAccess(review.getRestaurantId(), userId, role);
        review.setOwnerResponse(request.getResponse());
        review.setOwnerResponseAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        log.info("Réponse propriétaire ajoutée a l'avis ID: {}", reviewId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse deleteOwnerResponse(Long reviewId, Long userId, String role) {
        log.info("Suppression de la réponse propriétaire de l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = findReviewByIdOrThrow(reviewId);
        checkRestaurantAccess(review.getRestaurantId(), userId, role);
        review.setOwnerResponse(null);
        review.setOwnerResponseAt(null);
        Review savedReview = reviewRepository.save(review);
        log.info("Réponse propriétaire supprimée de l'avis ID: {}", reviewId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUnansweredReviews(Long restaurantId, Long userId, String role) {
        log.debug("Récuperation des avis sans réponse pour le restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return reviewRepository.findByRestaurantIdAndOwnerResponseIsNullOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    // ADMIN

    @Override
    @Transactional
    public ReviewResponse toggleVisibility(Long id, Long userId, String role) {
        log.info("Modification de la visibilité de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        // Vérifier les permissions admin
        if (!isAdmin(role)) {
            throw new ForbiddenException("Seuls les administrateurs peuvent modifier la visibilité des avis");
        }

        Review review = findReviewByIdOrThrow(id);
        review.setIsVisible(!review.getIsVisible());
        Review updatedReview = reviewRepository.save(review);
        updateRestaurantRating(review.getRestaurantId());
        log.info("Visibilité de l'avis ID: {} modifiée: {}", id, updatedReview.getIsVisible());

        return ReviewResponse.fromEntity(updatedReview);
    }

    // MON AVIS

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForRestaurant(Long restaurantId, Long userId) {
        log.debug("Récuperation de l'avis de l'utilisateur ID: {} pour le restaurant ID: {}", userId, restaurantId);
        return reviewRepository.findByUserIdAndRestaurantId(userId, restaurantId)
                .map(this::enrichReviewResponse)
                .orElse(null);
    }

    @Override
    public boolean hasReviewedRestaurant(Long userId, Long restaurantId) {
        log.debug("Vérification si l'utilisateur ID: {} a deja évalue le restaurant ID: {}", userId, restaurantId);
        return reviewRepository.existsByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReviewRestaurant(Long userId, Long restaurantId) {
        log.debug("Vérification si l'utilisateur ID: {} peut évaluer le restaurant ID: {}", userId, restaurantId);

        // Ne peut pas évaluer si deja fait
        if (hasReviewedRestaurant(userId, restaurantId)) {
            return false;
        }

        // Doit avoir une reservation terminée
        return bookingServiceClient.hasCompletedBooking(userId, restaurantId);
    }


    /**
     * Recherche un avis par ID ou lève une exception si non trouvé.
     *
     * @param id l'identifiant de l'avis
     * @return l'avis trouvé
     * @throws ResourceNotFoundException si l'avis n'existe pas
     */
    private Review findReviewByIdOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Avis non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("id", id);
                });
    }

    /**
     * Construit un nouvel avis à partir de la requête de creation.
     *
     * @param request la requête de creation contenant les informations de l'avis
     * @param userId l'identifiant de l'utilisateur
     * @param userName le nom de l'utilisateur
     * @return le nouvel avis construit
     */
    private Review buildNewReview(CreateReviewRequest request, Long userId, String userName) {
        return Review.builder()
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .comment(request.getComment())
                .userName(userName)
                .isVerified(true)
                .isVisible(true)
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un avis.
     *
     * @param review l'avis à modifier
     * @param request la requête de mise à jour contenant les nouveaux champs
     */
    private void updateReviewFields(Review review, UpdateReviewRequest request) {
        if (request.getRating() != null) {
            validateRating(request.getRating());
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
    }

    /**
     * Valide qu'une note est dans la plage autorisée.
     *
     * @param rating la note à valider
     * @throws ReviewException si la note est invalide
     */
    private void validateRating(Integer rating) {
        if (rating < reviewProperties.getMinRating() || rating > reviewProperties.getMaxRating()) {
            throw ReviewException.invalidRating(reviewProperties.getMinRating(), reviewProperties.getMaxRating());
        }
    }

    /**
     * Met à jour la note moyenne d'un restaurant.
     * Récupère les statistiques et notifie le restaurant-service.
     *
     * @param restaurantId l'identifiant du restaurant
     */
    private void updateRestaurantRating(Long restaurantId) {
        try {
            Object[] stats = reviewRepository.getAverageAndCount(restaurantId);
            double averageRating = stats[0] != null ? (Double) stats[0] : 0.0;
            Long totalReviews = stats[1] != null ? (Long) stats[1] : 0L;

            // Arrondir à 1 décimale
            averageRating = Math.round(averageRating * 10.0) / 10.0;

            restaurantServiceClient.updateRestaurantRating(restaurantId, averageRating, totalReviews);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la note du restaurant ID: {}: {}",
                    restaurantId, e.getMessage());
        }
    }

    /**
     * Vérifie que l'utilisateur a accès aux avis d'un restaurant.
     * Seuls les propriétaires, staff et admin ont accès.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur
     * @param role le rôle de l'utilisateur
     * @throws ForbiddenException si l'utilisateur n'a pas accès
     */
    private void checkRestaurantAccess(Long restaurantId, Long userId, String role) {
        // Staff et admin ont accès à tout
        if (isStaffOrAdmin(role)) {
            return;
        }

        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(restaurantId);

            if (restaurant.getOwnerId() != null && restaurant.getOwnerId().equals(userId)) {
                return;
            }

            throw new ForbiddenException("Vous n'avez pas accès aux avis de ce restaurant");
        } catch (ResourceNotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des permissions pour le restaurant ID: {}: {}",
                    restaurantId, e.getMessage());
            throw new BusinessException(
                    "Impossible de vérifier les permissions. Veuillez réessayer.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ExceptionConst.SERVICE_UNAVAILABLE
            );
        }
    }

    /**
     * Vérifie si un rôle correspond à admin.
     *
     * @param role le rôle à vérifier
     * @return true si le rôle est ADMIN
     */
    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * Vérifie si un rôle correspond à staff ou admin.
     *
     * @param role le rôle à vérifier
     * @return true si le rôle est STAFF ou ADMIN
     */
    private boolean isStaffOrAdmin(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role);
    }

    /**
     * Enrichit une réponse d'avis avec le nom du restaurant.
     * Si le restaurant service est indisponible, retourne la réponse simple.
     *
     * @param review l'avis
     * @return la réponse enrichie ou simple
     */
    private ReviewResponse enrichReviewResponse(Review review) {
        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(review.getRestaurantId());
            return ReviewResponse.fromEntityWithRestaurantName(review, restaurant.getName());
        } catch (Exception _) {
            return ReviewResponse.fromEntity(review);
        }
    }
}
