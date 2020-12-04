//package com.devokado.authServer.controller;
//
//import com.devokado.authServer.model.request.UserRequest;
//import com.devokado.authServer.service.UserService;
//import com.fasterxml.jackson.databind.ObjectMapper;
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
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.mockito.Mockito.doReturn;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//public class PostControllerTest {
//    @MockBean
//    private UserService userService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    @DisplayName("")
//    public void testRegisterUser() throws Exception {
//        UserRequest newUser = new UserRequest("09137911396",
//                "s.a.modares.h@gmail.com", "1234", "Ali", "Modares", true);
//
//        Post mockPost = new Post(1L, "Title test new", "Content test new");
//
//        doReturn(mockPost).when(userService).save(ArgumentMatchers.any());
//
//        mockMvc.perform(post("/post")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(new ObjectMapper().writeValueAsString(newPost)))
//
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//
//                .andExpect(jsonPath("$.title", is("Title test new")))
//                .andExpect(jsonPath("$.content", is("Content test new")));
//    }
//
//    @Test
//    @DisplayName("Get specific post with id - GET /post/1")
//    public void testGetSpecificPost() throws Exception {
//        Post mockPost = new Post(1L, "Title test", "Content test");
//
//        doReturn(mockPost).when(postService).get(mockPost.getId());
//
//        mockMvc.perform(get("/post/{id}", mockPost.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//
//                .andExpect(jsonPath("$.title", is("Title test")))
//                .andExpect(jsonPath("$.content", is("Content test")));
//
//
//    }
//
//    @Test
//    @DisplayName("Create new post - POST /post")
//    public void testSavePost() throws Exception {
//        Post newPost = new Post(1L, "Title test", "Content test");
//        Post mockPost = new Post(1L, "Title test new", "Content test new");
//
//        doReturn(mockPost).when(postService).save(ArgumentMatchers.any());
//
//        mockMvc.perform(post("/post")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(new ObjectMapper().writeValueAsString(newPost)))
//
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//
//                .andExpect(jsonPath("$.title", is("Title test new")))
//                .andExpect(jsonPath("$.content", is("Content test new")));
//    }
//
//    @Test
//    @DisplayName("Update an existing post - PUT /post/1")
//    public void testUpdatePost() throws Exception {
//        Post newPost = new Post(1L, "Title test", "Content test");
//        Post mockPost = new Post(1L, "Title test new", "Content test new");
//
//        doReturn(mockPost).when(postService).update(ArgumentMatchers.any(), eq(mockPost.getId()));
//
//        mockMvc.perform(put("/post/{id}", mockPost.getId())
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(new ObjectMapper().writeValueAsString(newPost)))
//
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//
//                .andExpect(jsonPath("$.title", is("Title test new")))
//                .andExpect(jsonPath("$.content", is("Content test new")));
//    }
//
//    @Test
//    @DisplayName("Delete post - DELETE /post")
//    public void testDeletePost() throws Exception {
//        Post mockPost = new Post(1L, "Title test", "Content test");
//
//        doReturn(mockPost).when(postService).get(mockPost.getId());
//
//        mockMvc.perform(delete("/post/{id}", mockPost.getId())).andExpect(status().isNoContent());
//    }
//}
