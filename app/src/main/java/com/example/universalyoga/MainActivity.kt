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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var addCourseFab: FloatingActionButton
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var dbHelper: YogaDBHelper
    private val courses = mutableListOf<YogaCourse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

//            // Clear existing data
//            db.execSQL("DELETE FROM classes")
//            db.execSQL("DELETE FROM courses")
//            // Add fresh test data
//            addTestData()


        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing database", e)
            Toast.makeText(this, "Error initializing database", Toast.LENGTH_LONG).show()
        }
    }

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
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}