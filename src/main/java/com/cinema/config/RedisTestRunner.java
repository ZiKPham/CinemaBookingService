// package com.cinema.config; // Thay đổi package cho phù hợp với dự án của bạn

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.stereotype.Component;

// @Component
// public class RedisTestRunner implements CommandLineRunner {

// @Autowired
// private StringRedisTemplate redisTemplate;

// @Override
// public void run(String... args) throws Exception {
// try {
// // Thử ghi dữ liệu lên Redis
// redisTemplate.opsForValue().set("test_key", "Hello from Cinema Spring
// Boot!");

// // Thử đọc lại dữ liệu từ Redis
// String value = redisTemplate.opsForValue().get("test_key");

// System.out.println("==========================================");
// System.out.println("REDIS CONNECTION SUCCESS! Value: " + value);
// System.out.println("==========================================");
// } catch (Exception e) {
// System.err.println("REDIS CONNECTION FAILED: " + e.getMessage());
// }
// }
// }