package com.idco.sample.controller

import com.idco.sample.model.Post
import com.idco.sample.service.PostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/kotlin")
class KotlinController {
    @Autowired
    lateinit var postService: PostService

    @GetMapping
    fun list(request: HttpServletRequest): List<Post> {
        return postService.listAll()
    }
}