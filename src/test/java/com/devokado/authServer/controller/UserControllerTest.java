//package com.devokado.authServer.controller;
//
//import com.devokado.authServer.model.request.LoginRequest;
//import com.devokado.authServer.model.request.UserRequest;
//import com.devokado.authServer.service.UserService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpResponseFactory;
//import org.apache.http.HttpStatus;
//import org.apache.http.HttpVersion;
//import org.apache.http.impl.DefaultHttpResponseFactory;
//import org.apache.http.message.BasicStatusLine;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentMatchers;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import javax.ws.rs.core.Response;
//import java.net.URI;
//
//import static org.mockito.Mockito.doReturn;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//public class UserControllerTest {
//    @MockBean
//    private UserService userService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    @DisplayName("Create user")
//    public void testRegisterUserSuccessfully() throws Exception {
//        UserRequest newUser = new UserRequest("09137911396",
//                "s.a.modares.h@gmail.com", "1234", "Ali", "Modares", true);
//
//        doReturn(201).when(userService).createUser(ArgumentMatchers.any());
//        doReturn(Response.created(URI.create("/users/register"))).when(userService).createKeycloakUser(ArgumentMatchers.any());
//        mockMvc.perform(post("/users/register")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(new ObjectMapper().writeValueAsString(newUser)))
//
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
//    }
//
//    @Test
//    @DisplayName("login user")
//    public void testLoginUser() throws Exception {
//        LoginRequest loginRequest = new LoginRequest("09137911396", "1234", "password", "auth-server", "e142c053-5cbc-4b8a-9dfb-1fe006e2651b");
//
//        HttpResponseFactory factory = new DefaultHttpResponseFactory();
//        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"), null);
//        doReturn(response).when(userService).createToken(ArgumentMatchers.any());
//
//        mockMvc.perform(post("/users/login")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(new ObjectMapper().writeValueAsString(loginRequest)))
//
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
//    }
//
//}
