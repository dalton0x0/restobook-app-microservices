package com.restobook.bookingservice.exceptions;

import org.springframework.http.HttpStatus;

public class BookingException extends BusinessException {

    public BookingException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BOOKING_ERROR");
    }

    public BookingException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public static BookingException slotNotAvailable() {
        return new BookingException("Ce créneau n'est plus disponible", "SLOT_NOT_AVAILABLE");
    }

    public static BookingException restaurantClosed() {
        return new BookingException("Le restaurant est fermé à cette date/heure", "RESTAURANT_CLOSED");
    }

    public static BookingException invalidPartySize(int min, int max) {
        return new BookingException(
                String.format("Le nombre de personnes doit être entre %d et %d", min, max),
                "INVALID_PARTY_SIZE"
        );
    }

    public static BookingException invalidDate(int maxDays) {
        return new BookingException(
                String.format("La date doit être comprise entre aujourd'hui et J+%d", maxDays),
                "INVALID_DATE"
        );
    }

    public static BookingException cancellationTooLate(int hours) {
        return new BookingException(
                String.format("L'annulation doit être effectuée au moins %d heures avant le créneau", hours),
                "CANCELLATION_TOO_LATE"
        );
    }

    public static BookingException cannotCancelBooking() {
        return new BookingException("Cette réservation ne peut plus être annulée", "CANNOT_CANCEL");
    }

    public static BookingException capacityExceeded() {
        return new BookingException("La capacité du restaurant est insuffisante pour ce créneau", "CAPACITY_EXCEEDED");
    }
}
