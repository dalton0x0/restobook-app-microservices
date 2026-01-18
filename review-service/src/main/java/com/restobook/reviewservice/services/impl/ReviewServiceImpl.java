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

/**
 * Implementation du service de gestion des avis.
 * Gere les operations CRUD et la logique metier associee aux avis de restaurants.
 */
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
        log.info("Creation d'un avis par l'utilisateur ID: {} pour le restaurant ID: {}",
                userId, request.getRestaurantId());

        // Valider que le restaurant existe
        if (!restaurantServiceClient.restaurantExists(request.getRestaurantId())) {
            throw new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId());
        }

        // Verifier que l'utilisateur n'a pas deja evalue ce restaurant
        if (hasReviewedRestaurant(userId, request.getRestaurantId())) {
            throw ReviewException.alreadyReviewed();
        }

        // Verifier que l'utilisateur a une reservation terminee
        boolean hasCompletedBooking = bookingServiceClient.hasCompletedBooking(userId, request.getRestaurantId());
        if (!hasCompletedBooking) {
            throw ReviewException.noCompletedBooking();
        }

        // Valider la note
        validateRating(request.getRating());

        // Construire et sauvegarder l'avis
        Review review = buildNewReview(request, userId, userName);
        Review savedReview = reviewRepository.save(review);

        log.info("Avis cree avec succes: ID {}", savedReview.getId());

        // Mettre a jour la note du restaurant
        updateRestaurantRating(request.getRestaurantId());

        return enrichReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        log.debug("Recuperation de l'avis ID: {}", id);

        Review review = findReviewByIdOrThrow(id);

        return enrichReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, UpdateReviewRequest request, Long userId, String role) {
        log.info("Modification de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = findReviewByIdOrThrow(id);

        // Verifier les permissions
        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez modifier que vos propres avis");
        }

        // Appliquer les modifications
        updateReviewFields(review, request);

        Review updatedReview = reviewRepository.save(review);

        log.info("Avis ID: {} mis a jour", id);

        // Mettre a jour la note du restaurant
        updateRestaurantRating(review.getRestaurantId());

        return enrichReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId, String role) {
        log.info("Suppression de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        Review review = findReviewByIdOrThrow(id);

        // Verifier les permissions
        if (!isAdmin(role) && !review.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous ne pouvez supprimer que vos propres avis");
        }

        Long restaurantId = review.getRestaurantId();
        reviewRepository.delete(review);

        log.info("Avis ID: {} supprime", id);

        // Mettre a jour la note du restaurant
        updateRestaurantRating(restaurantId);
    }

    // RECHERCHE

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        log.debug("Recuperation des avis du restaurant ID: {}", restaurantId);

        return reviewRepository.findByRestaurantIdAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable) {
        log.debug("Recuperation de tous les avis du restaurant ID: {}", restaurantId);

        checkRestaurantAccess(restaurantId, userId, role);

        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        log.debug("Recuperation des avis de l'utilisateur ID: {}", userId);

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByRestaurantAndRating(Long restaurantId, Integer rating, Pageable pageable) {
        log.debug("Recuperation des avis du restaurant ID: {} avec la note: {}", restaurantId, rating);

        return reviewRepository.findByRestaurantIdAndRatingAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, rating, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getVerifiedReviews(Long restaurantId, Pageable pageable) {
        log.debug("Recuperation des avis verifies du restaurant ID: {}", restaurantId);

        return reviewRepository.findByRestaurantIdAndIsVerifiedTrueAndIsVisibleTrueOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::enrichReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> searchReviews(Long restaurantId, String keyword, Pageable pageable) {
        log.debug("Recherche d'avis pour le restaurant ID: {} avec le mot-cle: {}", restaurantId, keyword);

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

        // Remplir avec les donnees reelles
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
        log.info("Ajout d'une reponse proprietaire a l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = findReviewByIdOrThrow(reviewId);
        checkRestaurantAccess(review.getRestaurantId(), userId, role);

        // Ajouter la reponse
        review.setOwnerResponse(request.getResponse());
        review.setOwnerResponseAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        log.info("Reponse proprietaire ajoutee a l'avis ID: {}", reviewId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse deleteOwnerResponse(Long reviewId, Long userId, String role) {
        log.info("Suppression de la reponse proprietaire de l'avis ID: {} par l'utilisateur ID: {}", reviewId, userId);

        Review review = findReviewByIdOrThrow(reviewId);
        checkRestaurantAccess(review.getRestaurantId(), userId, role);

        // Supprimer la reponse
        review.setOwnerResponse(null);
        review.setOwnerResponseAt(null);

        Review savedReview = reviewRepository.save(review);

        log.info("Reponse proprietaire supprimee de l'avis ID: {}", reviewId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUnansweredReviews(Long restaurantId, Long userId, String role) {
        log.debug("Recuperation des avis sans reponse pour le restaurant ID: {}", restaurantId);

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
        log.info("Modification de la visibilite de l'avis ID: {} par l'utilisateur ID: {}", id, userId);

        // Verifier les permissions admin
        if (!isAdmin(role)) {
            throw new ForbiddenException("Seuls les administrateurs peuvent modifier la visibilite des avis");
        }

        Review review = findReviewByIdOrThrow(id);

        // Inverser la visibilite
        review.setIsVisible(!review.getIsVisible());

        Review updatedReview = reviewRepository.save(review);

        log.info("Visibilite de l'avis ID: {} modifiee: {}", id, updatedReview.getIsVisible());

        // Mettre a jour la note du restaurant
        updateRestaurantRating(review.getRestaurantId());

        return ReviewResponse.fromEntity(updatedReview);
    }

    // MON AVIS

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForRestaurant(Long restaurantId, Long userId) {
        log.debug("Recuperation de l'avis de l'utilisateur ID: {} pour le restaurant ID: {}", userId, restaurantId);

        return reviewRepository.findByUserIdAndRestaurantId(userId, restaurantId)
                .map(this::enrichReviewResponse)
                .orElse(null);
    }

    @Override
    public boolean hasReviewedRestaurant(Long userId, Long restaurantId) {
        log.debug("Verification si l'utilisateur ID: {} a deja evalue le restaurant ID: {}", userId, restaurantId);

        return reviewRepository.existsByUserIdAndRestaurantId(userId, restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReviewRestaurant(Long userId, Long restaurantId) {
        log.debug("Verification si l'utilisateur ID: {} peut evaluer le restaurant ID: {}", userId, restaurantId);

        // Ne peut pas evaluer si deja fait
        if (hasReviewedRestaurant(userId, restaurantId)) {
            return false;
        }

        // Doit avoir une reservation terminee
        return bookingServiceClient.hasCompletedBooking(userId, restaurantId);
    }

    // METHODES PRIVEES

    /**
     * Recherche un avis par ID ou leve une exception si non trouve.
     * Methode privee pour eviter la duplication de code.
     *
     * @param id l'identifiant de l'avis
     * @return l'avis trouve
     * @throws ResourceNotFoundException si l'avis n'existe pas
     */
    private Review findReviewByIdOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Avis non trouve avec l'ID: {}", id);
                    return new ResourceNotFoundException("id", id);
                });
    }

    /**
     * Construit un nouvel avis a partir de la requete de creation.
     * Methode privee pour ameliorer la lisibilite et la testabilite.
     *
     * @param request la requete de creation contenant les informations de l'avis
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
     * Met a jour les champs modifiables d'un avis.
     * Methode privee pour factoriser la logique de mise a jour.
     *
     * @param review l'avis a modifier
     * @param request la requete de mise a jour contenant les nouveaux champs
     */
    private void updateReviewFields(Review review, UpdateReviewRequest request) {
        // Mise a jour de la note si fournie
        if (request.getRating() != null) {
            validateRating(request.getRating());
            review.setRating(request.getRating());
        }

        // Mise a jour du commentaire si fourni
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
    }

    /**
     * Valide qu'une note est dans la plage autorisee.
     * Methode privee pour centraliser la validation.
     *
     * @param rating la note a valider
     * @throws ReviewException si la note est invalide
     */
    private void validateRating(Integer rating) {
        if (rating < reviewProperties.getMinRating() || rating > reviewProperties.getMaxRating()) {
            throw ReviewException.invalidRating(reviewProperties.getMinRating(), reviewProperties.getMaxRating());
        }
    }

    /**
     * Met a jour la note moyenne d'un restaurant.
     * Recupere les statistiques et notifie le restaurant-service.
     * Methode privee pour centraliser cette logique.
     *
     * @param restaurantId l'identifiant du restaurant
     */
    private void updateRestaurantRating(Long restaurantId) {
        try {
            Object[] stats = reviewRepository.getAverageAndCount(restaurantId);
            double averageRating = stats[0] != null ? (Double) stats[0] : 0.0;
            Long totalReviews = stats[1] != null ? (Long) stats[1] : 0L;

            // Arrondir a 1 decimale
            averageRating = Math.round(averageRating * 10.0) / 10.0;

            restaurantServiceClient.updateRestaurantRating(restaurantId, averageRating, totalReviews);
        } catch (Exception e) {
            log.error("Erreur lors de la mise a jour de la note du restaurant ID: {}: {}",
                    restaurantId, e.getMessage());
        }
    }

    /**
     * Verifie que l'utilisateur a acces aux avis d'un restaurant.
     * Seuls les proprietaires, staff et admin ont acces.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur
     * @param role le role de l'utilisateur
     * @throws ForbiddenException si l'utilisateur n'a pas acces
     */
    private void checkRestaurantAccess(Long restaurantId, Long userId, String role) {
        // Staff et admin ont acces a tout
        if (isStaffOrAdmin(role)) {
            return;
        }

        try {
            RestaurantServiceClient.RestaurantInfo restaurant =
                    restaurantServiceClient.getRestaurantInfo(restaurantId);

            if (restaurant.getOwnerId() != null && restaurant.getOwnerId().equals(userId)) {
                return;
            }

            throw new ForbiddenException("Vous n'avez pas acces aux avis de ce restaurant");
        } catch (ResourceNotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la verification des permissions pour le restaurant ID: {}: {}",
                    restaurantId, e.getMessage());
            throw new BusinessException(
                    "Impossible de verifier les permissions. Veuillez reessayer.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ExceptionConst.SERVICE_UNAVAILABLE
            );
        }
    }

    /**
     * Verifie si un role correspond a admin.
     *
     * @param role le role a verifier
     * @return true si le role est ADMIN
     */
    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * Verifie si un role correspond a staff ou admin.
     *
     * @param role le role a verifier
     * @return true si le role est STAFF ou ADMIN
     */
    private boolean isStaffOrAdmin(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role);
    }

    /**
     * Enrichit une reponse d'avis avec le nom du restaurant.
     * Si le restaurant service est indisponible, retourne la reponse simple.
     *
     * @param review l'avis
     * @return la reponse enrichie ou simple
     */
    private ReviewResponse enrichReviewResponse(Review review) {
        try {
            RestaurantServiceClient.RestaurantInfo restaurant =
                    restaurantServiceClient.getRestaurantInfo(review.getRestaurantId());
            return ReviewResponse.fromEntityWithRestaurantName(review, restaurant.getName());
        } catch (Exception _) {
            return ReviewResponse.fromEntity(review);
        }
    }
}
