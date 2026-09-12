package com.cinema.domain.response;

import com.cinema.domain.Seat;
import com.cinema.util.constant.SeatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSeatDTO {
    private long id;
    private String seatNumber;
    private SeatType seatType;
    private boolean isBooked; // true nếu đã có người đặt/giữ chỗ, false nếu còn trống

    public ResSeatDTO(Seat seat, boolean isBooked) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.seatType = seat.getSeatType();
        this.isBooked = isBooked;
    }
}