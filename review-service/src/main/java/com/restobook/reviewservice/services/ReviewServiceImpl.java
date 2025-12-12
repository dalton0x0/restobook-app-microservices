package com.restobook.reviewservice.services;

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

        Review review = Review.builder()
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .comment(request.getComment())
                .userName(userName)
                .isVerified(true)
                .isVisible(true)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Avis créé avec succès: ID {}", savedReview.getId());
        updateRestaurantRating(request.getRestaurantId());

        return enrichReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        log.debug("Récupération de l'avis ID: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));
        return enrichReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, UpdateReviewRequest request, Long userId, String role) {
        log.info("Modification de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez modifier que vos propres avis");
        }

        if (request.getRating() != null) {
            validateRating(request.getRating());
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        log.info("Avis ID: {} mis à jour", id);
        updateRestaurantRating(review.getRestaurantId());

        return enrichReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId, String role) {
        log.info("Suppression de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez supprimer que vos propres avis");
        }

        Long restaurantId = review.getRestaurantId();
        reviewRepository.delete(review);
        log.info("Avis ID: {} supprimé", id);
        updateRestaurantRating(restaurantId);
    }

    // Recherche

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        log.debug("Récupération des avis du restaurant ID: {}", restaurantId);
        return reviewRepository.findByRestaurantIdAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable) {
        log.debug("Récupération de tous les avis du restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        log.debug("Récupération des avis de l'utilisateur ID: {}", userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurantAndRating(Long restaurantId, Integer rating, Pageable pageable) {
        log.debug("Récupération des avis du restaurant ID: {} avec la note: {}", restaurantId, rating);
        return reviewRepository.findByRestaurantIdAndRatingAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, rating, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getVerifiedReviews(Long restaurantId, Pageable pageable) {
        log.debug("Récupération des avis vérifiés du restaurant ID: {}", restaurantId);
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
        Long totalReviews = reviewRepository.countByRestaurantIdAndVisible(restaurantId);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(restaurantId);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
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

    // Owner

    @Override
    @Transactional
    public ReviewResponse addOwnerResponse(Long reviewId, OwnerResponseRequest request, Long userId, String role) {
        log.info("Ajout d'une réponse propriétaire à l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("id", reviewId));

        checkRestaurantAccess(review.getRestaurantId(), userId, role);
        review.setOwnerResponse(request.getResponse());
        review.setOwnerResponseAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        log.info("Réponse propriétaire ajoutée à l'avis ID: {}", reviewId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse deleteOwnerResponse(Long reviewId, Long userId, String role) {
        log.info("Suppression de la réponse propriétaire de l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("id", reviewId));

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
        log.debug("Récupération des avis sans réponse pour le restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return reviewRepository.findUnansweredReviews(restaurantId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    // Admin

    @Override
    @Transactional
    public ReviewResponse toggleVisibility(Long id, Long userId, String role) {
        log.info("Modification de la visibilité de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        if (!isAdmin(role)) {
            throw new ForbiddenException("Seuls les administrateurs peuvent modifier la visibilité des avis");
        }

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        review.setIsVisible(!review.getIsVisible());
        Review updatedReview = reviewRepository.save(review);
        log.info("Visibilité de l'avis ID: {} modifiée: {}", id, updatedReview.getIsVisible());
        updateRestaurantRating(review.getRestaurantId());

        return ReviewResponse.fromEntity(updatedReview);
    }

    // Mon avis

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForRestaurant(Long restaurantId, Long userId) {
        log.debug("Récupération de l'avis de l'utilisateur ID: {} pour le restaurant ID: {}", userId, restaurantId);
        return reviewRepository.findByUserIdAndRestaurantId(userId, restaurantId)
                .map(this::enrichReviewResponse)
                .orElse(null);
    }

    @Override
    public boolean hasReviewedRestaurant(Long userId, Long restaurantId) {
        log.debug("Vérification si l'utilisateur ID: {} a déjà évalué le restaurant ID: {}", userId, restaurantId);
        return reviewRepository.existsByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReviewRestaurant(Long userId, Long restaurantId) {
        log.debug("Vérification si l'utilisateur ID: {} peut évaluer le restaurant ID: {}", userId, restaurantId);

        // Ne peut pas reviewer si déjà fait
        if (hasReviewedRestaurant(userId, restaurantId)) {
            return false;
        }
        // Doit avoir une réservation terminée
        return bookingServiceClient.hasCompletedBooking(userId, restaurantId);
    }

    private void validateRating(Integer rating) {
        if (rating < reviewProperties.getMinRating() || rating > reviewProperties.getMaxRating()) {
            throw ReviewException.invalidRating(reviewProperties.getMinRating(), reviewProperties.getMaxRating());
        }
    }

    private void updateRestaurantRating(Long restaurantId) {
        try {
            Object[] stats = reviewRepository.getAverageAndCount(restaurantId);
            double averageRating = stats[0] != null ? (Double) stats[0] : 0.0;
            Long totalReviews = stats[1] != null ? (Long) stats[1] : 0L;

            // Arrondir à 1 décimale
            averageRating = Math.round(averageRating * 10.0) / 10.0;

            restaurantServiceClient.updateRestaurantRating(restaurantId, averageRating, totalReviews);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la note du restaurant ID: {}: {}", restaurantId, e.getMessage());
        }
    }

    private void checkRestaurantAccess(Long restaurantId, Long userId, String role) {
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
            log.error("Erreur lors de la vérification des permissions pour le restaurant ID: {}: {}", restaurantId, e.getMessage());
            throw new BusinessException(
                    "Impossible de vérifier les permissions. Veuillez réessayer.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ExceptionConst.SERVICE_UNAVAILABLE
            );
        }
    }

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }

    private boolean isStaffOrAdmin(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role);
    }

    private ReviewResponse enrichReviewResponse(Review review) {
        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(review.getRestaurantId());
            return ReviewResponse.fromEntityWithRestaurantName(review, restaurant.getName());
        } catch (Exception _) {
            return ReviewResponse.fromEntity(review);
        }
    }
}
