package com.cinema.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.response.ResRoomDTO;
import com.cinema.service.RoomService;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<ResRoomDTO>> getAllRooms() {
        return ResponseEntity.ok(this.roomService.fetchAllRooms());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResRoomDTO>> getRoomsByCinemaAndName(
            @RequestParam String cinemaName,
            @RequestParam(required = false) String roomName) {
        return ResponseEntity.ok(this.roomService.fetchRoomsByCinemaAndRoomName(cinemaName, roomName));
    }
}