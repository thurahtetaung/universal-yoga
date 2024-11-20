package com.example.universalyoga

// Data class representing a search result
data class SearchResult(
    val classId: Long,
    val courseId: Long,
    val courseType: String,
    val classDate: String,
    val teacherName: String,
    val courseTime: String,
    val courseDay: String
)