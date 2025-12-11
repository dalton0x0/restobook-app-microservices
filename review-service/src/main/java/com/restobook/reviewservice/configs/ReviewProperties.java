package com.restobook.reviewservice.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "review")
public class ReviewProperties {

    /**
     * Note minimale
     */
    private Integer minRating = 1;

    /**
     * Note maximale
     */
    private Integer maxRating = 5;

    /**
     * Longueur minimale du commentaire
     */
    private Integer minCommentLength = 0;

    /**
     * Longueur maximale du commentaire
     */
    private Integer maxCommentLength = 1000;
}
