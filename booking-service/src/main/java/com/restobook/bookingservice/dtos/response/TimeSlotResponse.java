package com.restobook.bookingservice.dtos.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {

    private LocalDate date;
    private List<AvailableSlot> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableSlot {
        private LocalTime time;
        private Integer availableCapacity;
        private Boolean available;
    }
}
