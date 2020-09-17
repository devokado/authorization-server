package com.idco.mesghal.controller;

import com.idco.mesghal.model.Post;
import com.idco.mesghal.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService service;

    @PostMapping()
    public void create(@RequestBody Post post) {
        service.save(post);
    }

    @GetMapping()
    public List<Post> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> retrieve(@PathVariable Long id) {
        try {
            Post post = service.get(id);
            return new ResponseEntity<>(post, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody Post post, @PathVariable Long id) {
        try {
            Post existPost = service.get(id);
            service.save(post);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}