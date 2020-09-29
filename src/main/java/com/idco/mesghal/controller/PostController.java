package com.idco.mesghal.controller;

import com.idco.mesghal.model.Post;
import com.idco.mesghal.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService service;

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody Post post) {
        try {
            return ResponseEntity.created(URI.create("/post/" + post.getId())).body(service.save(post));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping()
//    public Page<Post> list(Pageable pageable) {
//        return service.listAllPagination(pageable);
//    }

    @GetMapping()
    public List<Post> list() {
        return service.listAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Post> retrieve(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(service.get(id), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> update(@RequestBody Post post, @PathVariable Long id) {
        try {
            return new ResponseEntity<>(service.update(post, id), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}