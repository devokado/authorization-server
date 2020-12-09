package com.devokado.authServer.service;

import com.devokado.authServer.model.User;
import com.devokado.authServer.model.request.*;
import com.devokado.authServer.repository.UserRepository;
import com.devokado.authServer.util.LocaleHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private LocaleHelper locale;

    @Test
    @DisplayName("Get all users")
    public void testGetUsers() {
        User mockUser1 = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        User mockUser2 = new User(2L, java.util.UUID.randomUUID().toString(), "09123456788", "1234",
                "test2@gmail.com", "User2 firstname", "User2 lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Arrays.asList(mockUser1, mockUser2)).when(userRepository).findAll();

        List<User> allUsers = userService.listAll();

        Assertions.assertEquals(2, allUsers.size());
        Assertions.assertEquals(mockUser1.getId(), allUsers.get(0).getId());
        Assertions.assertEquals(mockUser2.getId(), allUsers.get(1).getId());
    }

    @Test
    @DisplayName("Get specific user with id")
    public void testGetSpecificUser() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findById(mockUser.getId());

        User foundUser = userService.get(mockUser.getId());

        Assertions.assertNotNull(mockUser);
        Assertions.assertSame(mockUser.getMobile(), foundUser.getMobile());
        Assertions.assertEquals(mockUser.getFirstname(), foundUser.getFirstname());

    }

    @Test
    @DisplayName("Get specific user with kuuid")
    public void testGetSpecificUserWithKuuid() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        User foundUser = userService.getWithKuuid(mockUser.getKuuid());

        Assertions.assertNotNull(foundUser);
        Assertions.assertSame(mockUser.getMobile(), foundUser.getMobile());
        Assertions.assertEquals(mockUser.getFirstname(), foundUser.getFirstname());
    }

    @Test
    @DisplayName("Get specific user with mobile")
    public void testGetSpecificUserWithMobile() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile(mockUser.getMobile());
        User foundUser = userService.getWithMobile(mockUser.getMobile());

        Assertions.assertNotNull(foundUser);
        Assertions.assertSame(mockUser.getMobile(), mockUser.getMobile());
        Assertions.assertEquals(mockUser.getFirstname(), mockUser.getFirstname());
    }

    @Test
    @DisplayName("save user in our db")
    public void testSaveUser() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(mockUser).when(userRepository).save(any());

        User savedPost = userService.save(mockUser);

        Assertions.assertNotNull(savedPost);
        Assertions.assertSame(mockUser.getMobile(), savedPost.getMobile());
        Assertions.assertSame(mockUser.getEmail(), savedPost.getEmail());
    }

    @Test
    @DisplayName("Update an existing user")
    public void testUpdateUser() {
        User existingUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        UserUpdateRequest newUser = new UserUpdateRequest("newtest@gmail.com", "User new firstname", "User new lastname", true);

        doReturn(Optional.of(existingUser)).when(userRepository).findByKuuid(existingUser.getKuuid());
        doReturn(newUser.create(existingUser)).when(userRepository).save(existingUser);

        User updatePost = userService.update(newUser, existingUser.getKuuid());

        Assertions.assertEquals(newUser.getEmail(), updatePost.getEmail());
        Assertions.assertEquals(newUser.getFirstname(), updatePost.getFirstname());
        Assertions.assertEquals(newUser.getLastname(), updatePost.getLastname());
        Assertions.assertEquals(newUser.getActive(), updatePost.getActive());
    }

    @Test
    @DisplayName("Partial update an existing user")
    public void testPartialUpdateUser() {
        User existingUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        UserPatchRequest newUser = new UserPatchRequest("newtest@gmail.com", null, null, null);

        doReturn(Optional.of(existingUser)).when(userRepository).findByKuuid(existingUser.getKuuid());
        doReturn(newUser.create(existingUser)).when(userRepository).save(existingUser);

        User updatePost = userService.partialUpdate(newUser, existingUser.getKuuid());

        Assertions.assertEquals(newUser.getEmail(), updatePost.getEmail());
    }

    //todo mock keycloak and kavenegar
    @Test
    @DisplayName("Change password success")
    public void testChangePassword() {
//        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", new BCryptPasswordEncoder().encode("1234"),
//                "test@gmail.com", "User firstname", "User lastname",
//                true, false, "", LocalDateTime.now(), LocalDateTime.now());
//
//        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());
//
//        String message = userService.changePassword(mockUser.getKuuid(), new ResetPasswordRequest(
//                "1234", "12345", "12345"));
//
//        Assertions.assertEquals(locale.getString("updatePasswordSuccess"), message);
    }

    @Test
    @DisplayName("Change password not match")
    public void testChangePasswordNotMatch() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", new BCryptPasswordEncoder().encode("1234"),
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        String message = userService.changePassword(mockUser.getKuuid(), new ResetPasswordRequest(
                "1234", "12345", "123456"));

        Assertions.assertEquals(locale.getString("passwordNotMatch"), message);
    }

    @Test
    @DisplayName("Change password incorrect")
    public void testChangePasswordIncorrect() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", new BCryptPasswordEncoder().encode("1234"),
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        String message = userService.changePassword(mockUser.getKuuid(), new ResetPasswordRequest(
                "12345", "12345", "12345"));

        Assertions.assertEquals(locale.getString("passwordIncorrect"), message);
    }

    @Test
    @DisplayName("create sms code")
    public void testCreateSmsCode() {
        String code = userService.createSMSCode();
        Assertions.assertEquals(6, code.length());
    }

    @Test
    @DisplayName("send sms")
    public void testSendSms() {
//        SendResult result = userService.sendSMS("123456","09137911396");
//        Assertions.assertEquals(200, result.get);
    }

    @Test
    @DisplayName("send verification sms success")
    public void testSendVerificationSms() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        String message = userService.sendVerificationSMS(mockUser.getKuuid(), new OtpRequest(mockUser.getMobile()));

        Assertions.assertEquals(locale.getString("codeSent"), message);
    }

    @Test
    @DisplayName("mobile is verificated")
    public void testSendVerificationSmsIsVerificated() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, true, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        String message = userService.sendVerificationSMS(mockUser.getKuuid(), new OtpRequest(mockUser.getMobile()));

        Assertions.assertEquals(locale.getString("verificated"), message);
    }

    @Test
    @DisplayName("invalid mobile number")
    public void testSendVerificationSmsIsInvalidMobile() {
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "12123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false, "", LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByKuuid(mockUser.getKuuid());

        String message = userService.sendVerificationSMS(mockUser.getKuuid(), new OtpRequest(mockUser.getMobile()));

        Assertions.assertEquals(locale.getString("invalidMobile"), message);
    }

    @Test
    @DisplayName("Verify mobile success")
    public void testVerifyMobile() {
        long time = System.currentTimeMillis() + 90000;
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false,
                "123456_" + time + "_0",
                LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile(mockUser.getMobile());

        String message = userService.verifyMobile(new OtpVerificationRequest(mockUser.getMobile(), "123456"));

        Assertions.assertEquals(locale.getString("verificationIsOk"), message);
    }

    @Test
    @DisplayName("Verify mobile invalid code")
    public void testVerifyMobileInvalidCode() {
        long time = System.currentTimeMillis() + 90000;
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false,
                "123456_" + time + "_0",
                LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile(mockUser.getMobile());

        String message = userService.verifyMobile(new OtpVerificationRequest(mockUser.getMobile(), "111111"));

        Assertions.assertEquals(locale.getString("codeNotValid"), message);
    }

    @Test
    @DisplayName("Verify mobile , User not found")
    public void testVerifyMobileUserNotFound() {
        long time = System.currentTimeMillis() + 90000;
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false,
                "123456_" + time + "_0",
                LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile("09123456799");

        String message = userService.verifyMobile(new OtpVerificationRequest(mockUser.getMobile(), "123456"));

        Assertions.assertEquals(locale.getString("usernameNotFound"), message);
    }

    @Test
    @DisplayName("Verify mobile success")
    public void testVerifyMobileTimeExpired() {
        long time = System.currentTimeMillis();
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false,
                "123456_" + time + "_0",
                LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile(mockUser.getMobile());

        String message = userService.verifyMobile(new OtpVerificationRequest(mockUser.getMobile(), "123456"));

        Assertions.assertEquals(locale.getString("codeExpired"), message);
    }

    @Test
    @DisplayName("Verify mobile with use limited")
    public void testVerifyMobileUseLimited() {
        long time = System.currentTimeMillis() + 90000;
        User mockUser = new User(1L, java.util.UUID.randomUUID().toString(), "09123456789", "1234",
                "test@gmail.com", "User firstname", "User lastname",
                true, false,
                "123456_" + time + "_1",
                LocalDateTime.now(), LocalDateTime.now());

        doReturn(Optional.of(mockUser)).when(userRepository).findByMobile(mockUser.getMobile());

        String message = userService.verifyMobile(new OtpVerificationRequest(mockUser.getMobile(), "123456"));

        Assertions.assertEquals(locale.getString("codeExpired"), message);
    }

    @Test
    @DisplayName("Create user")
    public void testCreateUser() {
//        UserRequest userRequest = new UserRequest("09137911396", "samdh@gmail.com", "1234", "Ali", "Modares", true);
//        Response.ResponseBuilder rs = new Response.ResponseBuilder();
//        Response.created(URI.create(""));
//        doReturn(Response.created(URI.create(""))).when(userService).createKeycloakUser(userRequest);
//        doThrow().when(userService).createKeycloakUser(userRequest);

//        int status = userService.createUser(userRequest);
//
//        Assertions.assertEquals(201, status);
    }
}
