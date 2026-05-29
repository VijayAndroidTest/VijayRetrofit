# VijayRetrofit

A lightweight, type-safe HTTP client framework for native Android development written in Kotlin. This project is built entirely from scratch using Java Dynamic Proxies, structural Reflection, and customizable Converter boundaries to mirror the core mechanical behavior of Square's Retrofit client.

## Core Architecture Design
The library operates inside an isolated thread layer to eliminate main-thread bottlenecks, making it resilient against slow network gateways, proxy hops, and mobile tethering/hotspot drops.

- 🚀 **Type-Safe Abstract API Execution:** Leverages Kotlin Coroutines (`suspend` functions) out-of-the box.
- 🔌 **Pluggable Decoupled Converters:** Features custom serialization bindings supporting `GsonConverterFactory`.
- 🛠️ **Transitive OkHttp Interceptor Support:** Exposes base configurations to chain application interceptors, logging matrices, or personalized time-out limits fluidly.
- 🎛️ **R8/ProGuard Optimized:** Pre-configured protection rules preserve dynamic proxy methods from structural obfuscation or stripping during release minification phases.

---

## Technical Implementation Usage

### 1. Define Data Architecture and Endpoints
```kotlin
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface PostService {
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: Int): Post
}