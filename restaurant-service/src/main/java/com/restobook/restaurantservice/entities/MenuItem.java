package com.restobook.restaurantservice.entities;

import com.restobook.restaurantservice.enums.MenuCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_item_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_menu_item_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 200)
    private String allergens;

    @Column(name = "nutritionalInfo", length = 500)
    private String nutritionalInfo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(name = "is_vegetarian")
    @Builder.Default
    private Boolean vegetarian = true;

    @Column(name="is_vegan")
    @Builder.Default
    private Boolean vegan = true;

    @Column(name="is_gluten_free")
    @Builder.Default
    private Boolean glutenFree = true;

    @Column(name="display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
