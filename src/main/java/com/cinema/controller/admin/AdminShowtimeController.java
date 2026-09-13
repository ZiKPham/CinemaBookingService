package com.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.Showtime;
import com.cinema.domain.request.ReqCreateShowtimeDTO;
import com.cinema.domain.request.ReqUpdateShowtimeDTO;
import com.cinema.domain.response.ResSeatDTO;
import com.cinema.domain.response.ResShowtimeDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.service.ShowtimeService;
import com.cinema.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;

    public AdminShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @PostMapping
    public ResponseEntity<ResShowtimeDTO> createShowtime(@Valid @RequestBody ReqCreateShowtimeDTO req)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.showtimeService.handleCreateShowtime(req));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllShowtimes(Specification<Showtime> spec, Pageable pageable) {
        return ResponseEntity.ok(this.showtimeService.fetchAllShowtimes(spec, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResShowtimeDTO> getShowtimeById(@PathVariable("id") long id) throws IdInvalidException {
        return ResponseEntity.ok(this.showtimeService.fetchShowtimeById(id));
    }

    @PutMapping
    public ResponseEntity<ResShowtimeDTO> updateShowtime(@Valid @RequestBody ReqUpdateShowtimeDTO reqDTO)
            throws IdInvalidException {
        ResShowtimeDTO res = this.showtimeService.handleUpdateShowtime(reqDTO);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable("id") long id) throws IdInvalidException {
        this.showtimeService.handleDeleteShowtime(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<ResSeatDTO>> getSeatsByShowtime(
            @PathVariable("showtimeId") long showtimeId) throws IdInvalidException {
        List<ResSeatDTO> seatMap = this.showtimeService.getSeatMapByShowtime(showtimeId);
        return ResponseEntity.ok(seatMap);
    }
}