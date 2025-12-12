package com.restobook.reviewservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_review_rating", columnList = "rating"),
        @Index(name = "idx_review_user_restaurant", columnList = "user_id, restaurant_id", unique = true)
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_restaurant", columnNames = {"user_id", "restaurant_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "owner_response", length = 1000)
    private String ownerResponse;

    @Column(name = "owner_response_at")
    private LocalDateTime ownerResponseAt;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isVerified = false;
        this.isVisible = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
