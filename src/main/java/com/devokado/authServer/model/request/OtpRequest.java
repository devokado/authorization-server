package com.devokado.authServer.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {
    @Size(min = 11, max = 11)
    private String mobile;
}
