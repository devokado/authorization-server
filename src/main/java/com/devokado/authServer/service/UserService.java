package com.devokado.authServer.service;

import com.devokado.authServer.model.User;
import com.devokado.authServer.repository.UserRepository;
import com.kavenegar.sdk.KavenegarApi;
import com.kavenegar.sdk.models.SendResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class UserService extends KeycloakService {

    @Value("${otp.code.size}")
    private int otpCodeSize;

    @Value("${otp.kavenegar.apikey}")
    private String apiKey;

    @Value("${otp.kavenegar.template}")
    private String template;

    @Autowired
    private UserRepository repository;

    public List<User> listAll() {
        return repository.findAll();
    }

    public Page<User> listAllPagination(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<User> listAllSorting(Sort sort) {
        return repository.findAll(sort);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public User getWithKuuid(String uuid) {
        return repository.findByKuuid(uuid).orElse(null);
    }

    public User getWithMobile(String mobile) {
        return repository.findByMobile(mobile).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public User update(User user, Long id) {
        User existUser = repository.findById(id).orElse(null);
        if (existUser != null) {
            if (user.getActive() != null)
                existUser.setActive(user.getActive());

            if (!StringUtils.isEmpty(existUser.getEmail()))
                existUser.setEmail(user.getEmail());

            if (!StringUtils.isEmpty(existUser.getFirstname()))
                existUser.setFirstname(user.getFirstname());

            if (!StringUtils.isEmpty(existUser.getLastname()))
                existUser.setLastname(user.getLastname());

            if (!StringUtils.isEmpty(existUser.getPassword()))
                existUser.setPassword(user.getPassword());

            if (!StringUtils.isEmpty(existUser.getOtp()))
                existUser.setOtp(user.getOtp());

            return repository.save(existUser);
        }
        return null;
    }

    public User updateWithKuuid(User user, String uuid) {
        User existUser = repository.findByKuuid(uuid).orElse(null);
        if (existUser != null) {
            if (user.getActive() != null)
                existUser.setActive(user.getActive());
            if (!StringUtils.isEmpty(existUser.getEmail()))
                existUser.setEmail(user.getEmail());

            if (!StringUtils.isEmpty(existUser.getFirstname()))
                existUser.setFirstname(user.getFirstname());

            if (!StringUtils.isEmpty(existUser.getLastname()))
                existUser.setLastname(user.getLastname());

            if (!StringUtils.isEmpty(existUser.getPassword()))
                existUser.setPassword(user.getPassword());

            if (!StringUtils.isEmpty(existUser.getOtp()))
                existUser.setOtp(user.getOtp());

            return repository.save(existUser);
        }
        return null;
    }

    public String createSMSCode() {
        if (otpCodeSize < 1) {
            throw new RuntimeException("Number of digits must be bigger than 0");
        }
        double maxValue = Math.pow(10.0, otpCodeSize); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long) (r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    public SendResult sendSMS(String code, String mobile) {
        KavenegarApi api = new KavenegarApi(apiKey);
        return api.verifyLookup(mobile, code, template);
    }


}
