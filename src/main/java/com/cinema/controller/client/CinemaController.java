package com.cinema.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.response.ResCinemaDTO;
import com.cinema.service.CinemaService;
import com.cinema.util.error.NameInvalidException;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public ResponseEntity<List<ResCinemaDTO>> getAllCinema() {
        return ResponseEntity.ok(this.cinemaService.fetchAllCinemas());
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ResCinemaDTO>> getCinemaByName(@PathVariable String name) throws NameInvalidException {
        return ResponseEntity.ok(this.cinemaService.fetchCinemaByName(name));
    }
}