package com.restobook.restaurantservice.entities;

import com.restobook.restaurantservice.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "opening_hours", indexes = {
        @Index(name = "idx_opening_hours_restaurant", columnList = "restaurant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "opening_time_morning")
    private LocalTime openingTimeMorning;

    @Column(name = "closing_time_morning")
    private LocalTime closingTimeMorning;

    @Column(name = "opening_time_evening")
    private LocalTime openingTimeEvening;

    @Column(name = "closing_time_evening")
    private LocalTime closingTimeEvening;

    @Column(nullable = false)
    @Builder.Default
    private Boolean closed = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public boolean isOpenAt(LocalTime time) {
        if (Boolean.TRUE.equals(closed)) return false;

        boolean openMorning = openingTimeMorning != null && closingTimeMorning != null
                && time.isAfter(openingTimeMorning) && !time.isAfter(closingTimeMorning);

        boolean openEvening = openingTimeEvening != null && closingTimeEvening != null
                && time.isAfter(openingTimeEvening) && !time.isAfter(closingTimeEvening);

        return openMorning || openEvening;
    }
}
