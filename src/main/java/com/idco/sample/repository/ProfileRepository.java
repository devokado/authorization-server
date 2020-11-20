package com.idco.sample.repository;

import com.idco.sample.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByMobileAndCode(String mobile, String code);
}