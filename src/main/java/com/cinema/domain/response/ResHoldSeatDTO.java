package com.cinema.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResHoldSeatDTO {
    private Long showtimeId;
    private List<Long> seatIds;
    private Instant expiresAt; // Thời điểm hết hạn giữ ghế (ví dụ: hiện tại + 5 phút)
    private String message;
}