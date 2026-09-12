package com.cinema.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cinema.domain.Cinema;
import com.cinema.domain.Room;
import com.cinema.domain.request.ReqCreateRoomDTO;
import com.cinema.domain.request.ReqUpdateRoomDTO;
import com.cinema.domain.response.ResRoomDTO;
import com.cinema.repository.CinemaRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.util.error.IdInvalidException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;

    public RoomService(RoomRepository roomRepository, CinemaRepository cinemaRepository) {
        this.roomRepository = roomRepository;
        this.cinemaRepository = cinemaRepository;
    }

    public ResRoomDTO handleCreateRoom(ReqCreateRoomDTO reqDTO) throws IdInvalidException {
        Optional<Cinema> cOptional = this.cinemaRepository.findById(reqDTO.getCinemaId());
        if (!cOptional.isPresent()) {
            throw new IdInvalidException("Cinema với id = " + reqDTO.getCinemaId() + " không tồn tại");
        }

        Room room = new Room();
        room.setName(reqDTO.getName());
        room.setTotalSeats(reqDTO.getTotalSeats());
        room.setCinema(cOptional.get());

        Room savedRoom = this.roomRepository.save(room);
        return this.convertToResRoomDTO(savedRoom);
    }

    public List<ResRoomDTO> fetchAllRooms() {
        List<Room> rooms = this.roomRepository.findAll();
        return rooms.stream()
                .map(this::convertToResRoomDTO)
                .collect(Collectors.toList());
    }

    public List<ResRoomDTO> fetchRoomsByCinemaAndRoomName(String cinemaName, String roomName) {
        List<Room> rooms;

        if (roomName != null && !roomName.trim().isEmpty()) {
            rooms = this.roomRepository.findByCinemaNameContainingIgnoreCaseAndNameContainingIgnoreCase(cinemaName,
                    roomName);
        } else {
            rooms = this.roomRepository.findByCinemaNameContainingIgnoreCase(cinemaName);
        }

        return rooms.stream()
                .map(this::convertToResRoomDTO)
                .collect(Collectors.toList());
    }

    public ResRoomDTO handleUpdateRoom(long id, ReqUpdateRoomDTO reqDTO) throws IdInvalidException {
        Optional<Room> rOptional = this.roomRepository.findById(id);
        if (!rOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + id + " không tồn tại");
        }

        Room currentRoom = rOptional.get();
        if (reqDTO.getName() != null && !reqDTO.getName().trim().isEmpty()) {
            currentRoom.setName(reqDTO.getName());
        }

        if (reqDTO.getTotalSeats() != null) {
            currentRoom.setTotalSeats(reqDTO.getTotalSeats());
        }

        if (reqDTO.getCinemaId() != null) {
            Optional<Cinema> cinemaOptional = this.cinemaRepository.findById(reqDTO.getCinemaId());
            if (!cinemaOptional.isPresent()) {
                throw new IdInvalidException("Cinema với id = " + reqDTO.getCinemaId() + " không tồn tại");
            }
            currentRoom.setCinema(cinemaOptional.get());
        }

        Room updatedRoom = this.roomRepository.save(currentRoom);
        return this.convertToResRoomDTO(updatedRoom);
    }

    public void handleDeleteRoom(long id) throws IdInvalidException {
        Optional<Room> roomOptional = this.roomRepository.findById(id);
        if (!roomOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + id + " không tồn tại");
        }
        this.roomRepository.deleteById(id);
    }

    public ResRoomDTO convertToResRoomDTO(Room room) {
        ResRoomDTO res = new ResRoomDTO();
        res.setId(room.getId());
        res.setName(room.getName());
        res.setTotalSeats(room.getTotalSeats());
        res.setCreatedAt(room.getCreatedAt());
        res.setUpdatedAt(room.getUpdatedAt());

        if (room.getCinema() != null) {
            ResRoomDTO.CinemaRoom cinemaRoom = new ResRoomDTO.CinemaRoom();
            cinemaRoom.setId(room.getCinema().getId());
            cinemaRoom.setName(room.getCinema().getName());
            res.setCinema(cinemaRoom);
        }

        return res;
    }
}
