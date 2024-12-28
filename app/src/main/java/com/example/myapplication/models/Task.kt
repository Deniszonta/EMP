package com.example.myapplication.models

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Task(
    var id: String = "",
    @PropertyName("title") var title: String = "",
    @PropertyName("description") var description: String = "",
    @PropertyName("isCompleted") var isCompleted: Boolean = false,
    @PropertyName("priority") var priority: String = "Medium", // Default priority
    @PropertyName("dueDate") var dueDate: Date? = Date() // Optional due date
){
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "", false, "Medium", Date())
}
