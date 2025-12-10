package com.restobook.bookingservice.services;

import com.restobook.bookingservice.dtos.request.CancelBookingRequest;
import com.restobook.bookingservice.dtos.request.CreateBookingRequest;
import com.restobook.bookingservice.dtos.request.UpdateBookingRequest;
import com.restobook.bookingservice.dtos.response.BookingResponse;
import com.restobook.bookingservice.dtos.response.TimeSlotResponse;
import com.restobook.bookingservice.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /**
     * Crée une nouvelle réservation
     */
    BookingResponse createBooking(CreateBookingRequest request, Long userId);

    /**
     * Récupère une réservation par son ID
     */
    BookingResponse getBookingById(Long id, Long userId, String role);

    /**
     * Récupère une réservation par sa référence
     */
    BookingResponse getBookingByReference(String reference, Long userId, String role);

    /**
     * Met à jour une réservation
     */
    BookingResponse updateBooking(Long id, UpdateBookingRequest request, Long userId, String role);

    /**
     * Annule une réservation
     */
    BookingResponse cancelBooking(Long id, CancelBookingRequest request, Long userId, String role);

    /**
     * Liste les réservations d'un utilisateur avec pagination
     */
    Page<BookingResponse> getBookingsByUser(Long userId, Pageable pageable);

    /**
     * Liste les réservations d'un utilisateur par statut avec pagination
     */
    Page<BookingResponse> getBookingsByUserAndStatus(Long userId, BookingStatus status, Pageable pageable);

    /**
     * Liste les réservations à venir d'un utilisateur
     */
    List<BookingResponse> getUpcomingBookingsByUser(Long userId);

    /**
     * Liste les réservations passées d'un utilisateur avec pagination
     */
    Page<BookingResponse> getPastBookingsByUser(Long userId, Pageable pageable);

    /**
     * Liste les réservations d'un restaurant avec pagination
     */
    Page<BookingResponse> getBookingsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable);

    /**
     * Liste les réservations d'un restaurant pour une date donnée
     */
    List<BookingResponse> getBookingsByRestaurantAndDate(Long restaurantId, LocalDate date, Long userId, String role);

    /**
     * Liste les réservations du jour pour un restaurant
     */
    List<BookingResponse> getTodayBookingsByRestaurant(Long restaurantId, Long userId, String role);

    /**
     * Récupère les créneaux horaires disponibles pour un restaurant
     */
    TimeSlotResponse getAvailableSlots(Long restaurantId, LocalDate date, Integer partySize);

    /**
     * Confirme une réservation en attente
     */
    BookingResponse confirmBooking(Long id, Long userId, String role);

    /**
     * Refuse une réservation en attente
     */
    BookingResponse rejectBooking(Long id, String reason, Long userId, String role);

    /**
     * Marque une réservation comme terminée
     */
    BookingResponse markAsCompleted(Long id, Long userId, String role);

    /**
     * Marque une réservation comme absence (no-show)
     */
    BookingResponse markAsNoShow(Long id, Long userId, String role);

    /**
     * Vérifie si un utilisateur a une réservation terminée dans un restaurant
     */
    boolean hasCompletedBooking(Long userId, Long restaurantId);
}
