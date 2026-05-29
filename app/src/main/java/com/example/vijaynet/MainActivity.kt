package com.example.retrotest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vijaynet.lib.VijayRetrofit
import com.example.vijaynet.lib.GsonConverterFactory
import com.example.vijaynet.lib.Post
import com.example.vijaynet.lib.PostService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // 1. Initialize once at the class level
    private val loggingClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("VijayNet", "URL: ${request.url}")
            chain.proceed(request)
        }.build()

    private val vijayRetrofit = VijayRetrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .client(loggingClient)
        .addConverterFactory(GsonConverterFactory())
        .build()

    private val postService = vijayRetrofit.create(PostService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var postItem by remember { mutableStateOf<Post?>(null) }
            var errorState by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(true) }
            var refreshTrigger by remember { mutableIntStateOf(0) }

            // 2. The effect now only handles the state orchestration
            LaunchedEffect(refreshTrigger) {
                isLoading = true
                errorState = ""

                try {
                    // The library now handles Dispatchers.IO internally
                    postItem = postService.getPostById(5)
                } catch (e: Exception) {
                    errorState = "Error: ${e.localizedMessage ?: "Network Error"}"
                } finally {
                    isLoading = false
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        Box(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else if (postItem != null) {
                                Column {
                                    Text("Title: ${postItem?.title}", style = MaterialTheme.typography.titleMedium)
                                    Text("Body: ${postItem?.body}", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(errorState, color = MaterialTheme.colorScheme.error)
                                    Button(onClick = { refreshTrigger++ }) { Text("Retry") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}