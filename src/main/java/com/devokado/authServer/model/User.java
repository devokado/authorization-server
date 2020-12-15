package com.devokado.authServer.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String kuuid;

    @Column(unique = true, length = 32)
    private String mobile;

    @Email
    @Column(unique = true, length = 32)
    private String email;

    private long otpCdt;
    private int otpStatus;

    private String firstname;
    private String lastname;

    private Boolean status = true;
    @CreationTimestamp
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime cdt;
    @UpdateTimestamp
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime udt;

    public User(String mobile, @Email String email, String firstname, String lastname) {
        this.mobile = mobile;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public User(String kuuid, String mobile, @Email String email, long otpCdt, int otpStatus, String firstname, String lastname, Boolean status) {
        this.kuuid = kuuid;
        this.mobile = mobile;
        this.email = email;
        this.otpCdt = otpCdt;
        this.otpStatus = otpStatus;
        this.firstname = firstname;
        this.lastname = lastname;
        this.status = status;
    }
}
