package com.cinema.controller.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.domain.User;
import com.cinema.domain.request.LoginDTO;
import com.cinema.domain.request.RegisterDTO;
import com.cinema.domain.response.ResLoginDTO;
import com.cinema.service.UserService;
import com.cinema.util.SecurityUtil;
import com.cinema.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterDTO registerDTO) throws IdInvalidException {
        User newUser = this.userService.handleRegister(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) throws IdInvalidException {
        User user = this.userService.getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new IdInvalidException("Thông tin đăng nhập không chính xác!");
        }

        boolean isMatched = this.passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!isMatched) {
            throw new IdInvalidException("Thông tin đăng nhập không chính xác!");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Hoặc lấy từ user.getRole()

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String access_token = this.securityUtil.createToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new ResLoginDTO((access_token)));
    }
}
