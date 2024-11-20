package com.example.universalyoga

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class SyncManager(private val context: Context) {
    private val dbHelper = YogaDBHelper(context)
    // Base URL for the PHP scripts
    private val baseUrl = "http://10.0.2.2/yogasite"
    val networkHelper = NetworkConnectionHelper(context)

    interface SyncCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
    }

    // Upload data to the server
    fun uploadDataToServer(callback: SyncCallback) {
        // Check if there is an internet connection
        if (networkHelper.value != true) {
            callback.onError("No internet connection available")
            return
        }

        try {
            // Get all courses and classes from the local database
            val courses = dbHelper.getAllCourses()
            val classes = dbHelper.getAllClasses()

            // Create JSON Arrays in the format expected by PHP endpoints
            val coursesJsonArray = JSONArray()
            courses.forEach { course ->
                coursesJsonArray.put(JSONObject().apply {
                    put("id", course.id)
                    put("day", course.dayOfWeek)
                    put("time", course.timeOfDay)
                    put("type", course.type)
                    put("capacity", course.capacity)
                    put("duration", course.duration)
                    put("price", course.price.toString())  // Convert to string as PHP expects text
                    put("description", course.description ?: "")
                })
            }

            // Log the JSON being sent for courses
            Log.d(TAG, "Sending courses JSON: ${coursesJsonArray.toString(2)}")

            // Create a custom JsonArrayRequest that handles string responses
            val courseRequest = object : JsonArrayRequest(
                Method.POST,
                "$baseUrl/saveCourse.php",
                coursesJsonArray,
                { response ->
                    Log.d(TAG, "Course upload success: $response")
                    uploadClasses(classes, callback)
                },
                { error ->
                    // Log the actual error response from server
                    val networkResponse = error.networkResponse
                    val errorMessage = if (networkResponse?.data != null) {
                        String(networkResponse.data)
                    } else {
                        error.message ?: "Unknown error"
                    }
                    Log.e(TAG, "Course upload error: $errorMessage")
                    callback.onError("Error uploading courses: $errorMessage")
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            // Add the request to the Volley request queue
            Volley.newRequestQueue(context).add(courseRequest)

        } catch (e: Exception) {
            Log.e(TAG, "Error preparing upload: ${e.message}", e)
            callback.onError("Error preparing data: ${e.message}")
        }
    }

    // Upload classes to the server
    private fun uploadClasses(classes: List<YogaClass>, callback: SyncCallback) {
        // Create a JSON Array for classes
        val classesJsonArray = JSONArray()
        classes.forEach { yogaClass ->
            classesJsonArray.put(JSONObject().apply {
                put("id", yogaClass.id)
                put("date_of_class", yogaClass.date)
                put("teacher", yogaClass.teacher)
                put("course_id", yogaClass.courseId)
                put("comments", yogaClass.comments ?: "")
            })
        }

        // Log the JSON being sent for classes
        Log.d(TAG, "Sending classes JSON: ${classesJsonArray.toString(2)}")

        val classRequest = object : JsonArrayRequest(
            Method.POST,
            "$baseUrl/saveClass.php",
            classesJsonArray,
            { response ->
                Log.d(TAG, "Class upload success: $response")
                callback.onSuccess("Data uploaded successfully")
            },
            { error ->
                val networkResponse = error.networkResponse
                val errorMessage = if (networkResponse?.data != null) {
                    String(networkResponse.data)
                } else {
                    error.message ?: "Unknown error"
                }
                Log.e(TAG, "Class upload error: $errorMessage")
                callback.onError("Error uploading classes: $errorMessage")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(classRequest)
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}