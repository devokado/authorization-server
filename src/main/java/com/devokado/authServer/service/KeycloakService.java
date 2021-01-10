package com.devokado.authServer.service;

import com.devokado.authServer.model.request.AuthRequest;
import com.devokado.authServer.util.HttpHelper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONObject;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private static final Logger logger = Logger.getLogger(KeycloakService.class);

    private Keycloak initialKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username(adminUsername)
                .password(adminPassword)
                .clientId("admin-cli")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
    }

    private RealmResource getKeycloakRealmResource() {
        Keycloak keycloak = initialKeycloak();
        return keycloak.realm(realm);
    }

    private UsersResource getKeycloakUserResource() {
        return getKeycloakRealmResource().users();
    }

    private CredentialRepresentation createCredentialRepresentation(String value) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(value.trim());
        return credentialRepresentation;
    }

    private void assignRealmRole(String roleName, String userId) {
        RealmResource realmResource = getKeycloakRealmResource();
        RoleRepresentation savedRoleRepresentation = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(savedRoleRepresentation));
    }

    private HttpResponse adminAuthorization() {
        HttpResponse response = null;
        try {
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("grant_type", "password"));
            urlParameters.add(new BasicNameValuePair("client_id", "admin-cli"));
            urlParameters.add(new BasicNameValuePair("username", adminUsername));
            urlParameters.add(new BasicNameValuePair("password", adminPassword));

            response = HttpHelper.post(serverUrl + "/realms/master/protocol/openid-connect/token", urlParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response createKeycloakUser(String roleName, long username, String password) {
        UsersResource userResource = getKeycloakUserResource();
        UserRepresentation user = new UserRepresentation();
        user.setUsername(Long.toString(username));
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(createCredentialRepresentation(password)));
        Response result = userResource.create(user);

        if (result.getStatus() == 201) {
            String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            assignRealmRole(roleName, userId);
        }

        return result;
    }

    public String getKuuidFromResponse(Response response) {
        return response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    }

    public HttpResponse generateToken(AuthRequest authRequest) {
        HttpResponse response = null;
        try {
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("grant_type", authRequest.getGrantType()));
            urlParameters.add(new BasicNameValuePair("client_id", authRequest.getClientId()));
            urlParameters.add(new BasicNameValuePair("username", authRequest.getUsername()));
            urlParameters.add(new BasicNameValuePair("password", authRequest.getPassword()));
            urlParameters.add(new BasicNameValuePair("client_secret", authRequest.getClientSecret()));
            response = HttpHelper.post(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token", urlParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public HttpResponse refreshToken(AuthRequest authRequest) {
        HttpResponse response = null;
        try {
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("refresh_token", authRequest.getRefreshToken()));
            urlParameters.add(new BasicNameValuePair("grant_type", authRequest.getGrantType()));
            urlParameters.add(new BasicNameValuePair("client_id", authRequest.getClientId()));
            urlParameters.add(new BasicNameValuePair("client_secret", authRequest.getClientSecret()));
            response = HttpHelper.post(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token", urlParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public void logoutKeycloakUser(String token) {
        getKeycloakUserResource().get(token).logout();
    }

    public HttpResponse updateKeycloakUserPassword(String userId, String newPassword) {
        HttpResponse response = null;
        try {
            HttpResponse adminResponse = adminAuthorization();
            logger.error("admin token");
            if (adminResponse.getStatusLine().getStatusCode() == 200) {
                logger.error("admin token 200");
                String jsonBody = EntityUtils.toString(adminResponse.getEntity());
                JSONObject json = new JSONObject(jsonBody);
                logger.error(json.get("access_token").toString());
                logger.error(String.format(serverUrl + "/admin/realms/" + realm + "/users/%s/reset-password", userId));
                response = HttpHelper.put(
                        String.format(serverUrl + "/admin/realms/" + realm + "/users/%s/reset-password", userId),
                        "{\"temporary\":false,\"type\":\"password\",\"value\":" + newPassword + "}",
                        Map.of("Authorization", "Bearer " + json.get("access_token").toString(),
                                "Content-Type", "application/json"));
            } else response = adminResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
