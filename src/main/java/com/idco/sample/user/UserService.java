package com.idco.sample.user;

import com.idco.sample.model.response.StatusResponse;
import com.idco.sample.user.request.LoginRequest;
import com.idco.sample.user.request.RegisterRequest;
import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.models.SendResult;
import lombok.val;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class UserService {

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${otp.security.email-verified}")
    private boolean emailVerified;

    @Value("${otp.code.size}")
    private int otpCodeSize;

    @Value("${otp.kavenegar.apikey}")
    private String apiKey;

    @Value("${otp.kavenegar.template}")
    private String template;

    private Keycloak initialKeycloak() {
        return KeycloakBuilder.builder().serverUrl(serverUrl).realm("master").username("admin").password("admin")
                .clientId("admin-cli").resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
    }

    private RealmResource getRealmResource() {
        val keycloak = initialKeycloak();
        return keycloak.realm(realm);
    }

    private UsersResource getKeycloakUserResource() {
        return getRealmResource().users();
    }

    public StatusResponse createUserInKeycloak(RegisterRequest model) {
        int statusId;

        try {
            UsersResource userResource = getKeycloakUserResource();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(model.getUsername());
            user.setEmail(model.getEmail());
            user.setFirstName(model.getFirstname());
            user.setLastName(model.getLastname());
            user.setAttributes(Map.of("mobile", List.of(model.getMobile())));
            user.setEmailVerified(emailVerified);
            user.setEnabled(true);
            // Create user
            Response result = userResource.create(user);
            System.out.println("Keycloak create user response code>>>>" + result.getStatus());

            statusId = result.getStatus();

            if (statusId == 201) {

                String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                System.out.println("User created with userId:" + userId);

                // Define password credential
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(model.getPassword());

                // Set password credential
                userResource.get(userId).resetPassword(passwordCred);

                // set role
                RealmResource realmResource = getRealmResource();
                RoleRepresentation savedRoleRepresentation = realmResource.roles().get("app-user").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(savedRoleRepresentation));
                return new StatusResponse(statusId, "created in keycloak successfully");

            } else if (statusId == 409) {
                return new StatusResponse(statusId, "Username: " + model.getUsername() + " already present in keycloak");
            } else {
                return new StatusResponse(statusId, "Username: " + model.getUsername() + " could not be created in keycloak");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getToken(LoginRequest loginRequest) {
        String response = null;
        try {

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("grant_type", "password"));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("username", loginRequest.getUsername()));
            urlParameters.add(new BasicNameValuePair("password", loginRequest.getPassword()));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));

            response = sendPost(urlParameters);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public String getRefreshToken(String refreshToken) {
        String response = null;
        try {

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));

            response = sendPost(urlParameters);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String sendPost(List<NameValuePair> urlParameters) throws Exception {

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token");
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public void logoutUser(String userId) {
        getKeycloakUserResource().get(userId).logout();
    }

    public void resetPassword(String userId, String newPassword) {
        UsersResource userResource = getKeycloakUserResource();

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword.trim());

        // Set password credential
        userResource.get(userId).resetPassword(passwordCred);
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

    public boolean isUserExist(String mobile) {
        List<UserRepresentation> users = getRealmResource().users().list();
        for (UserRepresentation user : users) {
//            String userMobile = String.valueOf(user.getAttributes().get("mobile"));
            return user.getUsername().equals(mobile);
        }
        return false;
    }

    public String getUserId(String username) {
        List<UserRepresentation> users = getRealmResource().users().list();
        for (UserRepresentation user : users) {
            if (user.getUsername().equals(username)) {
                return user.getId();
            }
        }
        return null;
    }

    public void updateUserInKeycloak(String userId, RegisterRequest model) {
        UserRepresentation userRepresentation = new UserRepresentation();
        if (!model.getFirstname().isEmpty())
            userRepresentation.setFirstName(model.getFirstname());
        if (!model.getLastname().isEmpty())
            userRepresentation.setLastName(model.getLastname());
        if (!model.getEmail().isEmpty())
            userRepresentation.setEmail(model.getEmail());
        UserResource userResource = getKeycloakUserResource().get(userId);
        if (userResource != null)
            userResource.update(userRepresentation);
//        userRepresentation.setAttributes(Map.of("mobile", List.of(model.getMobile())));
    }

    public AccessToken tokenParser(String token) {
        try {
            String[] jwt = token.split(" ");
            return TokenVerifier.create(jwt[1], AccessToken.class).getToken();
        } catch (VerificationException e) {
            e.printStackTrace();
        }
        return null;
    }


}
