package com.cinema.controller.admin;

import com.cinema.domain.Movie;
import com.cinema.domain.request.ReqCreateMovieDTO;
import com.cinema.domain.request.ReqUpdateMovieDTO;
import com.cinema.domain.response.ResMovieDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.service.MovieService;
import com.cinema.util.error.IdInvalidException;
import com.cinema.util.error.NameInvalidException;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/movies")
public class AdminMovieController {

    private final MovieService movieService;

    public AdminMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<ResMovieDTO> createMovie(@Valid @RequestBody ReqCreateMovieDTO reqCreateMovieDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.movieService.handleCreateMovie(reqCreateMovieDTO));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllMovies(Specification<Movie> spec, Pageable pageable) {
        return ResponseEntity.ok(this.movieService.fetchAllMovies(spec, pageable));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ResMovieDTO>> getMovieByName(@PathVariable String name) throws NameInvalidException {
        return ResponseEntity.ok(this.movieService.fetchMovieByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResMovieDTO> updateMovie(@PathVariable long id, @Valid @RequestBody ReqUpdateMovieDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.movieService.handleUpdateMovie(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable long id) throws IdInvalidException {
        this.movieService.handleDeleteMovie(id);
        return ResponseEntity.ok(null);
    }
}