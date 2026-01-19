package com.restobook.bookingservice.controllers;

import com.restobook.bookingservice.clients.AuthServiceClient;
import com.restobook.bookingservice.constants.AuthenticationConst;
import com.restobook.bookingservice.dtos.request.CancelBookingRequest;
import com.restobook.bookingservice.dtos.request.CreateBookingRequest;
import com.restobook.bookingservice.dtos.request.UpdateBookingRequest;
import com.restobook.bookingservice.dtos.response.*;
import com.restobook.bookingservice.enums.BookingStatus;
import com.restobook.bookingservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Gestion des réservations")
public class BookingController {

    private final BookingService bookingService;
    private final AuthServiceClient authServiceClient;

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.getBookingById(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Rechercher par référence")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @PathVariable String reference,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/reference/{} - Utilisateur: {}", reference, tokenInfo.getEmail());
        BookingResponse booking = bookingService.getBookingByReference(reference, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Mes réservations", description = "Liste toutes les réservations de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault(sort = "bookingDate", direction = Sort.Direction.DESC) Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/my-bookings - Utilisateur: {}", tokenInfo.getEmail());
        Page<BookingResponse> bookings = bookingService.getBookingsByUser(tokenInfo.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/my-bookings/status/{status}")
    @Operation(summary = "Mes réservations par statut")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookingsByStatus(
            @PathVariable BookingStatus status,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/my-bookings/status/{} - Utilisateur: {}", status, tokenInfo.getEmail());
        Page<BookingResponse> bookings = bookingService.getBookingsByUserAndStatus(tokenInfo.getUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/my-bookings/upcoming")
    @Operation(summary = "Mes réservations à venir")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyUpcomingBookings(
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/my-bookings/upcoming - Utilisateur: {}", tokenInfo.getEmail());
        List<BookingResponse> bookings = bookingService.getUpcomingBookingsByUser(tokenInfo.getUserId());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/my-bookings/past")
    @Operation(summary = "Mes réservations passées")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyPastBookings(
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/my-bookings/past - Utilisateur: {}", tokenInfo.getEmail());
        Page<BookingResponse> bookings = bookingService.getPastBookingsByUser(tokenInfo.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/available-slots")
    @Operation(summary = "Créneaux disponibles", description = "Récupère les créneaux disponibles pour un restaurant")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> getAvailableSlots(
            @RequestParam Long restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") Integer partySize) {

        log.debug("Requête HTTP GET /bookings/available-slots - Restaurant ID: {}, Date: {}, Personnes: {}",
                restaurantId, date, partySize);
        TimeSlotResponse slots = bookingService.getAvailableSlots(restaurantId, date, partySize);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @PostMapping
    @Operation(summary = "Créer une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP POST /bookings - Utilisateur: {}", tokenInfo.getEmail());
        BookingResponse booking = bookingService.createBooking(request, tokenInfo.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Réservation créée avec succès", booking));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PUT /bookings/{} - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.updateBooking(id, request, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation modifiée", booking));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP POST /bookings/{}/cancel - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.cancelBooking(id, request, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation annulée", booking));
    }

    // Restaurant (OWNER/STAFF/ADMIN)

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Réservations d'un restaurant")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getRestaurantBookings(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader,
            @PageableDefault(sort = "bookingDate", direction = Sort.Direction.DESC) Pageable pageable) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/restaurant/{} - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        Page<BookingResponse> bookings = bookingService.getBookingsByRestaurant(
                restaurantId, tokenInfo.getUserId(), tokenInfo.getRole(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/restaurant/{restaurantId}/date/{date}")
    @Operation(summary = "Réservations d'un restaurant pour une date")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getRestaurantBookingsByDate(
            @PathVariable Long restaurantId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/restaurant/{}/date/{} - Utilisateur: {}",
                restaurantId, date, tokenInfo.getEmail());
        List<BookingResponse> bookings = bookingService.getBookingsByRestaurantAndDate(
                restaurantId, date, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/restaurant/{restaurantId}/today")
    @Operation(summary = "Réservations du jour pour un restaurant")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayRestaurantBookings(
            @PathVariable Long restaurantId,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP GET /bookings/restaurant/{}/today - Utilisateur: {}", restaurantId, tokenInfo.getEmail());
        List<BookingResponse> bookings = bookingService.getTodayBookingsByRestaurant(
                restaurantId, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmer une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /bookings/{}/confirm - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.confirmBooking(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation confirmée", booking));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Refuser une réservation")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /bookings/{}/reject - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.rejectBooking(id, reason, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation refusée", booking));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marquer comme terminée")
    public ResponseEntity<ApiResponse<BookingResponse>> markAsCompleted(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /bookings/{}/complete - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.markAsCompleted(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation marquée comme terminée", booking));
    }

    @PatchMapping("/{id}/no-show")
    @Operation(summary = "Marquer comme absente")
    public ResponseEntity<ApiResponse<BookingResponse>> markAsNoShow(
            @PathVariable Long id,
            @RequestHeader(AuthenticationConst.AUTH_HEADER) String authHeader) {

        TokenValidationResponse tokenInfo = validateToken(authHeader);
        log.debug("Requête HTTP PATCH /bookings/{}/no-show - Utilisateur: {}", id, tokenInfo.getEmail());
        BookingResponse booking = bookingService.markAsNoShow(id, tokenInfo.getUserId(), tokenInfo.getRole());
        return ResponseEntity.ok(ApiResponse.success("Réservation marquée comme absente", booking));
    }

    private TokenValidationResponse validateToken(String authHeader) {
        String token = authHeader.replace(AuthenticationConst.TOKEN_PREFIX, "");
        return authServiceClient.validateToken(token);
    }
}
