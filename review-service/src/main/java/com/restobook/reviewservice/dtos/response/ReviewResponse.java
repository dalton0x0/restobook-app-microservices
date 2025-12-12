package com.restobook.reviewservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.restobook.reviewservice.entities.Review;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private String ownerResponse;
    private LocalDateTime ownerResponseAt;
    private String userName;
    private Boolean isVerified;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .restaurantId(review.getRestaurantId())
                .bookingId(review.getBookingId())
                .rating(review.getRating())
                .comment(review.getComment())
                .ownerResponse(review.getOwnerResponse())
                .ownerResponseAt(review.getOwnerResponseAt())
                .userName(review.getUserName())
                .isVerified(review.getIsVerified())
                .isVisible(review.getIsVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public static ReviewResponse fromEntityWithRestaurantName(Review review, String restaurantName) {
        ReviewResponse response = fromEntity(review);
        response.setRestaurantName(restaurantName);
        return response;
    }
}
