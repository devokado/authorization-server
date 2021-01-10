package com.devokado.authServer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    private String username;

    private String password;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @NotNull
    @JsonProperty("grant_type")
    private String grantType;

    @NotNull
    @JsonProperty("client_id")
    private String clientId;

    @NotNull
    @JsonProperty("client_secret")
    private String clientSecret;
}