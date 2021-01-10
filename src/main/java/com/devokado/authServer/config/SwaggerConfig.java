package com.devokado.authServer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    public Docket swaggerConfiguration() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.ant("/post/**"))
                .apis(RequestHandlerSelectors.basePackage("com.devokado.authServer"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Auth Server",
                "",
                "",
                "",
                new Contact("Ali Modares", "", "s.a.modares.h@gmail.com"),
                "License of API", "API license URL", Collections.emptyList());
    }
}