package com.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cinema.domain.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByCinemaNameContainingIgnoreCaseAndNameContainingIgnoreCase(String cinemaName, String roomName);

    List<Room> findByCinemaNameContainingIgnoreCase(String cinemaName);

}
