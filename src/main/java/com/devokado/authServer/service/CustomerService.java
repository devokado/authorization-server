package com.devokado.authServer.service;

import com.devokado.authServer.exceptions.*;
import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.AuthRequest;
import com.devokado.authServer.model.request.OtpRequest;
import com.devokado.authServer.util.StringHelper;
import com.devokado.authServer.util.ValidationHelper;
import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.models.SendResult;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Alimodares
 * @since 2020-12-13
 */
@Service
public class CustomerService extends UserService {

    @Value("${otp.code.size}")
    private int otpCodeSize;

    @Value("${otp.kavenegar.apikey}")
    private String apiKey;

    @Value("${otp.kavenegar.template}")
    private String template;

    @Value("${otp.code.expiretime}")
    private long otpExpireTime;

    private JavaMailSender emailSender;

    @Autowired
    public void init(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void otp(OtpRequest otpRequest) {
        String identifier = otpRequest.getUsername();
        if (ValidationHelper.isValidMobile(identifier)) {
            otpWithMobile(identifier);
        } else if (ValidationHelper.isValidMail(identifier)) {
            otpWithEmail(identifier);
        } else {
            throw new BadRequestException(locale.getString("invalidMobileOrEmail"));
        }
    }

    private void otpWithEmail(String email) {
        Optional<User> user = this.findByEmail(email);
        checkUser(user, email);
    }

    private void otpWithMobile(String mobile) {
        Optional<User> user = this.findByMobile(mobile);
        checkUser(user, mobile);
    }

    private void checkUser(Optional<User> user, String identifier) {
        if (user.isPresent()) {
            provideOtp(user.get(), identifier);
        } else {
            registerUser(identifier);
        }
    }

    private void registerUser(String identifier) {
        User user = new User();
        user.setOtpCdt(System.currentTimeMillis());
        user.setOtpStatus(0);
        if (ValidationHelper.isValidMobile(identifier)) {
            user.setMobile(identifier);
        } else if (ValidationHelper.isValidMail(identifier)) {
            user.setEmail(identifier);
        }
        User createdUser = this.save(user);
        String code = StringHelper.generateCode(otpCodeSize);
        Response response = this.createKeycloakUser("app-customer", createdUser.getId(), code);
        if (response.getStatus() == 201) {
            createdUser.setKuuid(this.getKuuidFromResponse(response));
            this.save(createdUser);
            sendVerificationCode(code, identifier);
        } else {
            this.deleteById(createdUser.getId());
            throw new RestException(locale.getString("failedToCreateUser"), HttpStatus.valueOf(response.getStatus()));
        }
    }

    private void provideOtp(User user, String identifier) {
        try {
            String code = StringHelper.generateCode(otpCodeSize);
            HttpResponse response = this.updateKeycloakUserPassword(user.getKuuid(), code);
            if (response.getStatusLine().getStatusCode() == 204) {
                user.setOtpCdt(System.currentTimeMillis());
                user.setOtpStatus(0);
                this.save(user);
                sendVerificationCode(code, identifier);
            } else
                throw new CustomException(EntityUtils.toString(response.getEntity()), response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendVerificationCode(String code, String identifier) {
        if (ValidationHelper.isValidMobile(identifier)) {
            this.sendSMS(code, identifier);
        } else if (ValidationHelper.isValidMail(identifier)) {
            this.sendEmail(code, identifier);
        }
    }

    private void sendSMS(String code, String mobile) {
        KavenegarApi api = new KavenegarApi(apiKey);
        SendResult result = api.verifyLookup(mobile, code, template);
        if (result.getStatus() != 5)
            throw new InternalServerErrorException(locale.getString("failedSendSms"));
    }

    private void sendEmail(String code, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("keycloak@jdro.ir");
        message.setTo(email);
        message.setSubject(locale.getString("emailSubject"));
        message.setText(String.format(locale.getString("emailTemplate"), code));
        emailSender.send(message);
    }

    public HttpResponse getOAuthTokenOtp(AuthRequest authRequest) {
        String identifier = authRequest.getUsername();
        if (ValidationHelper.isValidMobile(identifier)) {
            Optional<User> user = this.findByMobile(identifier);
            return provideOAuth(user.orElseThrow(), authRequest);
        } else if (ValidationHelper.isValidMail(identifier)) {
            Optional<User> user = this.findByEmail(identifier);
            return provideOAuth(user.orElseThrow(), authRequest);
        } else
            throw new BadRequestException(locale.getString("invalidMobileOrEmail"));
    }

    private HttpResponse provideOAuth(User user, AuthRequest authRequest) {
        if (user.getOtpStatus() == 1)
            throw new NotAvailableException(locale.getString("codeExpired"));
        else if (System.currentTimeMillis() > (user.getOtpCdt() + otpExpireTime))
            throw new NotAvailableException(locale.getString("codeExpired"));
        else {
            user.setOtpStatus(1);
            this.save(user);
            authRequest.setUsername(user.getId().toString());
            return this.generateToken(authRequest);
        }
    }

    public HttpResponse prepareOAuth(AuthRequest authRequest) {
        if (authRequest.getGrantType().equals("password")) {
            return getOAuthTokenOtp(authRequest);
        } else if (authRequest.getGrantType().equals("refresh_token")) {
            return refreshToken(authRequest);
        }
        return null;
    }
}