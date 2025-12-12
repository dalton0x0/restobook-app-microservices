package com.restobook.reviewservice.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewException extends BusinessException {

    public ReviewException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "REVIEW_ERROR");
    }

    public ReviewException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public static ReviewException alreadyReviewed() {
        return new ReviewException("Vous avez déjà laissé un avis pour ce restaurant", "ALREADY_REVIEWED");
    }

    public static ReviewException noCompletedBooking() {
        return new ReviewException(
                "Vous devez avoir effectué une réservation terminée pour laisser un avis",
                "NO_COMPLETED_BOOKING"
        );
    }

    public static ReviewException invalidRating(int min, int max) {
        return new ReviewException(
                String.format("La note doit être comprise entre %d et %d", min, max),
                "INVALID_RATING"
        );
    }

    public static ReviewException commentTooLong(int maxLength) {
        return new ReviewException(
                String.format("Le commentaire ne peut pas dépasser %d caractères", maxLength),
                "COMMENT_TOO_LONG"
        );
    }

    public static ReviewException cannotModify() {
        return new ReviewException("Cet avis ne peut plus être modifié", "CANNOT_MODIFY");
    }

    public static ReviewException cannotDelete() {
        return new ReviewException("Cet avis ne peut pas être supprimé", "CANNOT_DELETE");
    }
}
