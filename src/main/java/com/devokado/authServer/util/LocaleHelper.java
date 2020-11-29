package com.devokado.authServer.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class LocaleHelper {
    @Value("${application.language}")
    private String language;

    @Value("${application.country}")
    private String country;

    public String getString(String key) {
        Locale locale = new Locale(language, country);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        return messages.getString(key);
    }
}
