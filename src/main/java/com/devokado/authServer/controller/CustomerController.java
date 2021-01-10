package com.devokado.authServer.controller;

import com.devokado.authServer.exceptions.BadRequestException;
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

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final LocaleHelper locale;

    @Autowired
    public CustomerController(CustomerService customerService, LocaleHelper locale) {
        this.customerService = customerService;
        this.locale = locale;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Context HttpServletRequest request, @Valid @RequestBody OtpRequest otpRequest) {
        customerService.otp(otpRequest);
        return ResponseEntity.ok(Response.create(locale.getString("codeSent"), request));
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> oAuth(@Valid @RequestBody AuthRequest authRequest) {
        try {
            HttpResponse response = customerService.prepareOAuth(authRequest);
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

    @PatchMapping("/me")
    public ResponseEntity<?> patchUpdate(HttpServletRequest request, @RequestBody UserPatchRequest userPatchRequest) {
        String userId = customerService.extractAccessToken(request).getPreferredUsername();
        User updatedUser = customerService.partialUpdate(userPatchRequest, Long.getLong(userId));
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me")
    public ResponseEntity<?> putUpdate(HttpServletRequest request,
                                       @Valid @RequestBody UserUpdateRequest updateRequest) {
        String userId = customerService.extractAccessToken(request).getPreferredUsername();
        User updatedUser = customerService.update(updateRequest, Long.getLong(userId));
        return ResponseEntity.ok(updatedUser);
    }
}