package com.restobook.bookingservice.repositories;

import com.restobook.bookingservice.entities.Booking;
import com.restobook.bookingservice.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Recherche une reservation par sa reference unique.
     *
     * @param bookingReference la reference de la reservation
     * @return un Optional contenant la reservation si trouvée
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Récupère toutes les reservations d'un utilisateur triées par date et heure décroissantes.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param pageable les informations de pagination
     * @return une page de reservations
     */
    Page<Booking> findByUserIdOrderByBookingDateDescBookingTimeDesc(Long userId, Pageable pageable);

    /**
     * Récupère toutes les reservations d'un utilisateur filtrées par statut.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param status le statut des reservations
     * @param pageable les informations de pagination
     * @return une page de reservations
     */
    Page<Booking> findByUserIdAndStatusOrderByBookingDateDescBookingTimeDesc(
            Long userId, BookingStatus status, Pageable pageable);

    /**
     * Récupère toutes les reservations d'un restaurant triées par date et heure décroissantes.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page de reservations
     */
    Page<Booking> findByRestaurantIdOrderByBookingDateDescBookingTimeDesc(Long restaurantId, Pageable pageable);

    /**
     * Récupère toutes les reservations d'un restaurant pour une date spécifique triées par heure.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param bookingDate la date des reservations
     * @return une liste de reservations triée par heure
     */
    List<Booking> findByRestaurantIdAndBookingDateOrderByBookingTimeAsc(Long restaurantId, LocalDate bookingDate);

    /**
     * Récupère toutes les reservations actives (non annulées) d'un restaurant pour une date.
     * Utilise In pour filtrer sur PENDING et CONFIRMED.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param bookingDate la date des reservations
     * @param statuses les statuts a inclure (PENDING, CONFIRMED)
     * @return une liste de reservations actives triée par heure
     */
    List<Booking> findByRestaurantIdAndBookingDateAndStatusInOrderByBookingTimeAsc(Long restaurantId, LocalDate bookingDate, List<BookingStatus> statuses);

    /**
     * Calcule le nombre total de places reservees pour un créneau horaire spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param bookingDate la date du créneau
     * @param slotTime l'heure de debut du créneau
     * @param slotEndTime l'heure de fin du créneau
     * @return le nombre total de places reservees
     */
    @Query("SELECT COALESCE(SUM(b.partySize), 0) FROM Booking b " +
            "WHERE b.restaurantId = :restaurantId " +
            "AND b.bookingDate = :bookingDate " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((b.bookingTime <= :slotTime AND b.endTime > :slotTime) " +
            "OR (b.bookingTime >= :slotTime AND b.bookingTime < :slotEndTime))")
    Integer countBookedSeatsForSlot(
            @Param("restaurantId") Long restaurantId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("slotTime") LocalTime slotTime,
            @Param("slotEndTime") LocalTime slotEndTime
    );

    /**
     * Récupère toutes les reservations en attente qui doivent être complétées automatiquement.
     *
     * @param today la date actuelle
     * @param currentTime l'heure actuelle
     * @return une liste de reservations en attente a completer
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
            "AND (b.bookingDate < :today OR (b.bookingDate = :today AND b.bookingTime < :currentTime))")
    List<Booking> findPendingBookingsToComplete(
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );

    /**
     * Récupère toutes les reservations complétées d'un utilisateur pour un restaurant spécifique.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @param status le statut COMPLETED
     * @return une liste de reservations complétées
     */
    List<Booking> findByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, BookingStatus status);

    /**
     * Vérifie si un utilisateur à au moins une reservation complétée pour un restaurant.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param restaurantId l'identifiant du restaurant
     * @param status le statut a verifier
     * @return true si au moins une reservation existe
     */
    boolean existsByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, BookingStatus status);

    /**
     * Compte le nombre de reservations actives d'un restaurant pour une date.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param date la date
     * @param excludedStatuses les statuts a exclure (CANCELLED, REJECTED)
     * @return le nombre de reservations actives
     */
    long countByRestaurantIdAndBookingDateAndStatusNotIn(Long restaurantId, LocalDate date, List<BookingStatus> excludedStatuses);

    /**
     * Récupère toutes les reservations à venir d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param today la date actuelle
     * @param currentTime l'heure actuelle
     * @return une liste de reservations à venir, triée par date et heure
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND (b.bookingDate > :today OR (b.bookingDate = :today AND b.bookingTime >= :currentTime)) " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY b.bookingDate ASC, b.bookingTime ASC")
    List<Booking> findUpcomingBookingsByUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );

    /**
     * Récupère toutes les reservations passées d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param today la date actuelle
     * @param currentTime l'heure actuelle
     * @param pageable les informations de pagination
     * @return une page de reservations passees triée par date et heure décroissantes
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND (b.bookingDate < :today OR (b.bookingDate = :today AND b.bookingTime < :currentTime)) " +
            "ORDER BY b.bookingDate DESC, b.bookingTime DESC")
    Page<Booking> findPastBookingsByUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime,
            Pageable pageable
    );
}
