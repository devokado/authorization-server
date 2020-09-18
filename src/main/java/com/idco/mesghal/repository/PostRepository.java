package com.idco.mesghal.repository;

import com.idco.mesghal.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

//    @Query(value = "SELECT title,content FROM Post", nativeQuery = true)
//    List<Post> customFind();
}