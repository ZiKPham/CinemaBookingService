package com.cinema.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.domain.User;
import com.cinema.domain.request.ReqCreateUserDTO;
import com.cinema.domain.response.ResUserDTO;
import com.cinema.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResUserDTO> registerUser(@Valid @RequestBody ReqCreateUserDTO reqCreateUserDTO) {
        User user = this.userService.handleCreateUser(reqCreateUserDTO);
        ResUserDTO resUserDTO = this.userService.convertToResUserDTO(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(resUserDTO);
    }
}