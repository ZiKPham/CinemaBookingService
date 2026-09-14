package com.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.domain.Booking;
import com.cinema.domain.response.ResBookingDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.service.BookingService;
import com.cinema.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllShowtimes(
            @Parameter(hidden = true) @Filter Specification<Booking> spec, Pageable pageable) {
        return ResponseEntity.ok(this.bookingService.fetchAllBookings(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResBookingDTO> getBookingDetailForAdmin(@PathVariable("id") Long id)
            throws IdInvalidException {
        // Xem chi tiết bất kỳ vé nào không phân biệt chủ sở hữu
        ResBookingDTO detail = this.bookingService.getBookingDetailAdmin(id);
        return ResponseEntity.ok(detail);
    }
}