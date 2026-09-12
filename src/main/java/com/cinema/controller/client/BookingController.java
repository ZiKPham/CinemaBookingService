package com.cinema.controller.client;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.domain.request.ReqBookingDTO;
import com.cinema.domain.response.ResBookingDTO;
import com.cinema.service.BookingService;
import com.cinema.util.SecurityUtil;
import com.cinema.util.error.IdInvalidException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ResBookingDTO> createBooking(@Valid @RequestBody ReqBookingDTO reqBookingDTO)
            throws IdInvalidException {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Xác thực người dùng không hợp lệ"));

        return ResponseEntity.ok(this.bookingService.handleCreateBooking(reqBookingDTO, currentUserEmail));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResBookingDTO>> getMyBookings() throws IdInvalidException {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Xác thực người dùng không hợp lệ"));
        return ResponseEntity.ok(this.bookingService.getMyBookings(currentUserEmail));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<ResBookingDTO>> getMyBookingHistory(Principal principal) throws IdInvalidException {
        String email = principal.getName();

        List<ResBookingDTO> history = bookingService.getBookingHistoryByUser(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResBookingDTO> getBookingById(@PathVariable("id") Long id, Principal principal)
            throws IdInvalidException {
        String email = principal.getName();
        ResBookingDTO detail = bookingService.getBookingDetail(id, email);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ResBookingDTO> cancelBooking(@PathVariable("id") Long id, Principal principal)
            throws IdInvalidException {
        String email = principal.getName();
        ResBookingDTO cancelledBooking = bookingService.cancelBooking(id, email);
        return ResponseEntity.ok(cancelledBooking);
    }
}
