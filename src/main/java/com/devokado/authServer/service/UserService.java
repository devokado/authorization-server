package com.devokado.authServer.service;

import com.devokado.authServer.controller.UserController;
import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.repository.UserRepository;
import com.devokado.authServer.util.LocaleHelper;
import com.devokado.authServer.util.Validate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.models.SendResult;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class UserService extends KeycloakService {

    @Value("${otp.code.size}")
    private int otpCodeSize;

    @Value("${otp.kavenegar.apikey}")
    private String apiKey;

    @Value("${otp.kavenegar.template}")
    private String template;

    @Value("${otp.code.expiretime}")
    private long otpExpireTime;

    @Autowired
    private UserRepository repository;

    @Autowired
    private LocaleHelper locale;

    private static final Logger logger = Logger.getLogger(UserController.class);

    public List<User> listAll() {
        return repository.findAll();
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public User getWithKuuid(String uuid) {
        return repository.findByKuuid(uuid).orElse(null);
    }

    public User getWithMobile(String mobile) {
        return repository.findByMobile(mobile).orElse(null);
    }

    public User partialUpdate(UserPatchRequest userPatchRequest, String userId) {
        User user = getWithKuuid(userId);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> changes = mapper.convertValue(userPatchRequest, new TypeReference<>() {
        });

        changes.forEach((k, v) -> {
            Field field = ReflectionUtils.findField(User.class, k);
            if (field != null) {
                if (v != null) {
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, user, v);
                }
            }
        });

        return this.save(user);
    }

    public User update(UserUpdateRequest updateRequest, String userId) {
        User user = getWithKuuid(userId);
        User updatedUser = updateRequest.create(user);
        return save(updatedUser);
    }

    public String createSMSCode() {
        return String.valueOf(otpCodeSize < 1 ? 0 : new Random()
                .nextInt((9 * (int) Math.pow(10, otpCodeSize - 1)) - 1)
                + (int) Math.pow(10, otpCodeSize - 1));
    }

    public SendResult sendSMS(String code, String mobile) {
        KavenegarApi api = new KavenegarApi(apiKey);
        return api.verifyLookup(mobile, code, template);
    }

    public int createUser(UserRequest userRequest) {
        Response response = createKeycloakUser(userRequest);
        int responseStatus = response.getStatus();
        if (responseStatus == 201) {
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            try {
                User model = UserRequest.createUser(userRequest);
                model.setKuuid(userId);
                this.save(model);
                return 201;
            } catch (DataIntegrityViolationException e) {
                return 409;
            }
        } else {
            return responseStatus;
        }
    }

    public String changePassword(String userId, ResetPasswordRequest resetPasswordModel) {
        try {
            User userModel = getWithKuuid(userId);
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (bCryptPasswordEncoder.matches(resetPasswordModel.getOldPassword(), userModel.getPassword())) {
                if (resetPasswordModel.getNewPassword().equals(resetPasswordModel.getConfirmation())) {
                    HttpResponse response = this.updateKeycloakUserPassword(userId, resetPasswordModel.getNewPassword());
                    if (response.getStatusLine().getStatusCode() == 204) {
                        logger.error("change pass ok");
                        userModel.setPassword(bCryptPasswordEncoder.encode(resetPasswordModel.getNewPassword()));
                        this.save(userModel);
                        return locale.getString("updatePasswordSuccess");
                    } else return EntityUtils.toString(response.getEntity());
                } else return locale.getString("passwordNotMatch");
            } else return locale.getString("passwordIncorrect");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserIdWithToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }

    public String sendVerificationSMS(String userId, OtpRequest otpRequest) {
        if (Validate.isValidMobile(otpRequest.getMobile())) {
            User user = getWithKuuid(userId);
            if (!user.getMobileVerified()) {
                String code = this.createSMSCode();
                long expireTime = otpExpireTime + System.currentTimeMillis();
                user.setOtp(code + "_" + expireTime + "_" + 0);
                this.save(user);
                SendResult result = this.sendSMS(code, otpRequest.getMobile());
                if (result.getStatus() == 5)
                    return locale.getString("codeSent");
                else return locale.getString("failedSendSms");
            } else return locale.getString("verificated");
        } else
            return locale.getString("invalidMobile");
    }

    public String verifyMobile(OtpVerificationRequest otpVerificationModel) {
        User user = getWithMobile(otpVerificationModel.getMobile());
        if (user != null) {
            String[] otp = user.getOtp().split("_");
            String otpCode = otp[0];
            long otpExpireTime = Long.parseLong(otp[1]);
            int codeStatus = Integer.parseInt(otp[2]);

            if (!otpVerificationModel.getCode().equals(otpCode))
                return locale.getString("codeNotValid");
            else if (codeStatus == 1)
                return locale.getString("codeExpired");
            else if (System.currentTimeMillis() > otpExpireTime)
                return locale.getString("codeExpired");
            else {
                user.setMobileVerified(true);
                user.setOtp(otpCode + "_" + otpExpireTime + "_" + 1);
                save(user);
                return locale.getString("verificationIsOk");
            }

        } else return locale.getString("usernameNotFound");
    }
}
