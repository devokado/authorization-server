package com.devokado.authServer.controller;

import com.devokado.authServer.exceptions.BadRequestException;
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
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final LocaleHelper locale;

    @Autowired
    public AdminController(AdminService adminService, LocaleHelper locale) {
        this.adminService = adminService;
        this.locale = locale;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request, @Valid @RequestBody RegisterRequest registerRequest) {
        adminService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(Response.create(201, locale.getString("createdSuccess"), request));
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> oAuth(@Valid @RequestBody AuthRequest authRequest) {
        try {
            HttpResponse response = adminService.prepareOAuth(authRequest);
            if (response == null) {
                throw new BadRequestException(locale.getString("grantTypeInvalid"));
            } else {
                return ResponseEntity.status(response.getStatusLine().getStatusCode())
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @PatchMapping("/me")
    public ResponseEntity<?> patchUpdate(HttpServletRequest request, @RequestBody UserPatchRequest userPatchRequest) {
        String userId = adminService.extractAccessToken(request).getPreferredUsername();
        User updatedUser = adminService.partialUpdate(userPatchRequest, Long.getLong(userId));
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me")
    public ResponseEntity<?> putUpdate(HttpServletRequest request,
                                       @Valid @RequestBody UserUpdateRequest updateRequest) {
        String userId = adminService.extractAccessToken(request).getPreferredUsername();
        User updatedUser = adminService.update(updateRequest, Long.getLong(userId));
        return ResponseEntity.ok(updatedUser);
    }
}