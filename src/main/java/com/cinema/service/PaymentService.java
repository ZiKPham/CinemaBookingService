package com.cinema.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.config.VNPayConfig;
import com.cinema.domain.Booking;
import com.cinema.repository.BookingRepository;
import com.cinema.util.constant.BookingStatus;
import com.cinema.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final QRCodeService qrCodeService;

    /**
     * Tạo URL thanh toán VNPay Sandbox
     */
    public String createVNPayPayment(Long bookingId, HttpServletRequest request) throws IdInvalidException {
        // Thêm vào ngay đầu hàm createVNPayPayment trong PaymentService.java:
        System.out.println("=== VNPAY TMN CODE CURRENTLY USED: " + vnPayConfig.vnp_TmnCode + " ===");
        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại với ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IdInvalidException("Đơn đặt vé này không ở trạng thái chờ thanh toán");
        }

        long amount = (long) (booking.getTotalPrice() * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", booking.getId() + "_" + vnPayConfig.getRandomNumber(4));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang booking ID: " + booking.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnPayConfig.getIpAddress(request));

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = now.format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        String vnp_ExpireDate = now.plusMinutes(15).format(formatter);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp các tham số theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data (Dùng StandardCharsets trực tiếp để tránh
                // try-catch/exception)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    public PaymentService(VNPayConfig vnPayConfig, BookingRepository bookingRepository, EmailService emailService,
            QRCodeService qrCodeService) {
        this.vnPayConfig = vnPayConfig;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.qrCodeService = qrCodeService;
    }

    @Transactional
    public boolean processCallback(Map<String, String> queryParams) throws IdInvalidException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");

        // 1. Kiểm tra chữ ký bảo mật từ VNPay gửi về (RẤT QUAN TRỌNG)
        // Bạn cần đảm bảo VNPayConfig của bạn có phương thức validateRequest() hoặc
        // tương tự
        boolean checkSecureHash = vnPayConfig.validateSignature(queryParams);
        if (!checkSecureHash) {
            throw new IdInvalidException("Dữ liệu không hợp lệ hoặc chữ ký không khớp (Invalid Signature)");
        }

        if (vnp_TxnRef == null || vnp_TxnRef.isEmpty()) {
            throw new IdInvalidException("Mã tham chiếu giao dịch không hợp lệ");
        }

        Long bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);
        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại với ID: " + bookingId));

        // Kiểm tra xem đơn hàng đã được xử lý trước đó chưa để tránh lặp (Idempotency)
        if (booking.getStatus() != BookingStatus.PENDING) {
            return booking.getStatus() == BookingStatus.PAID;
        }

        if ("00".equals(vnp_ResponseCode)) {
            booking.setStatus(BookingStatus.PAID);
            this.bookingRepository.save(booking);

            // --- LOGIC SAU THANH TOÁN ---
            try {
                // 1. Tạo dữ liệu mã QR
                byte[] qrCode = qrCodeService.generateQRCodeImage("BOOKING_" + booking.getId(), 250, 250);

                // 2. Soạn nội dung HTML Email
                String emailContent = "<h3>Cảm ơn bạn đã đặt vé tại Cinema!</h3>"
                        + "<p>Mã đơn hàng: <b>" + booking.getId() + "</b></p>"
                        + "<p>Tổng tiền: <b>" + booking.getTotalPrice() + " VND</b></p>"
                        + "<p>Vui lòng xuất trình mã QR đính kèm khi đến rạp.</p>";

                // 3. Gửi Email (chạy ngầm bất đồng bộ)
                emailService.sendBookingConfirmationEmail(
                        booking.getUser().getEmail(),
                        "Xác Nhận Đặt Vé Thành Công - Mã Đơn #" + booking.getId(),
                        emailContent,
                        qrCode);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            this.bookingRepository.save(booking);
            return false;
        }
    }

    public Map<String, Object> getRevenueStatistics(String startDate, String endDate) throws IdInvalidException {
        // Lấy toàn bộ đơn hàng có trạng thái thành công (PAID)
        List<Booking> paidBookings = this.bookingRepository.findByStatus(BookingStatus.PAID);

        // Tính tổng doanh thu
        double totalRevenue = paidBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        // Tổng số vé/đơn hàng đã thanh toán thành công
        long totalPaidBookings = paidBookings.size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalPaidBookings", totalPaidBookings);
        statistics.put("details", paidBookings.stream().map(booking -> {
            Map<String, Object> item = new HashMap<>();
            item.put("bookingId", booking.getId());
            item.put("userEmail", booking.getUser().getEmail());
            item.put("movieTitle", booking.getShowtime().getMovie().getName());
            item.put("totalPrice", booking.getTotalPrice());
            item.put("createdAt", booking.getCreatedAt());
            return item;
        }).collect(Collectors.toList()));

        return statistics;
    }

}