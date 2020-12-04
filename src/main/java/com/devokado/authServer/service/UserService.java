package com.devokado.authServer.service;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.repository.UserRepository;
import com.devokado.authServer.util.LocaleHelper;
import com.devokado.authServer.util.Validate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.models.SendResult;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
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

    public List<User> listAll() {
        return repository.findAll();
    }

    public Page<User> listAllPagination(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<User> listAllSorting(Sort sort) {
        return repository.findAll(sort);
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

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public User partialUpdate(UserPatchRequest userPatchRequest, String userId) {
        User user = this.getWithKuuid(userId);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> changes = mapper.convertValue(userPatchRequest, new TypeReference<>() {});

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
        User user = this.getWithKuuid(userId);
        User updatedUser = updateRequest.create(user);
        return this.save(updatedUser);
    }

    public User updateWithKuuid(User user, String uuid) {
        User existUser = repository.findByKuuid(uuid).orElse(null);
        if (existUser != null) {
            if (user.getActive() != null)
                existUser.setActive(user.getActive());
            if (!StringUtils.isEmpty(existUser.getEmail()))
                existUser.setEmail(user.getEmail());

            if (!StringUtils.isEmpty(existUser.getFirstname()))
                existUser.setFirstname(user.getFirstname());

            if (!StringUtils.isEmpty(existUser.getLastname()))
                existUser.setLastname(user.getLastname());

            if (!StringUtils.isEmpty(existUser.getPassword()))
                existUser.setPassword(user.getPassword());

            if (!StringUtils.isEmpty(existUser.getOtp()))
                existUser.setOtp(user.getOtp());

            return repository.save(existUser);
        }
        return null;
    }

    public String createSMSCode() {
        if (otpCodeSize < 1) {
            throw new RuntimeException("Number of digits must be bigger than 0");
        }
        double maxValue = Math.pow(10.0, otpCodeSize); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long) (r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    public SendResult sendSMS(String code, String mobile) {
        KavenegarApi api = new KavenegarApi(apiKey);
        return api.verifyLookup(mobile, code, template);
    }

    public int createUser(UserRequest userRequest) {
        javax.ws.rs.core.Response response = this.createKeycloakUser(userRequest);
        int responseStatus = response.getStatus();
        if (responseStatus == 201) {
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            if (userRequest != null) {
                User model = UserRequest.createUser(userRequest);
                model.setKuuid(userId);
                this.save(model);
            }
            return 201;
        } else {
            return responseStatus;
        }
    }

    public String changePassword(String userId, ResetPasswordRequest resetPasswordModel) {
        User userModel = this.getWithKuuid(userId);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (bCryptPasswordEncoder.matches(resetPasswordModel.getOldPassword(), userModel.getPassword())) {
            if (resetPasswordModel.getNewPassword().equals(resetPasswordModel.getConfirmation())) {
                userModel.setPassword(bCryptPasswordEncoder.encode(resetPasswordModel.getNewPassword()));
                this.save(userModel);
                this.updateKeycloakUserPassword(userId, resetPasswordModel.getNewPassword());
                return locale.getString("updatePasswordSuccess");
            } else
                return locale.getString("passwordNotMatch");
        } else return locale.getString("passwordIncorrect");
    }

    public String getUserIdWithToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }

    public AccessToken getAccessToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken();
    }

    public String sendVerificationSMS(String userId, OtpRequest otpRequest) {
        if (Validate.isValidMobile(otpRequest.getMobile())) {
            User user = this.getWithKuuid(userId);
            if (!user.getMobileVerified()) {
                String code = this.createSMSCode();
                long expireTime = otpExpireTime + System.currentTimeMillis();
                user.setOtp(code + "_" + expireTime + "_" + 0);
                this.save(user);
                SendResult result = this.sendSMS(code, otpRequest.getMobile());
                return locale.getString("codeSent");
            } else return locale.getString("verificated");
        } else
            return locale.getString("invalidMobile");
    }

    public String verifyMobile(OtpVerificationRequest otpVerificationModel) {
        User user = this.getWithMobile(otpVerificationModel.getMobile());
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
                this.save(user);
                return locale.getString("verificationIsOk");
            }

        } else return locale.getString("usernameNotFound");
    }
}
