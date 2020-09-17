package com.idco.mesghal.service;

import com.idco.mesghal.repository.PostRepository;
import com.idco.mesghal.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PostService {
    @Autowired
    private PostRepository repository;

    public List<Post> listAll() {
        return repository.findAll();
    }

    public void save(Post post) {
        repository.save(post);
    }

    public Post get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
