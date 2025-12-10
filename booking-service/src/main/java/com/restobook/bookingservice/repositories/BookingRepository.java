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

    // Recherche par référence
    Optional<Booking> findByBookingReference(String bookingReference);

    // Réservations d'un utilisateur
    Page<Booking> findByUserIdOrderByBookingDateDescBookingTimeDesc(Long userId, Pageable pageable);

    // Réservations d'un utilisateur par statut
    Page<Booking> findByUserIdAndStatusOrderByBookingDateDescBookingTimeDesc(Long userId, BookingStatus status, Pageable pageable);

    // Réservations d'un restaurant
    Page<Booking> findByRestaurantIdOrderByBookingDateDescBookingTimeDesc(Long restaurantId, Pageable pageable);

    // Réservations d'un restaurant par date
    List<Booking> findByRestaurantIdAndBookingDateOrderByBookingTimeAsc(Long restaurantId, LocalDate bookingDate);

    // Réservations actives d'un restaurant pour une date (non annulées)
    @Query("SELECT b FROM Booking b WHERE b.restaurantId = :restaurantId " +
            "AND b.bookingDate = :bookingDate " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY b.bookingTime ASC")
    List<Booking> findActiveBookingsByRestaurantAndDate(
            @Param("restaurantId") Long restaurantId,
            @Param("bookingDate") LocalDate bookingDate
    );

    // Calculer le nombre de personnes réservées pour un créneau
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

    // Réservations à confirmer automatiquement (passées depuis X heures)
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
            "AND (b.bookingDate < :today OR (b.bookingDate = :today AND b.bookingTime < :currentTime))")
    List<Booking> findPendingBookingsToComplete(
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND b.restaurantId = :restaurantId " +
            "AND b.status = 'COMPLETED'")
    List<Booking> findCompletedBookingsByUserAndRestaurant(
            @Param("userId") Long userId,
            @Param("restaurantId") Long restaurantId
    );

    // Vérifier si une réservation est complétée pour un utilisateur et restaurant
    boolean existsByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, BookingStatus status);

    // Statistiques par restaurant
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.restaurantId = :restaurantId " +
            "AND b.bookingDate = :date AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    Long countBookingsByRestaurantAndDate(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date
    );

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND (b.bookingDate > :today OR (b.bookingDate = :today AND b.bookingTime >= :currentTime)) " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY b.bookingDate ASC, b.bookingTime ASC")
    List<Booking> findUpcomingBookingsByUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime
    );

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND (b.bookingDate < :today OR (b.bookingDate = :today AND b.bookingTime < :currentTime)) " +
            "ORDER BY b.bookingDate DESC, b.bookingTime DESC")
    Page<Booking> findPastBookingsByUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.restaurantId = :restaurantId " +
            "AND b.bookingDate = :today " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY b.bookingTime ASC")
    List<Booking> findTodayBookingsByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("today") LocalDate today
    );
}
