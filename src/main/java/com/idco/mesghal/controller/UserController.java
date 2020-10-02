package com.idco.mesghal.controller;

import com.idco.mesghal.model.request.LoginRequest;
import com.idco.mesghal.model.response.LoginResponse;
import com.idco.mesghal.entity.User;
import com.idco.mesghal.model.response.StatusResponse;
import com.idco.mesghal.repository.UserRepository;
import com.idco.mesghal.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            //todo change response to only show status
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            return ResponseEntity.created(URI.create("/user")).body(savedUser);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            } catch (Exception e) {
                return ResponseEntity.ok(new StatusResponse(200, "invalid username or password"));
            }
            return ResponseEntity.ok().body(new LoginResponse(jwtUtil.generateToken(request.getUsername())));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}