package com.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cinema.domain.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByName(String name);

    List<Movie> findByNameContainingIgnoreCase(String name);
}
