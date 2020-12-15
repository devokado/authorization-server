package com.devokado.authServer.repository;

import com.devokado.authServer.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Alimodares
 * @since 2020-12-08
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final File DATA_JSON = Paths.get("src", "test", "resources", "users.json").toFile();

    @BeforeEach
    public void setup() throws IOException {
        User[] users = new ObjectMapper().readValue(DATA_JSON, User[].class);
        Arrays.stream(users).forEach(userRepository::save);
    }

    @Test
    @DisplayName("Get user with kuuid")
    public void testGetUserWithKuuid() {
        Optional<User> user = userRepository.findByKuuid("865d7c3c-7dac-41d6-a811-69f6f98d9858");

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.get().getMobile(), "09123456789");
    }

    @Test
    @DisplayName("Get user with mobile")
    public void testGetUserWithMobile() {
        Optional<User> user = userRepository.findByMobile("09123456789");

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.get().getFirstname(), "Lionel");
        Assertions.assertEquals(user.get().getLastname(), "Messi");
    }

    @Test
    @DisplayName("Get user with email")
    public void testGetUserWithEmail() {
        Optional<User> user = userRepository.findByEmail("messi@gmail.com");

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.get().getFirstname(), "Lionel");
        Assertions.assertEquals(user.get().getLastname(), "Messi");
    }

    @Test
    @DisplayName("Test user saved successfully")
    public void testUserSavedSuccessfully() {
        User newUser = new User("09123456788", "test@gmail.com", "User firstname", "User lastname");

        User user = userRepository.save(newUser);

        Assertions.assertNotNull(user, "User should be saved");
        Assertions.assertEquals(newUser.getId(), user.getId());
        Assertions.assertEquals(newUser.getFirstname(), user.getFirstname());
    }

    @Test
    @DisplayName("Test user deleted successfully")
    public void testUserDeletedSuccessfully() {
        userRepository.deleteById(1L);

        Assertions.assertEquals(0L, userRepository.count());
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

}
