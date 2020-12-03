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
public class UserUpdateRequest {
    @Email
    @NotNull
    private String email;
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    @NotNull
    private Boolean active = true;

    public User create(User user) {
        user.setEmail(this.getEmail());
        user.setFirstname(this.getFirstname());
        user.setLastname(this.getLastname());
        user.setActive(this.getActive());
        return user;
    }
}
