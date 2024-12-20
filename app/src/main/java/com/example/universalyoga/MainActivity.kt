package com.example.universalyoga

import YogaCourseAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var addCourseFab: FloatingActionButton
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var dbHelper: YogaDBHelper
    private lateinit var syncManager: SyncManager
    private var isNetworkAvailable = false
    private val courses = mutableListOf<YogaCourse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        syncManager = SyncManager(this)
        // Initialize network observer to check network status
        setupNetworkObserver()
        // Initialize database first
        initializeDatabase()

        // Then initialize views and setup UI
        initializeViews()
        setupRecyclerView()
        setupFab()
        setupToolbar()

        // Load initial data
        loadCourses()
    }
    private fun setupNetworkObserver() {
        syncManager.networkHelper.observe(this) { isConnected ->
            isNetworkAvailable = isConnected
            Log.d("MainActivity", "Network status changed: $isConnected")
        }
    }
    // Initialize the database with test data if it's empty, for demonstration purposes
    private fun initializeDatabase() {
        dbHelper = YogaDBHelper(this)
        try {
            val db = dbHelper.writableDatabase

            // Check if database is empty
            val cursor = db.rawQuery("SELECT COUNT(*) FROM courses", null)
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()

            if (count == 0) {
                addTestData()
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing database", e)
            Toast.makeText(this, "Error initializing database", Toast.LENGTH_LONG).show()
        }
    }

    // Add dummy test data to the database
    private fun addTestData() {
        val testCourses = listOf(
            YogaCourse(
                id = 0,  // Let SQLite auto-generate IDs
                type = "Flow Yoga",
                dayOfWeek = "Monday",
                timeOfDay = "10:00 AM",
                duration = 60,
                capacity = 20,
                price = 15.0,
                description = "Beginner friendly flow yoga class"
            ),
            YogaCourse(
                id = 0,
                type = "Aerial Yoga",
                dayOfWeek = "Wednesday",
                timeOfDay = "2:00 PM",
                duration = 90,
                capacity = 15,
                price = 25.0,
                description = "Advanced aerial yoga session"
            ),
            YogaCourse(
                id = 0,
                type = "Family Yoga",
                dayOfWeek = "Saturday",
                timeOfDay = "11:00 AM",
                duration = 45,
                capacity = 30,
                price = 20.0,
                description = "Fun yoga class for families"
            )
        )

        try {
            // Insert courses and store their actual IDs
            val courseIds = testCourses.map { course ->
                dbHelper.insertCourse(course)
            }

            // Create class data using the actual course IDs
            val testClasses = listOf(
                // Flow Yoga classes (courseIds[0])
                YogaClass(
                    id = 0,
                    courseId = courseIds[0],
                    date = "November 4, 2024",
                    teacher = "Sarah Johnson",
                    comments = "Regular session"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[0],
                    date = "November 11, 2024",
                    teacher = "Sarah Johnson",
                    comments = "Regular session"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[0],
                    date = "November 18, 2024",
                    teacher = "Mike Wilson",
                    comments = "Guest teacher session"
                ),

                // Aerial Yoga classes (courseIds[1])
                YogaClass(
                    id = 0,
                    courseId = courseIds[1],
                    date = "October 30, 2024",
                    teacher = "Emma Davis",
                    comments = "Advanced techniques focus"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[1],
                    date = "November 6, 2024",
                    teacher = "Emma Davis",
                    comments = "Regular session"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[1],
                    date = "November 13, 2024",
                    teacher = "John Smith",
                    comments = "Substitute teacher"
                ),

                // Family Yoga classes (courseIds[2])
                YogaClass(
                    id = 0,
                    courseId = courseIds[2],
                    date = "November 2, 2024",
                    teacher = "Lisa Brown",
                    comments = "Kid-friendly session"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[2],
                    date = "November 9, 2024",
                    teacher = "Lisa Brown",
                    comments = "Regular session"
                ),
                YogaClass(
                    id = 0,
                    courseId = courseIds[2],
                    date = "November 16, 2024",
                    teacher = "Mike Wilson",
                    comments = "Special family event"
                )
            )

            // Insert classes
            testClasses.forEach { yogaClass ->
                val classId = dbHelper.insertClass(yogaClass)
                Log.d("MainActivity", "Added class with ID: $classId for course: ${yogaClass.courseId}")
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error adding test data", e)
        }
    }

    // Load courses from the database
    private fun loadCourses() {
        try {
            courses.clear()
            val loadedCourses = dbHelper.getAllCourses()
            courses.addAll(loadedCourses)
            updateRecyclerView(courses)

            Log.d("MainActivity", "Loaded ${courses.size} courses from database")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading courses", e)
            Toast.makeText(this, "Error loading courses", Toast.LENGTH_SHORT).show()
        }
    }

    // Register activity result contracts for starting activities and handling results
    private val addCourseResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newCourse = result.data?.getParcelableExtra<YogaCourse>("COURSE")
            newCourse?.let {
                try {
                    val id = dbHelper.insertCourse(it)
                    if (id > 0) {
                        loadCourses()
                        Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error adding course", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error inserting course", e)
                    Toast.makeText(this, "Error adding course", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val viewCourseResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadCourses()
        }
    }

    private fun initializeViews() {
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView)
        addCourseFab = findViewById(R.id.addCourseFab)
        topAppBar = findViewById(R.id.topAppBar)
    }

    private fun setupRecyclerView() {
        coursesRecyclerView.layoutManager = LinearLayoutManager(this)
        updateRecyclerView(courses)
    }

    private fun setupFab() {
        addCourseFab.setOnClickListener {
            val intent = Intent(this, CourseFormActivity::class.java)
            addCourseResult.launch(intent)
        }
    }

    // Update the RecyclerView with the latest course data
    private fun updateRecyclerView(courses: List<YogaCourse>) {
        coursesRecyclerView.adapter = YogaCourseAdapter(courses) { course ->
            val intent = Intent(this, CourseDetailActivity::class.java).apply {
                putExtra("COURSE", course)
            }
            viewCourseResult.launch(intent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(topAppBar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)  // Contains search and resetDatabase
        menuInflater.inflate(R.menu.sync_menu, menu)    // Contains upload
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_upload -> {
                if (isNetworkAvailable) {
                    // Show a confirmation dialog before uploading data
                    showUploadConfirmationDialog()
                } else {
                    // Show a dialog if there is no network connection
                    showNoNetworkDialog()
                }
                true
            }
            R.id.search -> {
                // Start the search activity when the search icon is clicked
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.resetDatabase -> {
                // Show a confirmation dialog before resetting the database
                showResetDatabaseConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Show a confirmation dialog before resetting the database
    private fun showResetDatabaseConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Database")
            .setMessage("Are you sure you want to reset the local database? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                // Reset the database
                resetDatabase()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Reset the database by deleting all data and updating the UI
    private fun resetDatabase() {
        try {
            dbHelper.resetDatabase()
            loadCourses()
            Toast.makeText(this, "Database reset successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error resetting database", e)
            Toast.makeText(this, "Error resetting database", Toast.LENGTH_SHORT).show()
        }
    }
    // Show a dialog if there is no network connection
    private fun showNoNetworkDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("OK", null)
            .show()
    }
    // Show a confirmation dialog before uploading data
    private fun showUploadConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Upload Data")
            .setMessage("Do you want to upload all local data to the server?")
            .setPositiveButton("Upload") { _, _ ->
                uploadData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    // Upload data to the server using the SyncManager
    private fun uploadData() {
        // Call the uploadDataToServer method of the SyncManager
        syncManager.uploadDataToServer(object : SyncManager.SyncCallback {
            override fun onSuccess(message: String) {
                runOnUiThread {
                    // Show a toast message on the UI thread
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    // Show an error dialog on the UI thread
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Upload Error")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        })
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}