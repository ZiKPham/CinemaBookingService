package com.cinema.controller.client;

import com.cinema.domain.Movie;
import com.cinema.domain.response.ResMovieDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.service.MovieService;
import com.cinema.util.error.NameInvalidException;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllMovies(Specification<Movie> spec, Pageable pageable) {
        return ResponseEntity.ok(this.movieService.fetchAllMovies(spec, pageable));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ResMovieDTO>> getMovieByName(@PathVariable String name) throws NameInvalidException {
        return ResponseEntity.ok(this.movieService.fetchMovieByName(name));
    }
}