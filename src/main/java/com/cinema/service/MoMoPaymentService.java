package com.cinema.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.cinema.domain.Booking;
import com.cinema.repository.BookingRepository;
import com.cinema.util.constant.BookingStatus;
import com.cinema.util.error.IdInvalidException;

@Service
public class MoMoPaymentService {

    private final BookingRepository bookingRepository;

    public MoMoPaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Tạo URL thanh toán MoMo Sandbox
     */
    public String createMoMoPayment(Long bookingId) throws IdInvalidException {
        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại với ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IdInvalidException("Đơn đặt vé này không ở trạng thái chờ thanh toán");
        }

        // Trả về thẳng URL trang Mock nội bộ để test cực kỳ mượt mà
        long amount = (long) booking.getTotalPrice();
        return "http://localhost:8080/api/v1/payments/momo/mock-payment-page?bookingId=" + bookingId + "&amount="
                + amount;
    }

    /**
     * Hàm tính chữ ký HMAC SHA256 cho MoMo
     */
    private String hmacSHA256(String key, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa chữ ký MoMo", e);
        }
    }

    public boolean processMoMoCallback(String orderId, int resultCode) throws IdInvalidException {
        Long bookingId = Long.parseLong(orderId.split("_")[0]);
        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            return booking.getStatus() == BookingStatus.PAID;
        }

        if (resultCode == 0) {
            booking.setStatus(BookingStatus.PAID);
            this.bookingRepository.save(booking);

            // Logic sinh mã QR và gửi email xác nhận ở đây...
            return true;
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            this.bookingRepository.save(booking);
            return false;
        }
    }
}