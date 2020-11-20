package com.idco.sample.service;

import com.idco.sample.model.Post;
import com.idco.sample.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    public List<Post> listAll() {
        return repository.findAll();
    }

    public Page<Post> listAllPagination(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Post> listAllSorting(Sort sort) {
        return repository.findAll(sort);
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
