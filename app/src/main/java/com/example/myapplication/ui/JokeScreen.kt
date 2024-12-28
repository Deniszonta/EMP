package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.api.JokeApi
import kotlinx.coroutines.launch

@Composable
fun JokeScreen(onBackToHome: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var jokeText by remember { mutableStateOf("Loading a joke...") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val jokeResponse = JokeApi.service.getRandomJoke()
                jokeText = when (jokeResponse.type) {
                    "single" -> jokeResponse.joke.orEmpty()
                    "twopart" -> "${jokeResponse.setup}\n${jokeResponse.delivery}"
                    else -> "Couldn't fetch a joke, try again!"
                }
            } catch (e: Exception) {
                jokeText = "Failed to load a joke. Please check your connection."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = jokeText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    val jokeResponse = JokeApi.service.getRandomJoke()
                    jokeText = when (jokeResponse.type) {
                        "single" -> jokeResponse.joke.orEmpty()
                        "twopart" -> "${jokeResponse.setup}\n${jokeResponse.delivery}"
                        else -> "Couldn't fetch a joke, try again!"
                    }
                } catch (e: Exception) {
                    jokeText = "Failed to load a joke. Please check your connection."
                }
            }
        }) {
            Text("Get Another Joke")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToHome) {
            Text("Back to Home")
        }
    }
}
