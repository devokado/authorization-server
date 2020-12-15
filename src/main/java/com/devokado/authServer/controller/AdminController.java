package com.devokado.authServer.controller;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.model.response.Response;
import com.devokado.authServer.service.AdminService;
import com.devokado.authServer.util.LocaleHelper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    LocaleHelper locale;

    /**
     * Register with email and password
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request, @Valid @RequestBody RegisterRequest registerRequest) {
        adminService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(Response.create(201, locale.getString("createdSuccess"), request));
    }

    /**
     * Login with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginModel) {
        try {
            HttpResponse response = adminService.login(loginModel);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get access again with refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenModel) {
        try {
            HttpResponse response = adminService.refreshToken(refreshTokenModel);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchUpdate(@PathVariable long id, @RequestBody UserPatchRequest userPatchRequest) {
        User updatedUser = adminService.partialUpdate(userPatchRequest, id);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putUpdate(@PathVariable long id, @Valid @RequestBody UserUpdateRequest updateRequest) {
        User updatedUser = adminService.update(updateRequest, id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Logout and expire tokens
     */
    @PostMapping("/me/logout")
    public ResponseEntity<Response> logout(@Context HttpServletRequest request) {
        try {
            String userId = adminService.extractUserIdFromToken(request);
            adminService.logoutKeycloakUser(userId);
            request.getSession().invalidate();
            request.logout();
            return ResponseEntity.ok(Response.create(locale.getString("logoutSuccess"), request));
        } catch (ServletException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Change password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Response> resetPassword(HttpServletRequest request,
                                                  @RequestBody ResetPasswordRequest resetPasswordModel) {
        try {
            String userId = adminService.extractUserIdFromToken(request);
            adminService.changePassword(userId, resetPasswordModel);
            return ResponseEntity.ok(Response.create(locale.getString("updatePasswordSuccess"), request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //todo update admin/customer with id or accessToken(preferred_username=profileId) in token?
//
//    /**
//     * Update partial user data
//     */
//    @PatchMapping("/me")
//    public ResponseEntity<?> patchUpdate(HttpServletRequest request, @RequestBody UserPatchRequest userPatchRequest) {
//        String userId = userService.getUserIdWithToken(request);
//        User updatedUser = userService.partialUpdate(userPatchRequest, userId);
//        return ResponseEntity.ok(updatedUser);
//    }
//
//    /**
//     * Update user data
//     */
//    @PutMapping("/me")
//    public ResponseEntity<?> putUpdate(HttpServletRequest request,
//                                       @Valid @RequestBody UserUpdateRequest updateRequest) {
//        String userId = userService.getUserIdWithToken(request);
//        User updatedUser = userService.update(updateRequest, userId);
//        return ResponseEntity.ok(updatedUser);
//    }
}