package com.cinema.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.request.ReqCreateCinemaDTO;
import com.cinema.domain.request.ReqUpdateCinemaDTO;
import com.cinema.domain.response.ResCinemaDTO;
import com.cinema.service.CinemaService;
import com.cinema.util.error.IdInvalidException;
import com.cinema.util.error.NameInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/cinemas")
public class AdminCinemaController {

    private final CinemaService cinemaService;

    public AdminCinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @PostMapping
    public ResponseEntity<ResCinemaDTO> createCinema(@Valid @RequestBody ReqCreateCinemaDTO reqCreateCinemaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.cinemaService.handleCreateCinema(reqCreateCinemaDTO));
    }

    @GetMapping
    public ResponseEntity<List<ResCinemaDTO>> getAllCinema() {
        return ResponseEntity.ok(this.cinemaService.fetchAllCinemas());
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ResCinemaDTO>> getCinemaByName(@PathVariable String name) throws NameInvalidException {
        return ResponseEntity.ok(this.cinemaService.fetchCinemaByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResCinemaDTO> updateCinema(@PathVariable long id, @RequestBody ReqUpdateCinemaDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.cinemaService.handleUpdaCinema(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCinema(@PathVariable long id) throws IdInvalidException {
        this.cinemaService.handleDeteCinema(id);
        return ResponseEntity.ok(null);
    }
}