package com.cinema.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.response.ResSeatDTO;
import com.cinema.domain.response.ResShowtimeDTO;
import com.cinema.service.ShowtimeService;
import com.cinema.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public ResponseEntity<List<ResShowtimeDTO>> getAllShowtimes() {
        return ResponseEntity.ok(this.showtimeService.fetchAllShowtimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResShowtimeDTO> getShowtimeById(@PathVariable("id") long id) throws IdInvalidException {
        return ResponseEntity.ok(this.showtimeService.fetchShowtimeById(id));
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<ResSeatDTO>> getSeatsByShowtime(
            @PathVariable("showtimeId") long showtimeId) throws IdInvalidException {
        List<ResSeatDTO> seatMap = this.showtimeService.getSeatMapByShowtime(showtimeId);
        return ResponseEntity.ok(seatMap);
    }
}