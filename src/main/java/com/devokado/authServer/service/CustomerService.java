package com.devokado.authServer.service;

import com.devokado.authServer.exceptions.CustomException;
import com.devokado.authServer.exceptions.RestException;
import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.LoginRequest;
import com.devokado.authServer.model.request.OtpRequest;
import com.devokado.authServer.util.LocaleHelper;
import com.devokado.authServer.util.StringHelper;
import com.devokado.authServer.util.Validate;
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
import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
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

    @Autowired
    private LocaleHelper locale;

    @Autowired
    private JavaMailSender emailSender;
    
    //todo handle exceptions
    public void delete() {
        userRepository.deleteAll();
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public void otp(OtpRequest otpRequest) {
        String identifier = otpRequest.getUsername();
        if (Validate.isValidMobile(identifier)) {
            otpWithMobile(identifier);
        } else if (Validate.isValidMail(identifier)) {
            otpWithEmail(identifier);
        } else {
            throw new BadRequestException(locale.getString("invalidMobileOrEmail"));
        }
    }

    private void otpWithEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        checkUser(user, email);
    }

    private void otpWithMobile(String mobile) {
        Optional<User> user = userRepository.findByMobile(mobile);
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
        if (Validate.isValidMobile(identifier)) {
            user.setMobile(identifier);
        } else if (Validate.isValidMail(identifier)) {
            user.setEmail(identifier);
        }
        User createdUser = userRepository.save(user);
        String code = StringHelper.generateCode(otpCodeSize);
        Response response = this.createKeycloakUser(createdUser.getId(), code);
        if (response.getStatus() == 201) {
            createdUser.setKuuid(this.getKuuidFromResponse(response));
            userRepository.save(createdUser);
            sendVerificationCode(code, identifier);
        } else {
            userRepository.deleteById(createdUser.getId());
            throw new RestException(locale.getString("failedToCreateUser"), response.getStatus());
        }
    }

    private void provideOtp(User user, String identifier) {
        try {
            String code = StringHelper.generateCode(otpCodeSize);
            HttpResponse response = this.updateKeycloakUserPassword(user.getKuuid(), code);
            if (response.getStatusLine().getStatusCode() == 204) {
                user.setOtpCdt(System.currentTimeMillis());
                user.setOtpStatus(0);
                userRepository.save(user);
                sendVerificationCode(code, identifier);
            } else
                throw new CustomException(EntityUtils.toString(response.getEntity()), response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendVerificationCode(String code, String identifier) {
        if (Validate.isValidMobile(identifier)) {
            this.sendSMS(code, identifier);
        } else if (Validate.isValidMail(identifier)) {
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

    public HttpResponse getOAuthTokenOtp(LoginRequest loginRequest) {
        //todo change requests class name
        String identifier = loginRequest.getUsername();
        if (Validate.isValidMobile(identifier)) {
            Optional<User> user = userRepository.findByMobile(identifier);
            if (user.isPresent()) {
                return provideOAuth(user.get(), loginRequest);
            } else throw new NotFoundException(locale.getString("userNotFound"));
        } else if (Validate.isValidMail(identifier)) {
            Optional<User> user = userRepository.findByEmail(identifier);
            if (user.isPresent())
                return provideOAuth(user.get(), loginRequest);
            else throw new NotFoundException(locale.getString("userNotFound"));
        } else
            throw new BadRequestException(locale.getString("invalidMobileOrEmail"));
    }

    private HttpResponse provideOAuth(User user, LoginRequest loginRequest) {
        if (user.getOtpStatus() == 1)
            throw new HttpClientErrorException(HttpStatus.GONE, locale.getString("codeExpired"));
        else if (System.currentTimeMillis() > (user.getOtpCdt() + otpExpireTime))
            throw new HttpClientErrorException(HttpStatus.GONE, locale.getString("codeExpired"));
        else {
            user.setOtpStatus(1);
            userRepository.save(user);
            loginRequest.setUsername(user.getId().toString());
            return this.createToken(loginRequest);
        }
    }
}