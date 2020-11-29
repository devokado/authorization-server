package com.devokado.authServer.controller;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.model.response.StatusResponse;
import com.devokado.authServer.repository.UserRepository;
import com.devokado.authServer.service.UserService;
import com.devokado.authServer.util.LocaleHelper;
import com.devokado.authServer.util.Validate;
import com.kavenegar.sdk.excepctions.ApiException;
import com.kavenegar.sdk.excepctions.HttpException;
import com.kavenegar.sdk.models.SendResult;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    Environment environment;

    @Autowired
    LocaleHelper localeHelper;

    /**
     * Register user with personal data
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest registerRequest) {
        try {
            Object result = userService.createKeycloakUser(registerRequest);
            if (result instanceof Response) {
                Response response = (Response) result;
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                if (registerRequest != null) {
                    User model = UserRequest.createUser(registerRequest);
                    model.setKuuid(userId);
                    userService.save(model);
                }
                return ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(new StatusResponse(201, "created in keycloak successfully"));
            } else {
                StatusResponse statusResponse = (StatusResponse) result;
                return ResponseEntity.status(statusResponse.getStatus())
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(statusResponse);
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Login with username or email
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            val token = userService.getToken(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(token);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get access again with refresh token
     */
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            val token = userService.getRefreshToken(refreshTokenRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                    .body(token);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logout user and expire user tokens
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization") String token) {
        try {
            userService.logoutKeycloakUser(token);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset user password
     */
    @PostMapping("/update/password")
    public ResponseEntity<?> resetPassword(@RequestHeader(value = "Authorization") String token,
                                           @RequestBody ResetPasswordRequest model) {
        try {
            AccessToken accessToken = userService.tokenParser(token);
            User userModel = userService.getWithKuuid(userService.tokenParser(token).getSubject());
            userModel.setPassword(new BCryptPasswordEncoder().encode(model.getNewPassword()));
            userService.save(userModel);
            userService.updateKeycloakUserPassword(accessToken.getSubject(), model.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user data
     */
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestHeader(value = "Authorization") String token,
                                    @Valid @RequestBody UserRequest registerRequest) {
        AccessToken accessToken = userService.tokenParser(token);
        if (registerRequest != null) {
            //todo update email in keycloak
            //todo merge user update and update password
            //todo check email verification
            //todo check token is active for all controllers
            //todo check logout and implement it
            //todo debug user update
            //todo set response message for all controllers
            User userModel = userService.getWithKuuid(accessToken.getSubject());
            User newUser = UserRequest.createUser(registerRequest);
            if (userModel != null) {
                if (newUser.getActive() != null)
                    userModel.setActive(newUser.getActive());

                if (!StringUtils.isEmpty(newUser.getEmail()))
                    userModel.setEmail(newUser.getEmail());

                if (!StringUtils.isEmpty(newUser.getFirstname()))
                    userModel.setFirstname(newUser.getFirstname());

                if (!StringUtils.isEmpty(newUser.getLastname()))
                    userModel.setLastname(newUser.getLastname());

                if (!StringUtils.isEmpty(newUser.getPassword()))
                    userModel.setPassword(newUser.getPassword());

                if (!StringUtils.isEmpty(newUser.getOtp()))
                    userModel.setOtp(newUser.getOtp());
            }

            userService.save(userModel);
//            userService.updateWithKuuid(UserRequest.createUser(registerRequest), accessToken.getSubject());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).build();
    }

    /**
     * Validate mobile with sms
     */
    @PostMapping("/otp")
    public ResponseEntity<?> otp(@Valid @RequestBody OtpRequest request, @RequestHeader(value = "Authorization") String token) {
        try {
            if (Validate.isValidMobile(request.getMobile())) {
                String code = userService.createSMSCode();
                long expireTime = Long.parseLong(Objects.requireNonNull(environment.getProperty("otp.code.expiretime"))) + System.currentTimeMillis();
                User user = userService.getWithKuuid(userService.tokenParser(token).getSubject());
                user.setOtp(code + "_" + expireTime);
                userService.save(user);
                SendResult result = userService.sendSMS(code, request.getMobile());
                return ResponseEntity.ok().build();
            } else
                return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("invalidMobile")));
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
    public ResponseEntity<?> otpVerification(@RequestBody OtpVerificationRequest request) {
        User user = userService.getWithMobile(request.getMobile());
        if (user != null) {
            String[] otp = user.getOtp().split("_");
            String otpCode = otp[0];
            long otpExpireTime = Long.parseLong(otp[1]);
            if (!request.getCode().equals(otpCode))
                return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("codeNotValid")));
            else if (System.currentTimeMillis() > otpExpireTime)
                return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("codeExpired")));
            else return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("verificationIsOk")));
        } else return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("usernameNotFound")));
    }

    @GetMapping()
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userService.listAll());
    }

    @GetMapping("/delete")
    public ResponseEntity<?> delete() {
        userRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}