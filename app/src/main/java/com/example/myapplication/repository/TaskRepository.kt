package com.example.myapplication.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.models.Task
import com.example.myapplication.workers.TaskNotificationWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private fun userTaskCollection() =
        currentUser?.let { db.collection("tasks").document(it.uid).collection("user_tasks") }

    fun addTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskCollection = userTaskCollection() ?: return
        val taskId = taskCollection.document().id
        val newTask = task.copy(id = taskId)

        taskCollection.document(taskId).set(newTask)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
            .addOnFailureListener { exception ->
                exception.printStackTrace() // Logs any errors
            }
    }


    fun updateTask(task: Task, onComplete: (Boolean) -> Unit) {
        userTaskCollection()?.document(task.id)?.set(task)
            ?.addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        userTaskCollection()?.document(taskId)?.delete()
            ?.addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun getTasks(onResult: (List<Task>) -> Unit) {
        userTaskCollection()?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val tasks = snapshot.documents.mapNotNull { document ->
                    try {
                        val task = document.toObject(Task::class.java)
                        task?.copy(id = document.id) // Include document ID
                    } catch (e: Exception) {
                        println("Error deserializing task: ${e.message}")
                        null
                    }
                }
                println("Updated Tasks from Firestore: $tasks") // Debug log
                onResult(tasks)
            }
        }
    }

    fun deleteAllTasks(onComplete: (Boolean) -> Unit) {
        val taskCollection = userTaskCollection()
        if (taskCollection == null) {
            onComplete(false)
            return
        }

        taskCollection.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnCompleteListener { onComplete(it.isSuccessful) }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        onComplete(false)
                    }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete(false)
            }
    }

    fun scheduleHighPriorityNotification(task: Task, context: Context) {
        if (task.priority == "High") {
            val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                .setInitialDelay(1, TimeUnit.SECONDS) // Set appropriate delay
                .setInputData(workDataOf("taskTitle" to task.title, "taskDescription" to task.description))
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

}
