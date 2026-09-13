package com.cinema.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cinema.domain.Booking;
import com.cinema.domain.User;
import com.cinema.util.constant.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByUserEmailOrderByIdDesc(String email);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant time);

    List<Booking> findByUser(User user);

    Optional<Booking> findByIdAndUser(Long id, User user);
}
