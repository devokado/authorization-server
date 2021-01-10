package com.devokado.authServer.service;

import com.devokado.authServer.exceptions.BadRequestException;
import com.devokado.authServer.exceptions.CustomException;
import com.devokado.authServer.exceptions.NotFoundException;
import com.devokado.authServer.exceptions.RestException;
import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.AuthRequest;
import com.devokado.authServer.model.request.RegisterRequest;
import com.devokado.authServer.model.request.ResetPasswordRequest;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

@Service
public class AdminService extends UserService {

    public void registerUser(RegisterRequest registerRequest) {
        User createdUser = this.save(registerRequest.asUser());
        Response response = this.createKeycloakUser("app-admin", createdUser.getId(), registerRequest.getPassword());
        if (response.getStatus() == 201) {
            createdUser.setKuuid(this.getKuuidFromResponse(response));
            this.save(createdUser);
        } else {
            this.deleteById(createdUser.getId());
            throw new RestException(locale.getString("failedToCreateUser"), HttpStatus.valueOf(response.getStatus()));
        }
    }

    public HttpResponse login(AuthRequest authRequest) {
        Optional<User> user = this.findByEmail(authRequest.getUsername());
        if (user.isPresent()) {
            authRequest.setUsername(user.get().getId().toString());
            return this.generateToken(authRequest);
        } else throw new NotFoundException(locale.getString("userNotFound"));
    }

    public void changePassword(String userId, ResetPasswordRequest resetPasswordModel) {
        try {
            if (resetPasswordModel.getNewPassword().equals(resetPasswordModel.getConfirmation())) {
                HttpResponse response = this.updateKeycloakUserPassword(userId, resetPasswordModel.getNewPassword());
                if (response.getStatusLine().getStatusCode() != 204) {
                    throw new CustomException(EntityUtils.toString(response.getEntity()), response.getStatusLine().getStatusCode());
                }
            } else throw new BadRequestException(locale.getString("passwordNotMatch"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse prepareOAuth(AuthRequest authRequest) {
        if (authRequest.getGrantType().equals("password")) {
            return login(authRequest);
        } else if (authRequest.getGrantType().equals("refresh_token")) {
            return refreshToken(authRequest);
        }
        return null;
    }
}
