package com.example.vijaynet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vijaynet.converter.GsonConverterFactory
import com.example.vijaynet.ui.theme.VijayNetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class vijaynet : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var postItem by remember { mutableStateOf<Post?>(null) }
            var errorState by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(true) }
            var refreshTrigger by remember { mutableStateOf(0) }

            // Safe Async Network Execution Layer Away From UI Thread
            LaunchedEffect(refreshTrigger) {
                isLoading = true
                errorState = ""
                postItem = null

                try {
                    withContext(Dispatchers.IO) {
                        // 1. Instantiating isolated short-timeout client
                        val loggingClient = OkHttpClient.Builder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .addInterceptor { chain ->
                                val request = chain.request()
                                println("VijayNet Requesting: ${request.url}")
                                chain.proceed(request)
                            }.build()

                        // 2. Initializing our custom dynamic mirror framework
                        val vijayRetrofit = VijayRetrofit.Builder()
                            .baseUrl("https://jsonplaceholder.typicode.com/")
                            .client(loggingClient)
                            .addConverterFactory(GsonConverterFactory())
                            .build()

                        val postService = vijayRetrofit.create(PostService::class.java)

                        // 3. Make the abstract api call signature invocation
                        val result = postService.getPostById(5)
                        postItem = result
                    }
                } catch (e: Exception) {
                    errorState = "Connection Blocked: ${e.localizedMessage ?: "Timeout"}"
                } finally {
                    isLoading = false
                }
            }

            VijayNetTheme {
                // Surface guarantees background canvas mapping inside your system windows
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (postItem != null) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Title: ${postItem?.title}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Body: ${postItem?.body}",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            } else if (errorState.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = errorState,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { refreshTrigger++ }) {
                                        Text("Retry Connection")
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Connecting via Hotspot...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}