package com.restobook.bookingservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.restobook.bookingservice.entities.Booking;
import com.restobook.bookingservice.enums.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long id;
    private String bookingReference;
    private Long userId;
    private Long restaurantId;
    private String restaurantName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private LocalTime endTime;
    private Integer partySize;
    private BookingStatus status;
    private String specialRequests;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .restaurantId(booking.getRestaurantId())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .endTime(booking.getEndTime())
                .partySize(booking.getPartySize())
                .status(booking.getStatus())
                .specialRequests(booking.getSpecialRequests())
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .cancellationReason(booking.getCancellationReason())
                .cancelledAt(booking.getCancelledAt())
                .confirmedAt(booking.getConfirmedAt())
                .completedAt(booking.getCompletedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static BookingResponse fromEntityWithRestaurantName(Booking booking, String restaurantName) {
        BookingResponse response = fromEntity(booking);
        response.setRestaurantName(restaurantName);
        return response;
    }
}
