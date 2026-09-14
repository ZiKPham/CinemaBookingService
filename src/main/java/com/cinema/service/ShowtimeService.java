package com.cinema.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.domain.Movie;
import com.cinema.domain.Room;
import com.cinema.domain.Seat;
import com.cinema.domain.Showtime;
import com.cinema.domain.request.ReqCreateShowtimeDTO;
import com.cinema.domain.request.ReqUpdateShowtimeDTO;
import com.cinema.domain.response.ResSeatDTO;
import com.cinema.domain.response.ResShowtimeDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.repository.TicketRepository;
import com.cinema.util.constant.BookingStatus;
import com.cinema.util.error.IdInvalidException;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository,
            RoomRepository roomRepository, SeatRepository seatRepository, TicketRepository ticketRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    public ResShowtimeDTO handleCreateShowtime(ReqCreateShowtimeDTO reqDTO) throws IdInvalidException {
        Optional<Movie> movieOptional = this.movieRepository.findById(reqDTO.getMovieId());
        if (!movieOptional.isPresent()) {
            throw new IdInvalidException("Movie với id = " + reqDTO.getMovieId() + " không tồn tại");
        }

        Optional<Room> roomOptional = this.roomRepository.findById(reqDTO.getRoomId());
        if (!roomOptional.isPresent()) {
            throw new IdInvalidException("Room với id = " + reqDTO.getRoomId() + " không tồn tại");
        }

        if (!reqDTO.getStartTime().isBefore(reqDTO.getEndTime())) {
            throw new IdInvalidException("Thời gian bắt đầu " + reqDTO.getStartTime()
                    + " phải trước thời gian kết thúc " + reqDTO.getEndTime());
        }

        List<Showtime> overlaps = this.showtimeRepository.findOverlappingShowtimes(
                reqDTO.getRoomId(), reqDTO.getStartTime(), reqDTO.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IdInvalidException("Khung giờ này phòng đã có suất chiếu khác. Vui lòng chọn giờ khác!");
        }

        Showtime showtime = new Showtime();
        showtime.setStartTime(reqDTO.getStartTime());
        showtime.setEndTime(reqDTO.getEndTime());
        showtime.setPrice(reqDTO.getPrice());
        showtime.setMovie(movieOptional.get());
        showtime.setRoom(roomOptional.get());

        Showtime saved = this.showtimeRepository.save(showtime);
        return this.convertToResShowtimeDTO(saved);
    }

    public ResultPaginationDTO fetchAllShowtimes(Specification<Showtime> spec, Pageable pageable) {
        Page<Showtime> pageShowtime = this.showtimeRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageShowtime.getTotalPages());
        mt.setTotal(pageShowtime.getTotalElements());

        rs.setMeta(mt);

        List<ResShowtimeDTO> listShowtime = pageShowtime.getContent().stream()
                .map(item -> convertToResShowtimeDTO(item)).collect(Collectors.toList());

        rs.setResult(listShowtime);
        return rs;
    }

    public ResShowtimeDTO fetchShowtimeById(Long id) throws IdInvalidException {
        Optional<Showtime> sOptional = this.showtimeRepository.findById(id);
        if (!sOptional.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + id + " không tồn tại");
        }
        return this.convertToResShowtimeDTO(sOptional.get());
    }

    public ResShowtimeDTO handleUpdateShowtime(ReqUpdateShowtimeDTO reqDTO) throws IdInvalidException {
        Optional<Showtime> sOptional = this.showtimeRepository.findById(reqDTO.getId());
        if (!sOptional.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + reqDTO.getId() + " không tồn tại");
        }

        Optional<Movie> movieOpt = this.movieRepository.findById(reqDTO.getMovieId());
        if (!movieOpt.isPresent()) {
            throw new IdInvalidException("Movie với id = " + reqDTO.getMovieId() + " không tồn tại");
        }

        Optional<Room> roomOpt = this.roomRepository.findById(reqDTO.getRoomId());
        if (!roomOpt.isPresent()) {
            throw new IdInvalidException("Room với id = " + reqDTO.getRoomId() + " không tồn tại");
        }

        if (!reqDTO.getStartTime().isBefore(reqDTO.getEndTime())) {
            throw new IdInvalidException("Thời gian bắt đầu phải diễn ra trước thời gian kết thúc");
        }

        List<Showtime> overlaps = this.showtimeRepository.findOverLappingShowtimesForUpdate(
                reqDTO.getId(), reqDTO.getRoomId(), reqDTO.getStartTime(), reqDTO.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IdInvalidException("Khung giờ này phòng đã có suất chiếu khác. Vui lòng chọn giờ khác!");
        }

        Showtime currentShowtime = sOptional.get();
        currentShowtime.setStartTime(reqDTO.getStartTime());
        currentShowtime.setEndTime(reqDTO.getEndTime());
        currentShowtime.setPrice(reqDTO.getPrice());
        currentShowtime.setMovie(movieOpt.get());
        currentShowtime.setRoom(roomOpt.get());

        Showtime updated = this.showtimeRepository.save(currentShowtime);
        return this.convertToResShowtimeDTO(updated);
    }

    public void handleDeleteShowtime(long id) throws IdInvalidException {
        Optional<Showtime> showtimeOpt = this.showtimeRepository.findById(id);
        if (!showtimeOpt.isPresent()) {
            throw new IdInvalidException("Showtime với id = " + id + " không tồn tại");
        }
        this.showtimeRepository.deleteById(id);
    }

    public List<ResSeatDTO> getSeatMapByShowtime(long showtimeId) throws IdInvalidException {
        Showtime showtime = this.showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IdInvalidException("Suất chiếu không tồn tại với ID: " + showtimeId));

        // Lấy toàn bộ ghế thuộc phòng chiếu của suất này
        List<Seat> allSeats = this.seatRepository.findByRoomId(showtime.getRoom().getId());

        // Các trạng thái đơn hàng tính là ghế đã bị khóa/đặt
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.PAID);
        List<Long> bookedSeatIds = this.ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId, activeStatuses);

        // Map sang DTO để trả về trạng thái
        return allSeats.stream().map(seat -> {
            boolean isBooked = bookedSeatIds.contains(seat.getId());
            return new ResSeatDTO(seat, isBooked); // Hoặc SeatResponseDTO tùy theo file DTO của bạn
        }).collect(Collectors.toList());
    }

    public ResShowtimeDTO convertToResShowtimeDTO(Showtime showtime) {
        ResShowtimeDTO res = new ResShowtimeDTO();
        res.setId(showtime.getId());
        res.setStartTime(showtime.getStartTime());
        res.setEndTime(showtime.getEndTime());
        res.setPrice(showtime.getPrice());
        res.setCreatedAt(showtime.getCreatedAt());
        res.setUpdatedAt(showtime.getUpdatedAt());

        if (showtime.getMovie() != null) {
            ResShowtimeDTO.MovieShowtime m = new ResShowtimeDTO.MovieShowtime();
            m.setId(showtime.getMovie().getId());
            m.setName(showtime.getMovie().getName());
            res.setMovie(m);
        }

        if (showtime.getRoom() != null) {
            ResShowtimeDTO.RoomShowtime r = new ResShowtimeDTO.RoomShowtime();
            r.setId(showtime.getRoom().getId());
            r.setName(showtime.getRoom().getName());
            if (showtime.getRoom().getCinema() != null) {
                r.setCinemaName(showtime.getRoom().getCinema().getName());
            }
            res.setRoom(r);
        }

        return res;
    }
}
