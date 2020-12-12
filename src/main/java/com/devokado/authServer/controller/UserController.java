package com.devokado.authServer.controller;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.model.response.Response;
import com.devokado.authServer.service.UserService;
import com.devokado.authServer.util.LocaleHelper;
import com.kavenegar.sdk.excepctions.ApiException;
import com.kavenegar.sdk.excepctions.HttpException;
import lombok.val;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
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
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    LocaleHelper locale;

    /**
     * Register user with personal data
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request, @Valid @RequestBody UserRequest registerRequest) {
        try {
            int status = userService.createUser(registerRequest);
            switch (status) {
                case 201:
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                            .body(Response.create(201, locale.getString("createdSuccess"), request));
                case 409:
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                            .body(Response.create(409, locale.getString("duplicateUsername"), request));
                default:
                    return ResponseEntity.status(status)
                            .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                            .body(Response.create(status, locale.getString("failedToCreateUser"), request));
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Login with username or email
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginModel) {
        try {
            HttpResponse response = userService.createToken(loginModel);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (NoSuchElementException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get access again with refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenModel) {
        try {
            val response = userService.getRefreshToken(refreshTokenModel);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (NoSuchElementException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logout user and expire user tokens
     */
    @PostMapping("/me/logout")
    public ResponseEntity<Response> logout(@Context HttpServletRequest request) {
        try {
            String userId = userService.getUserIdWithToken(request);
            userService.logoutKeycloakUser(userId);
            request.getSession().invalidate();
            request.logout();
            return ResponseEntity.ok(Response.create(locale.getString("logoutSuccess"), request));
        } catch (NoSuchElementException | ServletException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Change user password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Response> resetPassword(HttpServletRequest request,
                                                  @RequestBody ResetPasswordRequest resetPasswordModel) {
        try {
            String userId = userService.getUserIdWithToken(request);
            String statusMessage = userService.changePassword(userId, resetPasswordModel);
            return ResponseEntity.ok(Response.create(statusMessage, request));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validate mobile with sms
     */
    @PostMapping("/otp")
    public ResponseEntity<Response> otp(HttpServletRequest request,
                                        @Valid @RequestBody OtpRequest otpRequest) {
        try {
            String userId = userService.getUserIdWithToken(request);
            String statusMessage = userService.sendVerificationSMS(userId, otpRequest);
            return ResponseEntity.ok(Response.create(statusMessage, request));
        } catch (HttpException ex) {
            logger.error("HttpException  : " + ex.getMessage());
            return ResponseEntity.status(ex.getCode()).build();
        } catch (ApiException ex) {
            logger.error("ApiException  : " + ex.getMessage());
            return ResponseEntity.status(ex.getCode().getValue()).build();
        }
    }

    /**
     * mobile verification
     */
    @PostMapping("/otp-verification")
    public ResponseEntity<?> otpVerification(
            HttpServletRequest request,
            @RequestBody OtpVerificationRequest otpVerificationModel) {
        String statusMessage = userService.verifyMobile(otpVerificationModel);
        return ResponseEntity.ok(Response.create(statusMessage, request));
    }

    /**
     * Update partial user data
     */
    @PatchMapping("/me")
    public ResponseEntity<?> patchUpdate(HttpServletRequest request, @RequestBody UserPatchRequest userPatchRequest) {
        String userId = userService.getUserIdWithToken(request);
        User updatedUser = userService.partialUpdate(userPatchRequest, userId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user data
     */
    @PutMapping("/me")
    public ResponseEntity<?> putUpdate(HttpServletRequest request,
                                       @Valid @RequestBody UserUpdateRequest updateRequest) {
        String userId = userService.getUserIdWithToken(request);
        User updatedUser = userService.update(updateRequest, userId);
        return ResponseEntity.ok(updatedUser);
    }
}