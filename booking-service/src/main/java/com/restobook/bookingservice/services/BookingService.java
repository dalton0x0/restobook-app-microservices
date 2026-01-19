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
     * Récupère une réservation par son identifiant.
     *
     * @param id l'identifiant de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation
     */
    BookingResponse getBookingById(Long id, Long userId, String role);

    /**
     * Récupère une réservation par sa référence unique.
     *
     * @param reference la référence de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation
     */
    BookingResponse getBookingByReference(String reference, Long userId, String role);

    /**
     * Liste toutes les réservations d'un utilisateur avec pagination.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page de réservations de l'utilisateur
     */
    Page<BookingResponse> getBookingsByUser(Long userId, Pageable pageable);

    /**
     * Liste les réservations d'un utilisateur filtrées par statut avec pagination.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param status le statut de réservation à filtrer
     * @param pageable les informations de pagination
     * @return une page de réservations correspondant au statut
     */
    Page<BookingResponse> getBookingsByUserAndStatus(Long userId, BookingStatus status, Pageable pageable);

    /**
     * Liste les réservations à venir d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     * @return une liste de réservations futures
     */
    List<BookingResponse> getUpcomingBookingsByUser(Long userId);

    /**
     * Liste les réservations passées d'un utilisateur avec pagination.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page de réservations passées
     */
    Page<BookingResponse> getPastBookingsByUser(Long userId, Pageable pageable);

    /**
     * Liste toutes les réservations d'un restaurant avec pagination.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page de réservations du restaurant
     */
    Page<BookingResponse> getBookingsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable);

    /**
     * Liste les réservations d'un restaurant pour une date spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param date la date des réservations
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @return une liste de réservations pour la date spécifiée
     */
    List<BookingResponse> getBookingsByRestaurantAndDate(Long restaurantId, LocalDate date, Long userId, String role);

    /**
     * Liste les réservations du jour en cours pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param userId l'identifiant de l'utilisateur effectuant la requête
     * @param role le rôle de l'utilisateur
     * @return une liste de réservations du jour
     */
    List<BookingResponse> getTodayBookingsByRestaurant(Long restaurantId, Long userId, String role);

    /**
     * Récupère les créneaux horaires disponibles pour un restaurant à une date donnée.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param date la date pour laquelle récupérer les créneaux
     * @param partySize le nombre de personnes
     * @return les créneaux horaires disponibles
     */
    TimeSlotResponse getAvailableSlots(Long restaurantId, LocalDate date, Integer partySize);

    /**
     * Crée une nouvelle réservation pour un utilisateur.
     *
     * @param request les informations de la réservation à créer
     * @param userId l'identifiant de l'utilisateur effectuant la réservation
     * @return les informations de la réservation créée
     */
    BookingResponse createBooking(CreateBookingRequest request, Long userId);

    /**
     * Met à jour les informations d'une réservation existante.
     *
     * @param id l'identifiant de la réservation
     * @param request les nouvelles informations de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation mise à jour
     */
    BookingResponse updateBooking(Long id, UpdateBookingRequest request, Long userId, String role);

    /**
     * Annule une réservation existante.
     *
     * @param id l'identifiant de la réservation
     * @param request les informations d'annulation incluant la raison
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation annulée
     */
    BookingResponse cancelBooking(Long id, CancelBookingRequest request, Long userId, String role);

    /**
     * Confirme une réservation en attente.
     *
     * @param id l'identifiant de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation confirmée
     */
    BookingResponse confirmBooking(Long id, Long userId, String role);

    /**
     * Refuse une réservation en attente.
     *
     * @param id l'identifiant de la réservation
     * @param reason la raison du refus
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation refusée
     */
    BookingResponse rejectBooking(Long id, String reason, Long userId, String role);

    /**
     * Marque une réservation comme terminée après le service.
     *
     * @param id l'identifiant de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation marquée comme terminée
     */
    BookingResponse markAsCompleted(Long id, Long userId, String role);

    /**
     * Marque une réservation comme absence (no-show) lorsque le client ne se présente pas.
     *
     * @param id l'identifiant de la réservation
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations de la réservation marquée comme absence
     */
    BookingResponse markAsNoShow(Long id, Long userId, String role);

    /**
     * Vérifie si un utilisateur possède au moins une réservation terminée dans un restaurant.
     * Utilisé pour déterminer si l'utilisateur peut laisser un avis.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @return true si l'utilisateur a une réservation terminée, false sinon
     */
    boolean hasCompletedBooking(Long userId, Long restaurantId);
}
