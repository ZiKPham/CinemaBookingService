package com.cinema.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.domain.Booking;
import com.cinema.domain.Seat;
import com.cinema.domain.Showtime;
import com.cinema.domain.Ticket;
import com.cinema.domain.User;
import com.cinema.domain.request.ReqBookingDTO;
import com.cinema.domain.response.ResBookingDTO;
import com.cinema.domain.response.ResultPaginationDTO;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.repository.TicketRepository;
import com.cinema.repository.UserRepository;
import com.cinema.util.constant.BookingStatus;
import com.cinema.util.constant.SeatType;
import com.cinema.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, TicketRepository ticketRepository,
            ShowtimeRepository showtimeRepository, SeatRepository seatRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResBookingDTO handleCreateBooking(ReqBookingDTO req, String currentUserEmail) throws IdInvalidException {

        User user = this.userRepository.findByEmail(currentUserEmail);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        Showtime showtime = this.showtimeRepository.findById(req.getShowtimeId())
                .orElseThrow(() -> new IdInvalidException("Lịch chiếu không tồn tại với ID: " + req.getShowtimeId()));

        List<Seat> seats = this.seatRepository.findAllById(req.getSeatIds());
        if (seats.size() != req.getSeatIds().size()) {
            throw new IdInvalidException("Một số ghế chọn không tồn tại trong hệ thống");
        }

        long roomOfShowtimeId = showtime.getRoom().getId();
        boolean invalidRoom = seats.stream().anyMatch(seat -> seat.getRoom().getId() != roomOfShowtimeId);
        if (invalidRoom) {
            throw new IdInvalidException("Các ghế được chọn không thuộc phòng chiếu của lịch chiếu này");
        }

        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.PAID);
        List<Long> bookedSeatIds = this.ticketRepository.findBookedSeatIds(req.getShowtimeId(), req.getSeatIds(),
                activeStatuses);
        if (!bookedSeatIds.isEmpty()) {
            throw new IdInvalidException("Các ghế sau đã được đặt hoặc đang giữ chỗ: " + bookedSeatIds);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setPaymentMethod(req.getPaymentMethod());
        booking.setStatus(BookingStatus.PENDING);

        double totalPrice = 0;
        List<Ticket> tickets = new ArrayList<>();

        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seat);

            double seatPrice = showtime.getPrice();
            if (seat.getSeatType() == SeatType.VIP) {
                seatPrice += 20000;
            } else if (seat.getSeatType() == SeatType.SWEETBOX) {
                seatPrice += 50000;
            }

            ticket.setPrice(seatPrice);
            ticket.setTicketCode("TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            totalPrice += seatPrice;
            tickets.add(ticket);
        }

        booking.setTotalPrice(totalPrice);
        booking.setTickets(tickets);

        Booking savedBooking = this.bookingRepository.save(booking);

        return convertToResBookingDTO(savedBooking);
    }

    public List<ResBookingDTO> getMyBookings(String email) {
        List<Booking> bookings = this.bookingRepository.findByUserEmailOrderByIdDesc(email);
        return bookings.stream().map(this::convertToResBookingDTO).collect(Collectors.toList());
    }

    public List<ResBookingDTO> getBookingHistoryByUser(String email) throws IdInvalidException {
        // 1. Tìm user dựa vào email đang đăng nhập
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        // 2. Lấy danh sách booking của user và convert sang DTO
        List<Booking> bookings = this.bookingRepository.findByUser(user);
        return bookings.stream()
                .map(this::convertToResBookingDTO)
                .collect(Collectors.toList());
    }

    public ResBookingDTO getBookingDetail(Long id, String email) throws IdInvalidException {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        Booking booking = this.bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem"));

        return convertToResBookingDTO(booking);
    }

    @Transactional
    public ResBookingDTO cancelBooking(Long id, String email) throws IdInvalidException {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        Booking booking = this.bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép hủy khi đơn hàng còn đang ở trạng thái PENDING (chờ thanh toán)
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IdInvalidException("Chỉ có thể hủy các đơn hàng đang chờ thanh toán (PENDING)");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = this.bookingRepository.save(booking);

        return convertToResBookingDTO(updatedBooking);
    }

    public ResultPaginationDTO fetchAllBookings(Specification<Booking> spec, Pageable pageable) {
        Page<Booking> pageBooking = this.bookingRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageBooking.getTotalPages());
        mt.setTotal(pageBooking.getTotalElements());

        rs.setMeta(mt);

        List<ResBookingDTO> listBooking = pageBooking.getContent().stream()
                .map(item -> convertToResBookingDTO(item)).collect(Collectors.toList());

        rs.setResult(listBooking);
        return rs;
    }

    public ResBookingDTO getBookingDetailAdmin(Long id) throws IdInvalidException {
        Booking booking = this.bookingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng với ID: " + id));
        return convertToResBookingDTO(booking);
    }

    private ResBookingDTO convertToResBookingDTO(Booking booking) {
        ResBookingDTO res = new ResBookingDTO();
        res.setId(booking.getId());
        res.setTotalPrice(booking.getTotalPrice());
        res.setStatus(booking.getStatus());
        res.setPaymentMethod(booking.getPaymentMethod());
        res.setCreatedAt(booking.getCreatedAt());

        ResBookingDTO.UserSummary userSummary = new ResBookingDTO.UserSummary();
        userSummary.setId(booking.getUser().getId());
        userSummary.setFullName(booking.getUser().getFullName());
        userSummary.setEmail(booking.getUser().getEmail());
        res.setUser(userSummary);

        ResBookingDTO.ShowtimeSummary showtimeSummary = new ResBookingDTO.ShowtimeSummary();
        showtimeSummary.setId(booking.getShowtime().getId());
        showtimeSummary.setMovieTitle(booking.getShowtime().getMovie().getName());
        showtimeSummary.setRoomName(booking.getShowtime().getRoom().getName());
        showtimeSummary.setStartTime(booking.getShowtime().getStartTime());
        res.setShowtime(showtimeSummary);

        List<ResBookingDTO.TicketSummary> ticketSummaries = booking.getTickets().stream().map(t -> {
            ResBookingDTO.TicketSummary ts = new ResBookingDTO.TicketSummary();
            ts.setId(t.getId());
            ts.setSeatNumber(t.getSeat().getSeatNumber());
            ts.setPrice(t.getPrice());
            ts.setTicketCode(t.getTicketCode());
            ts.setQrCode(t.getTicketCode()); // Gán mã QR chính là mã vé hoặc chuỗi định danh riêng
            return ts;
        }).toList();

        res.setTickets(ticketSummaries);
        return res;
    }
}
