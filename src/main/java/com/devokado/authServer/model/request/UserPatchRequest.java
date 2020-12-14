package com.devokado.authServer.model.request;

import com.devokado.authServer.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPatchRequest {
    @Email
    private String email;
    private String firstname;
    private String lastname;

    public User create(User user) {
        user.setEmail(this.getEmail());
        user.setFirstname(this.getFirstname());
        user.setLastname(this.getLastname());
        return user;
    }
}
