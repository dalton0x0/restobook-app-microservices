package com.restobook.bookingservice.services.impl;

import com.restobook.bookingservice.clients.RestaurantServiceClient;
import com.restobook.bookingservice.configs.BookingProperties;
import com.restobook.bookingservice.constants.ExceptionConst;
import com.restobook.bookingservice.dtos.request.CancelBookingRequest;
import com.restobook.bookingservice.dtos.request.CreateBookingRequest;
import com.restobook.bookingservice.dtos.request.UpdateBookingRequest;
import com.restobook.bookingservice.dtos.response.BookingResponse;
import com.restobook.bookingservice.dtos.response.TimeSlotResponse;
import com.restobook.bookingservice.entities.Booking;
import com.restobook.bookingservice.enums.BookingStatus;
import com.restobook.bookingservice.exceptions.BookingException;
import com.restobook.bookingservice.exceptions.BusinessException;
import com.restobook.bookingservice.exceptions.ForbiddenException;
import com.restobook.bookingservice.exceptions.ResourceNotFoundException;
import com.restobook.bookingservice.repositories.BookingRepository;
import com.restobook.bookingservice.services.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantServiceClient restaurantServiceClient;
    private final BookingProperties bookingProperties;


    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        log.info("Création d'une réservation pour l'utilisateur {} au restaurant {}", userId, request.getRestaurantId());

        if (!restaurantServiceClient.restaurantExists(request.getRestaurantId())) {
            throw new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId());
        }

        validateBookingDate(request.getBookingDate());
        validatePartySize(request.getPartySize());

        if (!restaurantServiceClient.isRestaurantOpen(
                request.getRestaurantId(),
                request.getBookingDate().getDayOfWeek(),
                request.getBookingTime())) {
            throw BookingException.restaurantClosed();
        }

        LocalTime endTime = request.getBookingTime().plusMinutes(bookingProperties.getDurationMinutes());
        Integer capacity = restaurantServiceClient.getRestaurantCapacity(request.getRestaurantId());
        Integer bookedSeats = bookingRepository.countBookedSeatsForSlot(
                request.getRestaurantId(),
                request.getBookingDate(),
                request.getBookingTime(),
                endTime
        );

        if (bookedSeats + request.getPartySize() > capacity) {
            throw BookingException.capacityExceeded();
        }

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .userId(userId)
                .restaurantId(request.getRestaurantId())
                .bookingDate(request.getBookingDate())
                .bookingTime(request.getBookingTime())
                .endTime(endTime)
                .partySize(request.getPartySize())
                .status(BookingStatus.CONFIRMED)
                .specialRequests(request.getSpecialRequests())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .confirmedAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Réservation créée: {} (ID: {})", savedBooking.getBookingReference(), savedBooking.getId());

        return enrichBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, Long userId, String role) {
        log.debug("Récupération de la réservation ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));
        checkBookingAccess(booking, userId, role);
        return enrichBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference, Long userId, String role) {
        log.debug("Récupération de la réservation par référence: {}", reference);
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("reference", reference));
        checkBookingAccess(booking, userId, role);
        return enrichBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request, Long userId, String role) {
        log.info("Modification de la réservation {} par l'utilisateur {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkBookingAccess(booking, userId, role);

        if (!booking.canBeCancelled()) {
            throw new BookingException("Cette réservation ne peut plus être modifiée");
        }

        // Mise à jour des champs
        boolean dateTimeChanged = false;

        if (request.getBookingDate() != null) {
            validateBookingDate(request.getBookingDate());
            booking.setBookingDate(request.getBookingDate());
            dateTimeChanged = true;
        }

        if (request.getBookingTime() != null) {
            booking.setBookingTime(request.getBookingTime());
            booking.setEndTime(request.getBookingTime().plusMinutes(bookingProperties.getDurationMinutes()));
            dateTimeChanged = true;
        }

        if (request.getPartySize() != null) {
            validatePartySize(request.getPartySize());
            booking.setPartySize(request.getPartySize());
        }

        // Vérifier la disponibilité si date/heure changée
        if (dateTimeChanged) {
            if (!restaurantServiceClient.isRestaurantOpen(
                    booking.getRestaurantId(),
                    booking.getBookingDate().getDayOfWeek(),
                    booking.getBookingTime())) {
                throw BookingException.restaurantClosed();
            }

            Integer capacity = restaurantServiceClient.getRestaurantCapacity(booking.getRestaurantId());
            Integer bookedSeats = bookingRepository.countBookedSeatsForSlot(
                    booking.getRestaurantId(),
                    booking.getBookingDate(),
                    booking.getBookingTime(),
                    booking.getEndTime()
            );

            // Exclure la réservation actuelle du calcul
            if (bookedSeats + booking.getPartySize() > capacity) {
                throw BookingException.capacityExceeded();
            }
        }

        if (request.getSpecialRequests() != null) {
            booking.setSpecialRequests(request.getSpecialRequests());
        }
        if (request.getCustomerName() != null) {
            booking.setCustomerName(request.getCustomerName());
        }
        if (request.getCustomerEmail() != null) {
            booking.setCustomerEmail(request.getCustomerEmail());
        }
        if (request.getCustomerPhone() != null) {
            booking.setCustomerPhone(request.getCustomerPhone());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Réservation mise à jour: {}", id);

        return enrichBookingResponse(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, CancelBookingRequest request, Long userId, String role) {
        log.info("Annulation de la réservation {} par l'utilisateur {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkBookingAccess(booking, userId, role);

        if (!booking.canBeCancelled()) {
            throw BookingException.cannotCancelBooking();
        }

        // Vérifier le délai d'annulation sauf pour admin/staff
        if (!isStaffOrAdmin(role)) {
            LocalDateTime bookingDateTime = LocalDateTime.of(booking.getBookingDate(), booking.getBookingTime());
            LocalDateTime cancellationDeadline = bookingDateTime.minusHours(bookingProperties.getCancellationHours());

            if (LocalDateTime.now().isAfter(cancellationDeadline)) {
                throw BookingException.cancellationTooLate(bookingProperties.getCancellationHours());
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request != null ? request.getReason() : null);
        booking.setCancelledAt(LocalDateTime.now());
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Réservation annulée: {}", id);

        return enrichBookingResponse(cancelledBooking);
    }

    // Recherche

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByUser(Long userId, Pageable pageable) {
        log.debug("Récupération des réservations de l'utilisateur ID: {}", userId);
        return bookingRepository.findByUserIdOrderByBookingDateDescBookingTimeDesc(userId, pageable)
                .map(this::enrichBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByUserAndStatus(Long userId, BookingStatus status, Pageable pageable) {
        log.debug("Récupération des réservations de l'utilisateur ID: {} avec le statut: {}", userId, status);
        return bookingRepository.findByUserIdAndStatusOrderByBookingDateDescBookingTimeDesc(userId, status, pageable)
                .map(this::enrichBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookingsByUser(Long userId) {
        log.debug("Récupération des réservations à venir de l'utilisateur ID: {}", userId);
        return bookingRepository.findUpcomingBookingsByUser(userId, LocalDate.now(), LocalTime.now())
                .stream()
                .map(this::enrichBookingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getPastBookingsByUser(Long userId, Pageable pageable) {
        log.debug("Récupération des réservations passées de l'utilisateur ID: {}", userId);
        return bookingRepository.findPastBookingsByUser(userId, LocalDate.now(), LocalTime.now(), pageable)
                .map(this::enrichBookingResponse);
    }

    // Restaurant

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByRestaurant(Long restaurantId, Long userId, String role, Pageable pageable) {
        log.debug("Récupération des réservations du restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return bookingRepository.findByRestaurantIdOrderByBookingDateDescBookingTimeDesc(restaurantId, pageable)
                .map(BookingResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByRestaurantAndDate(Long restaurantId, LocalDate date, Long userId, String role) {
        log.debug("Récupération des réservations du restaurant ID: {} pour la date: {}", restaurantId, date);
        checkRestaurantAccess(restaurantId, userId, role);
        return bookingRepository.findByRestaurantIdAndBookingDateOrderByBookingTimeAsc(restaurantId, date)
                .stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getTodayBookingsByRestaurant(Long restaurantId, Long userId, String role) {
        log.debug("Récupération des réservations du jour pour le restaurant ID: {}", restaurantId);
        checkRestaurantAccess(restaurantId, userId, role);
        return bookingRepository.findTodayBookingsByRestaurant(restaurantId, LocalDate.now())
                .stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    // Créneaux

    @Override
    @Transactional(readOnly = true)
    public TimeSlotResponse getAvailableSlots(Long restaurantId, LocalDate date, Integer partySize) {
        log.debug("Recherche des créneaux disponibles pour le restaurant {} le {}", restaurantId, date);

        validateBookingDate(date);
        validatePartySize(partySize);

        if (!restaurantServiceClient.restaurantExists(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }

        Integer capacity = restaurantServiceClient.getRestaurantCapacity(restaurantId);

        // Générer les créneaux (11h30-14h30 et 18h30-22h30)
        List<TimeSlotResponse.AvailableSlot> slots = new ArrayList<>();

        // Midi
        generateSlotsForPeriod(slots, restaurantId, date, LocalTime.of(11, 30), LocalTime.of(14, 30), capacity, partySize);

        // Soir
        generateSlotsForPeriod(slots, restaurantId, date, LocalTime.of(18, 30), LocalTime.of(22, 30), capacity, partySize);

        return TimeSlotResponse.builder()
                .date(date)
                .slots(slots)
                .build();
    }

    private void generateSlotsForPeriod(List<TimeSlotResponse.AvailableSlot> slots, Long restaurantId,
                                        LocalDate date, LocalTime start, LocalTime end,
                                        Integer capacity, Integer partySize) {

        LocalTime current = start;
        int slotInterval = bookingProperties.getSlotIntervalMinutes();
        int duration = bookingProperties.getDurationMinutes();

        while (current.plusMinutes(duration).isBefore(end) || current.plusMinutes(duration).equals(end)) {
            boolean isOpen = restaurantServiceClient.isRestaurantOpen(restaurantId, date.getDayOfWeek(), current);

            Integer bookedSeats = 0;
            if (isOpen) {
                bookedSeats = bookingRepository.countBookedSeatsForSlot(
                        restaurantId, date, current, current.plusMinutes(duration)
                );
            }

            int availableCapacity = isOpen ? capacity - bookedSeats : 0;
            boolean isAvailable = isOpen && availableCapacity >= partySize;

            // Ne pas proposer les créneaux passés pour aujourd'hui
            if (date.equals(LocalDate.now()) && current.isBefore(LocalTime.now())) {
                isAvailable = false;
            }

            slots.add(TimeSlotResponse.AvailableSlot.builder()
                    .time(current)
                    .availableCapacity(Math.max(0, availableCapacity))
                    .available(isAvailable)
                    .build());

            current = current.plusMinutes(slotInterval);
        }
    }

    // Actions Restaurant

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id, Long userId, String role) {
        log.info("Confirmation de la réservation ID: {} par l'utilisateur ID: {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkRestaurantAccess(booking.getRestaurantId(), userId, role);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Seules les réservations en attente peuvent être confirmées");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Réservation confirmée: {}", savedBooking.getBookingReference());

        return BookingResponse.fromEntity(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id, String reason, Long userId, String role) {
        log.info("Refus de la réservation ID: {} par l'utilisateur ID: {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkRestaurantAccess(booking.getRestaurantId(), userId, role);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Seules les réservations en attente peuvent être refusées");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());
        Booking rejectedBooking = bookingRepository.save(booking);
        log.info("Réservation refusée: {}", rejectedBooking.getBookingReference());

        return BookingResponse.fromEntity(rejectedBooking);
    }

    @Override
    @Transactional
    public BookingResponse markAsCompleted(Long id, Long userId, String role) {
        log.info("Marquage de la réservation ID: {} comme terminée par l'utilisateur ID: {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkRestaurantAccess(booking.getRestaurantId(), userId, role);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Seules les réservations confirmées peuvent être marquées comme terminées");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        Booking completedBooking = bookingRepository.save(booking);
        log.info("Réservation marquée comme terminée: {}", completedBooking.getBookingReference());

        return BookingResponse.fromEntity(completedBooking);
    }

    @Override
    @Transactional
    public BookingResponse markAsNoShow(Long id, Long userId, String role) {
        log.info("Marquage de la réservation ID: {} comme absente par l'utilisateur ID: {}", id, userId);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", id));

        checkRestaurantAccess(booking.getRestaurantId(), userId, role);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Seules les réservations confirmées peuvent être marquées comme absence");
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        Booking noShowBooking = bookingRepository.save(booking);
        log.info("Réservation marquée comme absente: {}", noShowBooking.getBookingReference());

        return BookingResponse.fromEntity(noShowBooking);
    }

    // Interne

    @Override
    @Transactional(readOnly = true)
    public boolean hasCompletedBooking(Long userId, Long restaurantId) {
        log.debug("Vérification de réservation terminée - Utilisateur ID: {}, Restaurant ID: {}", userId, restaurantId);
        return bookingRepository.existsByUserIdAndRestaurantIdAndStatus(userId, restaurantId, BookingStatus.COMPLETED);
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateBookingDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDaysAdvance());

        if (date.isBefore(today) || date.isAfter(maxDate)) {
            throw BookingException.invalidDate(bookingProperties.getMaxDaysAdvance());
        }
    }

    private void validatePartySize(Integer partySize) {
        if (partySize < bookingProperties.getMinPartySize() || partySize > bookingProperties.getMaxPartySize()) {
            throw BookingException.invalidPartySize(bookingProperties.getMinPartySize(), bookingProperties.getMaxPartySize());
        }
    }

    private void checkBookingAccess(Booking booking, Long userId, String role) {
        if (isStaffOrAdmin(role)) {
            return;
        }

        // Le client qui a fait la réservation peut voir
        if (booking.getUserId().equals(userId)) {
            return;
        }

        // Idem pour le proprio
        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(booking.getRestaurantId());
            if (restaurant.getOwnerId() != null && restaurant.getOwnerId().equals(userId)) {
                return;
            }
        } catch (ResourceNotFoundException _) {
            log.warn("Restaurant {} non trouvé lors de la vérification d'accès", booking.getRestaurantId());
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du propriétaire du restaurant {}: {}",
                    booking.getRestaurantId(), e.getMessage());
        }

        throw new ForbiddenException("Vous n'avez pas accès à cette réservation");
    }

    private void checkRestaurantAccess(Long restaurantId, Long userId, String role) {
        if (isStaffOrAdmin(role)) {
            return;
        }

        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(restaurantId);
            if (restaurant.getOwnerId() != null && restaurant.getOwnerId().equals(userId)) {
                return;
            }
            throw new ForbiddenException("Vous n'avez pas accès aux réservations de ce restaurant");
        } catch (ResourceNotFoundException _) {
            log.warn("Restaurant {} non trouvé lors de la vérification d'accès", restaurantId);
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        } catch (ForbiddenException e) {
            // Re-lancer l'exception ForbiddenException
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la communication avec le Restaurant Service: {}", e.getMessage());
            throw new BusinessException(
                    "Impossible de vérifier les permissions. Veuillez réessayer.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ExceptionConst.SERVICE_UNAVAILABLE
            );
        }
    }

    private boolean isStaffOrAdmin(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role);
    }

    private BookingResponse enrichBookingResponse(Booking booking) {
        try {
            RestaurantServiceClient.RestaurantInfo restaurant = restaurantServiceClient.getRestaurantInfo(booking.getRestaurantId());
            return BookingResponse.fromEntityWithRestaurantName(booking, restaurant.getName());
        } catch (Exception _) {
            return BookingResponse.fromEntity(booking);
        }
    }
}
