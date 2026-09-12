package com.cinema.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.domain.response.ResBookingDTO;
import com.cinema.service.BookingService;
import com.cinema.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<ResBookingDTO>> getAllBookingsForAdmin() {
        // Lấy danh sách toàn bộ vé của tất cả người dùng trong hệ thống
        List<ResBookingDTO> bookings = this.bookingService.fetchAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResBookingDTO> getBookingDetailForAdmin(@PathVariable("id") Long id)
            throws IdInvalidException {
        // Xem chi tiết bất kỳ vé nào không phân biệt chủ sở hữu
        ResBookingDTO detail = this.bookingService.getBookingDetailAdmin(id);
        return ResponseEntity.ok(detail);
    }
}