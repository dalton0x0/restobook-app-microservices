package com.restobook.bookingservice.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelBookingRequest {

    @Size(max = 500, message = "La raison d'annulation ne peut pas dépasser 500 caractères")
    private String reason;
}
