import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.api.ForismaticApi
import kotlinx.coroutines.launch

@Composable
fun QuoteScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var quoteText by remember { mutableStateOf("Loading...") }
    var quoteAuthor by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val quote = ForismaticApi.service.getQuote()
                quoteText = quote.quoteText
                quoteAuthor = quote.quoteAuthor
            } catch (e: Exception) {
                quoteText = "Failed to load quote. Please try again."
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
        Text(text = quoteText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "- $quoteAuthor", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    val quote = ForismaticApi.service.getQuote()
                    quoteText = quote.quoteText
                    quoteAuthor = quote.quoteAuthor
                } catch (e: Exception) {
                    quoteText = "Failed to load quote. Please try again."
                }
            }
        }) {
            Text("Get Another Quote")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("task_list") // Navigate to TaskListScreen
        }) {
            Text("Back to Task List")
        }
    }
}
