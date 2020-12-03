package com.devokado.authServer.service;

import com.devokado.authServer.model.request.LoginRequest;
import com.devokado.authServer.model.request.RefreshTokenRequest;
import com.devokado.authServer.model.request.UserRequest;
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
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class KeycloakService {

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private Keycloak initialKeycloak() {
        return KeycloakBuilder.builder().serverUrl(serverUrl).realm("master").username("admin").password("admin")
                .clientId("admin-cli").resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
    }

    private RealmResource getKeycloakRealmResource() {
        val keycloak = initialKeycloak();
        return keycloak.realm(realm);
    }

    private UsersResource getKeycloakUserResource() {
        return getKeycloakRealmResource().users();
    }

    public javax.ws.rs.core.Response createKeycloakUser(UserRequest model) {
        int statusId;

        try {
            UsersResource userResource = getKeycloakUserResource();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(model.getMobile());
            user.setEmail(model.getEmail());
            user.setEnabled(model.getActive());
            javax.ws.rs.core.Response result = userResource.create(user);

            statusId = result.getStatus();

            if (statusId == 201) {
                String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                // Define password credential
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(model.getPassword());

                // Set password credential
                userResource.get(userId).resetPassword(passwordCred);

                // set role
                RealmResource realmResource = getKeycloakRealmResource();
                RoleRepresentation savedRoleRepresentation = realmResource.roles().get("app-user").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(savedRoleRepresentation));
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createToken(LoginRequest loginRequest) {
        String response = null;
        try {
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("grant_type", loginRequest.getGrantType()));
            urlParameters.add(new BasicNameValuePair("client_id", loginRequest.getClientId()));
            urlParameters.add(new BasicNameValuePair("username", loginRequest.getUsername()));
            urlParameters.add(new BasicNameValuePair("password", loginRequest.getPassword()));
            urlParameters.add(new BasicNameValuePair("client_secret", loginRequest.getClientSecret()));

            response = sendPost(urlParameters);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public String getRefreshToken(RefreshTokenRequest refreshTokenRequest) {
        String response = null;
        try {

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("refresh_token", refreshTokenRequest.getRefreshToken()));
            urlParameters.add(new BasicNameValuePair("grant_type", refreshTokenRequest.getGrantType()));
            urlParameters.add(new BasicNameValuePair("client_id", refreshTokenRequest.getClientId()));
            urlParameters.add(new BasicNameValuePair("client_secret", refreshTokenRequest.getClientSecret()));

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

    public void logoutKeycloakUser(String token) {
        getKeycloakUserResource().get(token).logout();
    }

    public void updateKeycloakUserPassword(String userId, String newPassword) {
        UsersResource userResource = getKeycloakUserResource();

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword.trim());

        // Set password credential
        userResource.get(userId).resetPassword(passwordCred);
    }

    public void sendVerifyEmail(String userId) {
        getKeycloakUserResource().get(userId).sendVerifyEmail("auth-server");
    }

    public boolean isKeycloakUserExist(String mobile) {
        List<UserRepresentation> users = getKeycloakRealmResource().users().list();
        for (UserRepresentation user : users) {
//            String userMobile = String.valueOf(user.getAttributes().get("mobile"));
            return user.getUsername().equals(mobile);
        }
        return false;
    }

    public String getKeycloakUserId(String username) {
        List<UserRepresentation> users = getKeycloakRealmResource().users().list();
        for (UserRepresentation user : users) {
            if (user.getUsername().equals(username)) {
                return user.getId();
            }
        }
        return null;
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
