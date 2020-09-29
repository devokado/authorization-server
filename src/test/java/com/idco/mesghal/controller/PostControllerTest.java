package com.idco.mesghal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idco.mesghal.model.Post;
import com.idco.mesghal.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {
    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get all posts - GET /post")
    public void testGetPosts() throws Exception {
        Post mockPost1 = new Post("Title test1", "Content test1");
        Post mockPost2 = new Post("Title test2", "Content test2");

        List<Post> posts = new ArrayList<>();
        posts.add(mockPost1);
        posts.add(mockPost2);

        doReturn(posts).when(postService).listAll();

        mockMvc.perform(MockMvcRequestBuilders.get("/post"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("$[0].title", is("Title test1")))
                .andExpect(jsonPath("$[1].title", is("Title test2")));
    }

    @Test
    @DisplayName("Get specific post with id - GET /post/1")
    public void testGetSpecificPost() throws Exception {
        Post mockPost = new Post("Title test", "Content test");

        doReturn(mockPost).when(postService).get(mockPost.getId());

        mockMvc.perform(get("/post/{id}", mockPost.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("$.title", is("Title test")))
                .andExpect(jsonPath("$.content", is("Content test")));


    }

    @Test
    @DisplayName("Create new post - POST /post")
    public void testSavePost() throws Exception {
        Post newPost = new Post("Title test", "Content test");
        Post mockPost = new Post("Title test new", "Content test new");

        doReturn(mockPost).when(postService).save(ArgumentMatchers.any());

        mockMvc.perform(post("/post")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(newPost)))

                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("$.title", is("Title test new")))
                .andExpect(jsonPath("$.content", is("Content test new")));
    }

    @Test
    @DisplayName("Update an existing post - PUT /post/1")
    public void testUpdatePost() throws Exception {
        Post newPost = new Post("Title test", "Content test");
        Post mockPost = new Post("Title test new", "Content test new");

        doReturn(mockPost).when(postService).update(ArgumentMatchers.any(), eq(mockPost.getId()));

        mockMvc.perform(put("/post/{id}", mockPost.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(newPost)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("$.title", is("Title test new")))
                .andExpect(jsonPath("$.content", is("Content test new")));
    }

    @Test
    @DisplayName("Delete post - DELETE /post")
    public void testDeletePost() throws Exception {
        Post mockPost = new Post("Title test", "Content test");

        doReturn(mockPost).when(postService).get(mockPost.getId());

        mockMvc.perform(delete("/post/{id}", 1)).andExpect(status().isNoContent());
    }
}
