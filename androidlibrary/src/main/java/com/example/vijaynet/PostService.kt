package com.example.vijaynet

import com.example.vijaynet.annotations.GET
import com.example.vijaynet.annotations.POSTS
import com.example.vijaynet.annotations.Path
import com.example.vijaynet.annotations.Query

interface PostService {
    @GET("posts/1")
    suspend fun getFirstPost(): Post

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: Int): Post

    // 1. Tests Query Parameters: https://jsonplaceholder.typicode.com/posts?userId=1
    @GET("posts")
    suspend fun getPostsByUserId(@Query("userId") userId: Int): List<Post>

    // 2. Tests Form POST body execution payloads
    @POSTS("posts")
    suspend fun createNewPost(body: Post): Post
}