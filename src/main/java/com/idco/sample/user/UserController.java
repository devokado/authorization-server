package com.idco.sample.user;

import com.idco.sample.model.Profile;
import com.idco.sample.model.response.StatusResponse;
import com.idco.sample.repository.ProfileRepository;
import com.idco.sample.user.request.*;
import com.idco.sample.util.LocaleHelper;
import com.idco.sample.util.Validate;
import com.kavenegar.sdk.excepctions.ApiException;
import com.kavenegar.sdk.excepctions.HttpException;
import com.kavenegar.sdk.models.SendResult;
import lombok.val;
import org.jboss.logging.Logger;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    Environment environment;

    @Autowired
    LocaleHelper localeHelper;

    /**
     * Register user with personal data
     */
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
     * Login with mobile
     */
    @PostMapping("/otp")
    public ResponseEntity<?> otp(@Valid @RequestBody OtpRequest request) {
        try {
            if (Validate.isValidMobile(request.getMobile())) {
                //todo save otpcode,mobile and expiretime in user credential or a DB
                String code = userService.createSMSCode();
                Profile profile = new Profile();
                profile.setCode(code);
                profile.setMobile(request.getMobile());
                profile.setExpireTime(Long.parseLong(Objects.requireNonNull(environment.getProperty("otp.code.expiretime"))) + System.currentTimeMillis());
                profileRepository.save(profile);
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
     * Otp verification and get token
     */
    @PostMapping("/otp-verification")
    public ResponseEntity<?> otpVerification(@RequestBody OtpVerificationRequest request) {
        //todo find code and mobile in DB
        Profile profile = profileRepository.findByMobileAndCode(request.getMobile(), request.getCode());
        if (profile != null) {
            //todo check code is valid and doesn't expire
            logger.error(profile.getExpireTime() + " " + System.currentTimeMillis());
            if (System.currentTimeMillis() > profile.getExpireTime())
                return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("tokenExpired")));
            else {
                //todo check user status (exist or no)
                if (userService.isUserExist(request.getMobile())) {
                    //todo if user is exist login user and get token
                    //todo get token with custom attribute(mobile) in keycloak without username and password

                    //todo get user id from username

                    userService.resetPassword(userService.getUserId(request.getMobile()), request.getCode());
                    val token = userService.getToken(new LoginRequest(request.getMobile(), request.getCode()));
                    return ResponseEntity.ok()
                            .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                            .body(token);
                } else {
                    //todo if user doesn't exist first one create user with username=mobile password=code and then login user and get token
                    StatusResponse statusResponse = userService.createUserInKeycloak(new RegisterRequest(request.getMobile(),
                            request.getCode(), "", "", "", request.getMobile()));
                    if (statusResponse.getStatus() == 201) {
                        val token = userService.getToken(new LoginRequest(request.getMobile(), request.getCode()));
                        return ResponseEntity.ok()
                                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                                .body(token);
                    }
                }
            }
        } else
            return ResponseEntity.ok(new StatusResponse(200, localeHelper.getString("usernameNotFound")));
        return ResponseEntity.ok().build();
    }

    /**
     * Get access again with refresh token
     */
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

    /**
     * Logout user and expire user tokens
     */
    @RolesAllowed("user")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestHeader(value = "Authorization") String token) {
        try {
//            request.logout();
            String[] jwt = token.split(" ");
            AccessToken accessToken = TokenVerifier.create(jwt[1], AccessToken.class).getToken();
            String userId = accessToken.getSubject();
            userService.logoutUser(userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException | VerificationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset user password
     */
    @RolesAllowed("user")
    @PostMapping("/update/password")
    public ResponseEntity<?> resetPassword(@RequestHeader(value = "Authorization") String token,
                                           @RequestBody ResetPasswordRequest model) {
        try {
            AccessToken accessToken = userService.tokenParser(token);
            userService.resetPassword(accessToken.getSubject(), model.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user data
     */
    @RolesAllowed("user")
    @PutMapping("/update/user")
    public ResponseEntity<?> update(@RequestHeader(value = "Authorization") String token,
                                    @Valid @RequestBody RegisterRequest registerRequest) {
        AccessToken accessToken = userService.tokenParser(token);
        userService.updateUserInKeycloak(accessToken.getSubject(), registerRequest);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)).build();
    }
}