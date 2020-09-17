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

    public Post save(Post post) {
        return repository.save(post);
    }

    public Post get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Post update(Post post, Long id) {
        Post existPost = repository.findById(id).orElse(null);
        if (existPost != null) {
            if (post.getTitle() != null)
                existPost.setTitle(post.getTitle());

            if (post.getContent() != null)
                existPost.setContent(post.getContent());

            return repository.save(existPost);
        }
        return null;
    }
}
