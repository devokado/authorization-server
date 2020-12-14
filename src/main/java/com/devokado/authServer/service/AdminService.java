package com.devokado.authServer.service;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.UserPatchRequest;
import com.devokado.authServer.model.request.UserUpdateRequest;
import com.devokado.authServer.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Map;

@Service
public class AdminService extends KeycloakService {

    @Autowired
    private UserRepository repository;

    public User save(User user) {
        return repository.save(user);
    }

    public User get(Long id) {
        return repository.findById(id).orElse(null);
    }

//    public int createUser(UserRequest userRequest) {
//        Response response = createKeycloakUser(userRequest);
//        int responseStatus = response.getStatus();
//        if (responseStatus == 201) {
//            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
//            try {
//                User model = UserRequest.createUser(userRequest);
//                model.setKuuid(userId);
//                this.save(model);
//                return 201;
//            } catch (DataIntegrityViolationException e) {
//                return 409;
//            }
//        } else {
//            return responseStatus;
//        }
//    }

//    public String changePassword(String userId, ResetPasswordRequest resetPasswordModel) {
//        try {
//            User userModel = getWithKuuid(userId);
//            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//            if (bCryptPasswordEncoder.matches(resetPasswordModel.getOldPassword(), userModel.getPassword())) {
//                if (resetPasswordModel.getNewPassword().equals(resetPasswordModel.getConfirmation())) {
//                    HttpResponse response = this.updateKeycloakUserPassword(userId, resetPasswordModel.getNewPassword());
//                    if (response.getStatusLine().getStatusCode() == 204) {
//                        logger.error("change pass ok");
//                        userModel.setPassword(bCryptPasswordEncoder.encode(resetPasswordModel.getNewPassword()));
//                        this.save(userModel);
//                        return locale.getString("updatePasswordSuccess");
//                    } else return EntityUtils.toString(response.getEntity());
//                } else return locale.getString("passwordNotMatch");
//            } else return locale.getString("passwordIncorrect");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public String getUserIdWithToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }

//    public String sendVerificationSMS(String userId, OtpRequest otpRequest) {
//        if (Validate.isValidMobile(otpRequest.getUsername())) {
//            User user = getWithKuuid(userId);
//            if (!user.getMobileVerified()) {
//                String code = this.createSMSCode();
//                long expireTime = otpExpireTime + System.currentTimeMillis();
//                user.setOtp(code + "_" + expireTime + "_" + 0);
//                this.save(user);
//                SendResult result = this.sendSMS(code, otpRequest.getUsername());
//                if (result.getStatus() == 5)
//                    return locale.getString("codeSent");
//                else return locale.getString("failedSendSms");
//            } else return locale.getString("verificated");
//        } else
//            return locale.getString("invalidMobile");
//    }
//
//    public String verifyMobile(OtpVerificationRequest otpVerificationModel) {
//        User user = getWithMobile(otpVerificationModel.getMobile());
//        if (user != null) {
//            String[] otp = user.getOtp().split("_");
//            String otpCode = otp[0];
//            long otpExpireTime = Long.parseLong(otp[1]);
//            int codeStatus = Integer.parseInt(otp[2]);
//
//            if (!otpVerificationModel.getCode().equals(otpCode))
//                return locale.getString("codeNotValid");
//            else if (codeStatus == 1)
//                return locale.getString("codeExpired");
//            else if (System.currentTimeMillis() > otpExpireTime)
//                return locale.getString("codeExpired");
//            else {
//                user.setMobileVerified(true);
//                user.setOtp(otpCode + "_" + otpExpireTime + "_" + 1);
//                save(user);
//                return locale.getString("verificationIsOk");
//            }
//
//        } else return locale.getString("usernameNotFound");
//    }
}
