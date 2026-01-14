package com.restobook.reviewservice.controllers;

import com.restobook.reviewservice.clients.AuthServiceClient;
import com.restobook.reviewservice.constants.AuthenticationConst;
import com.restobook.reviewservice.dtos.request.CreateReviewRequest;
import com.restobook.reviewservice.dtos.request.OwnerResponseRequest;
import com.restobook.reviewservice.dtos.request.UpdateReviewRequest;
import com.restobook.reviewservice.dtos.response.*;
import com.restobook.reviewservice.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Reviews", description = "Gestion des avis")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthServiceClient authServiceClient;

    @PostMapping
    @Operation(summary = "Créer un avis", description = "Nécessite une réservation terminée")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP POST /api/v1/reviews - Utilisateur: {}", tokenInfo.getEmail());
        String userName = getUserName(tokenInfo);
        ReviewResponse review = reviewService.createReview(request, tokenInfo.getUserId(), userName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Avis créé avec succès", review));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un avis")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable Long id) {
        log.debug("Requête HTTP GET /api/v1/reviews/{}", id);
        ReviewResponse review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un avis")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PUT /api/v1/reviews/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        ReviewResponse review = reviewService.updateReview(id, request, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Avis modifié", review));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avis")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP DELETE /api/v1/reviews/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        reviewService.deleteReview(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Avis supprimé"));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Avis d'un restaurant", description = "Retourne les avis visibles")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByRestaurant(
            @PathVariable Long restaurantId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}", restaurantId);
        Page<ReviewResponse> reviews = reviewService.getReviewsByRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/restaurant/{restaurantId}/all")
    @Operation(summary = "Tous les avis d'un restaurant", description = "Pour owner/admin - inclut les avis masqués")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getAllReviewsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/all - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        Page<ReviewResponse> reviews = reviewService.getAllReviewsByRestaurant(
                restaurantId, tokenInfo.getUserId(), tokenInfo.getRole(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/restaurant/{restaurantId}/rating/{rating}")
    @Operation(summary = "Avis par note")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByRating(
            @PathVariable Long restaurantId,
            @PathVariable Integer rating,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/rating/{}", restaurantId, rating);
        Page<ReviewResponse> reviews = reviewService.getReviewsByRestaurantAndRating(restaurantId, rating, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/restaurant/{restaurantId}/verified")
    @Operation(summary = "Avis vérifiés uniquement")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getVerifiedReviews(
            @PathVariable Long restaurantId,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/verified", restaurantId);
        Page<ReviewResponse> reviews = reviewService.getVerifiedReviews(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/restaurant/{restaurantId}/search")
    @Operation(summary = "Rechercher dans les avis")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> searchReviews(
            @PathVariable Long restaurantId,
            @RequestParam String keyword,
            @PageableDefault Pageable pageable) {

        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/search?keyword={}", restaurantId, keyword);
        Page<ReviewResponse> reviews = reviewService.searchReviews(restaurantId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/restaurant/{restaurantId}/stats")
    @Operation(summary = "Statistiques des avis")
    public ResponseEntity<ApiResponse<ReviewStatsResponse>> getReviewStats(@PathVariable Long restaurantId) {
        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/stats", restaurantId);
        ReviewStatsResponse stats = reviewService.getReviewStats(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/my-reviews")
    @Operation(summary = "Mes avis")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /api/v1/reviews/my-reviews - Utilisateur: {}", tokenInfo.getEmail());
        Page<ReviewResponse> reviews = reviewService.getReviewsByUser(tokenInfo.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(reviews)));
    }

    @GetMapping("/my-review/restaurant/{restaurantId}")
    @Operation(summary = "Mon avis pour un restaurant")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReviewForRestaurant(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /api/v1/reviews/my-review/restaurant/{} - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        ReviewResponse review = reviewService.getMyReviewForRestaurant(restaurantId, tokenInfo.getUserId());
        if (review == null) {
            return ResponseEntity.ok(ApiResponse.success("Aucun avis trouvé", null));
        }
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    @GetMapping("/can-review/restaurant/{restaurantId}")
    @Operation(summary = "Vérifier si je peux laisser un avis")
    public ResponseEntity<ApiResponse<Boolean>> canReviewRestaurant(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /api/v1/reviews/can-review/restaurant/{} - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        boolean canReview = reviewService.canReviewRestaurant(tokenInfo.getUserId(), restaurantId);
        return ResponseEntity.ok(ApiResponse.success(canReview));
    }

    // Owner

    @PostMapping("/{reviewId}/owner-response")
    @Operation(summary = "Répondre à un avis", description = "Pour le propriétaire du restaurant")
    public ResponseEntity<ApiResponse<ReviewResponse>> addOwnerResponse(
            @PathVariable Long reviewId,
            @Valid @RequestBody OwnerResponseRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP POST /api/v1/reviews/{}/owner-response - Utilisateur: {}", reviewId, tokenInfo.getEmail());
        ReviewResponse review = reviewService.addOwnerResponse(reviewId, request, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réponse ajoutée", review));
    }

    @DeleteMapping("/{reviewId}/owner-response")
    @Operation(summary = "Supprimer la réponse à un avis")
    public ResponseEntity<ApiResponse<ReviewResponse>> deleteOwnerResponse(
            @PathVariable Long reviewId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP DELETE /api/v1/reviews/{}/owner-response - Utilisateur: {}", reviewId, tokenInfo.getEmail());
        ReviewResponse review = reviewService.deleteOwnerResponse(reviewId, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réponse supprimée", review));
    }

    @GetMapping("/restaurant/{restaurantId}/unanswered")
    @Operation(summary = "Avis sans réponse", description = "Pour le propriétaire du restaurant")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getUnansweredReviews(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /api/v1/reviews/restaurant/{}/unanswered - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        List<ReviewResponse> reviews = reviewService.getUnansweredReviews(restaurantId, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    // Admin

    @PatchMapping("/{id}/toggle-visibility")
    @Operation(summary = "Masquer/afficher un avis", description = "Admin uniquement")
    public ResponseEntity<ApiResponse<ReviewResponse>> toggleVisibility(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /api/v1/reviews/{}/toggle-visibility - Utilisateur: {}", id, tokenInfo.getEmail());
        ReviewResponse review = reviewService.toggleVisibility(id, tokenInfo.getUserId(), tokenInfo.getRole());
        String message = Boolean.TRUE.equals(review.getIsVisible()) ? "Avis rendu visible" : "Avis masqué";
        return ResponseEntity.ok(ApiResponse.success(message, review));
    }

    private TokenValidationResponse validateToken(String authHeader) {
        String token = authHeader.replace(AuthenticationConst.TOKEN_PREFIX, "");
        return authServiceClient.validateToken(token);
    }

    private String getUserName(TokenValidationResponse tokenInfo) {
        try {
            TokenValidationResponse.UserInfo userInfo = authServiceClient.getUserInfo(tokenInfo.getUserId());
            if (userInfo != null && userInfo.getFirstName() != null) {
                String lastName = userInfo.getLastName();
                if (lastName != null && !lastName.isEmpty()) {
                    return userInfo.getFirstName() + " " + lastName.charAt(0) + ".";
                }
                return userInfo.getFirstName();
            }
        } catch (Exception e) {
            log.debug("Impossible de récupérer le nom de l'utilisateur: {}", e.getMessage());
        }
        // Partie avant @ de l'email
        String email = tokenInfo.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Utilisateur";
    }
}
