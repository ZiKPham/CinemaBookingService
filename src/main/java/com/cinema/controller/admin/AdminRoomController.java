package com.cinema.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.request.ReqCreateRoomDTO;
import com.cinema.domain.request.ReqUpdateRoomDTO;
import com.cinema.domain.response.ResRoomDTO;
import com.cinema.service.RoomService;
import com.cinema.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<ResRoomDTO> createRoom(@Valid @RequestBody ReqCreateRoomDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roomService.handleCreateRoom(reqDTO));
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

    @PutMapping("/{id}")
    public ResponseEntity<ResRoomDTO> updateRoom(@PathVariable long id, @RequestBody ReqUpdateRoomDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.roomService.handleUpdateRoom(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable long id) throws IdInvalidException {
        this.roomService.handleDeleteRoom(id);
        return ResponseEntity.ok(null);
    }
}