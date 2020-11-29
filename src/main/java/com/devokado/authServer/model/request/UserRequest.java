package com.devokado.authServer.model.request;

import com.devokado.authServer.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotNull
    @Size(min = 11, max = 11)
    private String mobile;

    @Email
    private String email;

    @NotNull
    @Size(min = 4)
    private String password;

    private String firstname;
    private String lastname;
    private Boolean active = true;

    public static User createUser(UserRequest registerRequest) {
        User user = new User();
        user.setMobile(registerRequest.getMobile());
        user.setPassword(new BCryptPasswordEncoder().encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstname(registerRequest.getFirstname());
        user.setLastname(registerRequest.getLastname());
        user.setActive(registerRequest.getActive());
        return user;
    }
}
