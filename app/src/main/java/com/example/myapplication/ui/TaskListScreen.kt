package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Task
import com.example.myapplication.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController

@Composable
fun TaskListScreen(navController: NavController, onLogout: () -> Unit) {
    val repository = TaskRepository()
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val priorityOrder = listOf("High", "Medium", "Low")

    LaunchedEffect(Unit) {
        repository.getTasks { fetchedTasks ->
            tasks = fetchedTasks
            println("Fetched Tasks: $fetchedTasks") // Debugging log
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Task List", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                }) {
                    Text("Logout")
                }
            }
        }

        // Feature Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { navController.navigate("quote") }) {
                        Text("Get Inspiration")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigate("joke") }) {
                        Text("Show Me a Joke")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { navController.navigate("light_sensor") }) {
                        Text("Light Sensor")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigate("accelerometer") }) {
                        Text("Accelerometer")
                    }
                }
            }
        }

        // Task Input
        item {
            if (taskToEdit == null) {
                Text(text = "Add New Task", style = MaterialTheme.typography.titleLarge)
            } else {
                Text(text = "Edit Task", style = MaterialTheme.typography.titleLarge)
            }
        }

        item {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Priority and Due Date Row
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                dueDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Due Date: ${dueDate?.let { dateFormatter.format(it) } ?: "Not Set"}")
                }

                Spacer(modifier = Modifier.width(8.dp))

                PriorityDropdown(
                    selectedPriority = priority,
                    onPriorityChange = { priority = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Add or Update Task Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && dueDate != null) {
                        if (taskToEdit == null) {
                            val newTask = Task(title = title, description = description, priority = priority, dueDate = dueDate!!)
                            repository.addTask(newTask) { isSuccess ->
                                if (isSuccess) {
                                    title = ""
                                    description = ""
                                    priority = "Medium"
                                    dueDate = null
                                }
                            }
                        } else {
                            val updatedTask = taskToEdit!!.copy(
                                title = title,
                                description = description,
                                priority = priority,
                                dueDate = dueDate!!
                            )
                            repository.updateTask(updatedTask) { isSuccess ->
                                if (isSuccess) {
                                    taskToEdit = null
                                    title = ""
                                    description = ""
                                    priority = "Medium"
                                    dueDate = null
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (taskToEdit == null) "Add Task" else "Save Changes")
            }
        }

        // Task Lists
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Active Tasks", style = MaterialTheme.typography.titleLarge)
        }

        items(
            tasks.filter { !it.isCompleted }
                .sortedBy { priorityOrder.indexOf(it.priority) }
        ) { task ->
            TaskItem(
                task = task,
                repository = repository,
                onEdit = { selectedTask ->
                    taskToEdit = selectedTask
                    title = selectedTask.title
                    description = selectedTask.description
                    priority = selectedTask.priority
                    dueDate = selectedTask.dueDate
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Completed Tasks", style = MaterialTheme.typography.titleLarge)
        }

        items(
            tasks.filter { it.isCompleted }
                .sortedBy { priorityOrder.indexOf(it.priority) }
        ) { task ->
            TaskItem(
                task = task,
                repository = repository,
                onEdit = { selectedTask ->
                    taskToEdit = selectedTask
                    title = selectedTask.title
                    description = selectedTask.description
                    priority = selectedTask.priority
                    dueDate = selectedTask.dueDate
                }
            )
        }

        // Delete All Tasks Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    repository.deleteAllTasks { isSuccess ->
                        if (isSuccess) {
                            tasks = emptyList()
                            println("All tasks deleted successfully.")
                        } else {
                            println("Failed to delete all tasks.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete All Tasks")
            }
        }
    }
}



@Composable
fun TaskItem(
    task: Task,
    repository: TaskRepository,
    onEdit: (Task) -> Unit
) {
    val priorityColor = when (task.priority) {
        "High" -> MaterialTheme.colorScheme.error
        "Medium" -> MaterialTheme.colorScheme.secondary
        "Low" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Priority: ${task.priority}", color = priorityColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Due Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.dueDate)}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val updatedTask = task.copy(isCompleted = !task.isCompleted)
                    repository.updateTask(updatedTask) { isSuccess ->
                        if (isSuccess) {
                            println("Task updated: $updatedTask")
                        }
                    }
                }) {
                    Text(if (task.isCompleted) "Active" else "Completed")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onEdit(task) }) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    repository.deleteTask(task.id) { isSuccess ->
                        if (!isSuccess) {
                            println("Failed to delete task")
                        }
                    }
                }) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun PriorityDropdown(
    selectedPriority: String,
    onPriorityChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val priorities = listOf("High", "Medium", "Low")

    Box(modifier = modifier) { // Use the modifier here
        Button(onClick = { expanded = true }) {
            Text(text = "Priority: $selectedPriority")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            priorities.forEach { priority ->
                DropdownMenuItem(
                    onClick = {
                        onPriorityChange(priority)
                        expanded = false
                    },
                    text = { Text(priority) }
                )
            }
        }
    }
}


@Composable
fun AccelerometerScreen(onBackToHome: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var xValue by remember { mutableStateOf(0f) }
    var yValue by remember { mutableStateOf(0f) }
    var zValue by remember { mutableStateOf(0f) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    xValue = event.values[0]
                    yValue = event.values[1]
                    zValue = event.values[2]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not used
            }
        }
    }

    DisposableEffect(Unit) {
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Accelerometer Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "X: $xValue", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Y: $yValue", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Z: $zValue", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToHome) {
            Text("Back to Home")
        }
    }
}

