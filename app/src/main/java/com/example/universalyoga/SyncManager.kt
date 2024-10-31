package com.example.universalyoga

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class SyncManager(private val context: Context) {
    private val dbHelper = YogaDBHelper(context)
    private val baseUrl = "http://10.0.2.2:3000/api/sync"
    val networkHelper = NetworkConnectionHelper(context)  // Changed to public

    interface SyncCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
    }

    fun uploadDataToServer(callback: SyncCallback) {
        if (networkHelper.value != true) {
            callback.onError("No internet connection available")
            return
        }

        try {
            // Get all courses and classes from local database
            val courses = dbHelper.getAllCourses()
            val classes = dbHelper.getAllClasses() // Get all classes
            Log.d(TAG, "Courses to upload: ${courses.size}")
            Log.d(TAG, "Classes to upload: ${classes.size}")
            // Log the first few items of each list for verification
            courses.take(2).forEach { course ->
                Log.d(TAG, "Sample course: $course")
            }
            classes.take(2).forEach { yogaClass ->
                Log.d(TAG, "Sample class: $yogaClass")
            }
            // Create JSON Arrays for courses and classes
            val coursesJsonArray = JSONArray()
            courses.forEach { course ->
                coursesJsonArray.put(JSONObject().apply {
                    put("id", course.id)
                    put("type", course.type)
                    put("dayOfWeek", course.dayOfWeek)
                    put("timeOfDay", course.timeOfDay)
                    put("duration", course.duration)
                    put("capacity", course.capacity)
                    put("price", course.price)
                    put("description", course.description)
                })
            }

            val classesJsonArray = JSONArray()
            classes.forEach { yogaClass ->
                classesJsonArray.put(JSONObject().apply {
                    put("id", yogaClass.id)
                    put("courseId", yogaClass.courseId)
                    put("date", yogaClass.date)
                    put("teacher", yogaClass.teacher)
                    put("comments", yogaClass.comments)
                })
            }

            // Create the main JSON payload
            val jsonPayload = JSONObject().apply {
                put("courses", coursesJsonArray)
                put("classes", classesJsonArray)
            }

            Log.d(TAG, "Upload payload: $jsonPayload")

            // Make API request
            val request = JsonObjectRequest(
                Request.Method.POST,
                "$baseUrl/upload",
                jsonPayload,
                { response ->
                    Log.d(TAG, "Upload response: $response")
                    val success = response.optBoolean("success", false)
                    if (success) {
                        callback.onSuccess("Data uploaded successfully")
                    } else {
                        callback.onError(response.optString("message", "Unknown error"))
                    }
                },
                { error ->
                    Log.e(TAG, "Upload error: ${error.message}", error)
                    callback.onError("Network error: ${error.message}")
                }
            )

            Volley.newRequestQueue(context).add(request)

        } catch (e: Exception) {
            Log.e(TAG, "Error preparing upload: ${e.message}", e)
            callback.onError("Error preparing data: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}