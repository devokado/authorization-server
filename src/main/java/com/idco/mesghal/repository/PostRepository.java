package com.idco.mesghal.repository;

import com.idco.mesghal.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

public interface PostRepository extends JpaRepository<Post, Long> {
//    List<Post> findByTitle(@Param("name") String name);
}