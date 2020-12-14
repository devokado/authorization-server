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
public class UserService extends KeycloakService {

    @Autowired
    public UserRepository userRepository;

    public User get(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User partialUpdate(UserPatchRequest userPatchRequest, long userId) {
        User user = get(userId);

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

        return userRepository.save(user);
    }

    public User update(UserUpdateRequest updateRequest, long userId) {
        User user = get(userId);
        User updatedUser = updateRequest.create(user);
        return userRepository.save(updatedUser);
    }

    public String getUserIdWithToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }
}
