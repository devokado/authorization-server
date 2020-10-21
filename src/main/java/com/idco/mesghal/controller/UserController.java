package com.idco.mesghal.controller;

import com.idco.mesghal.model.request.LoginRequest;
import com.idco.mesghal.model.request.RefreshTokenRequest;
import com.idco.mesghal.model.request.RegisterRequest;
import com.idco.mesghal.model.request.ResetPasswordRequest;
import com.idco.mesghal.model.response.StatusResponse;
import com.idco.mesghal.service.UserService;
import lombok.val;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest user) {
        try {
            StatusResponse statusResponse = userService.createUserInKeycloak(user);
            return ResponseEntity.status(statusResponse.getStatus())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(statusResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            val token = userService.getToken(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(token);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            val token = userService.getRefreshToken(request.getRefresh_token());
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(token);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RolesAllowed("user")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization") String token) {
        try {
            String[] jwt = token.split(" ");
            AccessToken accessToken = TokenVerifier.create(jwt[1], AccessToken.class).getToken();
            String userId = accessToken.getSubject();
            userService.logoutUser(userId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | VerificationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RolesAllowed("user")
    @PostMapping("/update/password")
    public ResponseEntity<?> resetPassword(@RequestHeader(value = "Authorization") String token,
                                           @RequestBody ResetPasswordRequest model) {
        try {
            String[] jwt = token.split(" ");
            AccessToken accessToken = TokenVerifier.create(jwt[1], AccessToken.class).getToken();
            userService.resetPassword(accessToken.getSubject(), model.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | VerificationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}