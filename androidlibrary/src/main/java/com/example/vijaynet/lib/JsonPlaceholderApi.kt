package com.example.vijaynet.lib

interface JsonPlaceholderApi {

    @GET("posts/{id}")
    suspend fun getPost(
        @Path("id") id: Int
    ): Post

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @POSTS("posts")
    suspend fun createPost(
        @Body post: Post
    ): Post
}