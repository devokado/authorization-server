package com.devokado.authServer.controller;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.model.response.Response;
import com.devokado.authServer.service.CustomerService;
import com.devokado.authServer.util.LocaleHelper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    LocaleHelper locale;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Context HttpServletRequest request, @Valid @RequestBody OtpRequest otpRequest) {
        customerService.otp(otpRequest);
        return ResponseEntity.ok(Response.create(locale.getString("codeSent"), request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            HttpResponse response = customerService.getOAuthTokenOtp(loginRequest);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenModel) {
        try {
            HttpResponse response = customerService.getRefreshToken(refreshTokenModel);
            return ResponseEntity.status(response.getStatusLine().getStatusCode())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchUpdate(@PathVariable long id, @RequestBody UserPatchRequest userPatchRequest) {
        User updatedUser = customerService.partialUpdate(userPatchRequest, id);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putUpdate(@PathVariable long id, @Valid @RequestBody UserUpdateRequest updateRequest) {
        User updatedUser = customerService.update(updateRequest, id);
        return ResponseEntity.ok(updatedUser);
    }
}