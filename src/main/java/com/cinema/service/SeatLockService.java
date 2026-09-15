package com.cinema.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cinema.repository.TicketRepository;
import com.cinema.util.constant.BookingStatus;

@Service
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final TicketRepository ticketRepository;

    public SeatLockService(StringRedisTemplate redisTemplate, TicketRepository ticketRepository) {
        this.redisTemplate = redisTemplate;
        this.ticketRepository = ticketRepository;
    }

    // Thời gian giữ ghế mặc định: 5 phút (300 giây)
    private static final long LOCK_TTL_SECONDS = 300;

    /**
     * Tạo khóa Redis cho ghế của suất chiếu
     * Key format: seat:lock:{showtimeId}:{seatId}
     * 
     * @return true nếu giữ ghế thành công, false nếu ghế đã bị người khác giữ
     */
    public boolean lockSeat(Long showtimeId, Long seatId, Long userId) {

        List<BookingStatus> activeStatus = List.of(BookingStatus.PENDING, BookingStatus.PAID);
        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIds(showtimeId, List.of(seatId), activeStatus);

        if (bookedSeatIds != null && !bookedSeatIds.isEmpty()) {
            return false;
        }

        String lockKey = generateLockKey(showtimeId, seatId);

        String userStrId = String.valueOf(userId);

        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, userStrId, LOCK_TTL_SECONDS,
                TimeUnit.SECONDS);

        return Boolean.TRUE.equals(isLocked);
    }

    public boolean isSeatLocked(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public String getSeatHolder(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        return redisTemplate.opsForValue().get(lockKey);
    }

    public void unlockSeat(Long showtimeId, Long seatId) {
        String lockKey = generateLockKey(showtimeId, seatId);
        redisTemplate.delete(lockKey);
    }

    private String generateLockKey(Long showtimeId, Long seatId) {
        return "seat:lock:" + showtimeId + ":" + seatId;
    }
}
