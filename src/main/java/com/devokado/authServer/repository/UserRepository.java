package com.devokado.authServer.repository;

import com.devokado.authServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);

    Optional<User> findByEmail(String email);

    Optional<User> findByKuuid(String kuuid);
}