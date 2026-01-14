package com.restobook.bookingservice.controllers;

import com.restobook.bookingservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Internal", description = "Endpoints internes pour la communication inter-services")
public class InternalController {

    private final BookingService bookingService;

    @GetMapping("/bookings/completed")
    @Operation(summary = "Vérifier si un utilisateur a une réservation terminée",
            description = "Utilisé par le Review Service pour vérifier l'éligibilité aux avis")
    public ResponseEntity<Boolean> hasCompletedBooking(
            @RequestParam Long userId,
            @RequestParam Long restaurantId) {

        log.debug("Requête HTTP GET /internal/bookings/completed - Utilisateur ID: {}, Restaurant ID: {} - Requête inter-service", userId, restaurantId);
        boolean hasCompleted = bookingService.hasCompletedBooking(userId, restaurantId);
        return ResponseEntity.ok(hasCompleted);
    }
}
