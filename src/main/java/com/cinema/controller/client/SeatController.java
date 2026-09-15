package com.cinema.controller.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.domain.User;
import com.cinema.domain.request.ReqHoldSeatDTO;
import com.cinema.domain.response.ResHoldSeatDTO;
import com.cinema.service.SeatLockService;
import com.cinema.service.UserService;
import com.cinema.util.error.IdInvalidException;

import jakarta.validation.Valid;

import java.security.Principal;
import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatLockService seatLockService;
    private final UserService userService;

    public SeatController(SeatLockService seatLockService, UserService userService) {
        this.seatLockService = seatLockService;
        this.userService = userService;
    }

    @PostMapping("/hold")
    public ResponseEntity<?> holdSeats(@Valid @RequestBody ReqHoldSeatDTO request, Principal principal)
            throws IdInvalidException {
        String email = principal.getName();
        User currentUser = userService.getUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        for (Long seatId : request.getSeatIds()) {
            boolean success = seatLockService.lockSeat(request.getShowtimeId(), seatId, currentUser.getId());
            if (!success) {
                throw new IdInvalidException("Ghế có ID " + seatId + " đang được giữ hoặc đã có người chọn!");
            }
        }

        Instant expiresAt = Instant.now().plusSeconds(300);

        ResHoldSeatDTO res = new ResHoldSeatDTO(request.getShowtimeId(), request.getSeatIds(), expiresAt,
                "Giữ ghế thành công trong 5 phút");

        return ResponseEntity.ok(res);
    }

}
