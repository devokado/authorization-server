package com.devokado.authServer.service;

import com.devokado.authServer.exceptions.DuplicateException;
import com.devokado.authServer.exceptions.NotFoundException;
import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.UserPatchRequest;
import com.devokado.authServer.model.request.UserUpdateRequest;
import com.devokado.authServer.repository.UserRepository;
import com.devokado.authServer.util.LocaleHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService extends KeycloakService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public LocaleHelper locale;

    public Iterable<User> list() {
        return userRepository.findAll();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    public User get(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent())
            return user;
        else throw new NotFoundException(locale.getString("userNotFound"));
    }

    public Optional<User> findByMobile(String mobile) {
        Optional<User> user = userRepository.findByMobile(mobile);
        if (user.isPresent())
            return user;
        else throw new NotFoundException(locale.getString("userNotFound"));
    }

    public User save(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateException(locale.getString("duplicateUsername"));
        }
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

    public String extractUserIdFromToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }

    public AccessToken extractAccessToken(HttpServletRequest request) {
        KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) request.getUserPrincipal();
        return principal.getAccount().getKeycloakSecurityContext().getToken();
    }
}
