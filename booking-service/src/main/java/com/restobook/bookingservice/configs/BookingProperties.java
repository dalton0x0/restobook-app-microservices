package com.restobook.bookingservice.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    /**
     * Durée d'une réservation en minutes (défaut : 90)
     */
    private Integer durationMinutes = 90;

    /**
     * Intervalle entre les créneaux en minutes (défaut : 30)
     */
    private Integer slotIntervalMinutes = 30;

    /**
     * Nombre maximum de personnes par réservation (défaut : 8)
     */
    private Integer maxPartySize = 8;

    /**
     * Nombre minimum de personnes par réservation (défaut : 1)
     */
    private Integer minPartySize = 1;

    /**
     * Nombre maximum de jours à l'avance pour réserver (défaut : 7)
     */
    private Integer maxAdvanceDays = 7;

    /**
     * Nombre d'heures avant le créneau pour annuler gratuitement (défaut : 2)
     */
    private Integer cancellationHours = 2;

    /**
     * Jours maximum pour réserver
     */
    private Integer maxDaysAdvance = 7;
}
