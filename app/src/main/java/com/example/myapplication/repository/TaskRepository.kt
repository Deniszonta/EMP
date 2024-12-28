package com.example.myapplication.repository

import com.example.myapplication.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
}
