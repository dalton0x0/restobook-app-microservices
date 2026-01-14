package com.restobook.bookingservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AvailableSlot {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime time;
        private Integer availableCapacity;
        private Boolean available;
        private Boolean isNextDay;
    }
}
