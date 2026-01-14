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
        if (Boolean.TRUE.equals(closed)) {
            return false;
        }

        // Vérifier les horaires du matin
        boolean openMorning = isTimeInRange(time, openingTimeMorning, closingTimeMorning);

        // Vérifier les horaires du soir
        boolean openEvening = isTimeInRange(time, openingTimeEvening, closingTimeEvening);

        return openMorning || openEvening;
    }

    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }

        // Cas normal : start < end (ex: 09:00 - 14:00)
        if (!end.isBefore(start)) {
            // L'heure doit être >= start ET <= end
            return !time.isBefore(start) && !time.isAfter(end);
        }

        // Cas où la plage traverse minuit (ex: 22:00 - 06:00)
        // L'heure est valide si elle est >= start OU <= end
        return !time.isBefore(start) || !time.isAfter(end);
    }
}
