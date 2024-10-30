package com.example.universalyoga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Locale

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var courseType: TextView
    private lateinit var schedule: TextView
    private lateinit var duration: TextView
    private lateinit var teacher: TextView
    private lateinit var capacity: TextView
    private lateinit var price: TextView
    private lateinit var description: TextView
    private lateinit var classesRecyclerView: RecyclerView
    private lateinit var addClassFab: FloatingActionButton
    private lateinit var scheduledClassAdapter: ScheduledClassAdapter
    private lateinit var dbHelper: YogaDBHelper
    private val classes = mutableListOf<YogaClass>()
    private var currentCourse: YogaCourse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        dbHelper = YogaDBHelper(this)
        // Get course from intent
        currentCourse = intent.getParcelableExtra("COURSE")

        if (currentCourse == null) {
            Toast.makeText(this, "Error loading course", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        displayCourseDetails()
        loadClasses()  // Load classes from database
        setupClassesList()
        setupFab()
    }

    private fun initializeViews() {
        topAppBar = findViewById(R.id.topAppBar)
        courseType = findViewById(R.id.courseType)
        schedule = findViewById(R.id.schedule)
        duration = findViewById(R.id.duration)
        capacity = findViewById(R.id.capacity)
        price = findViewById(R.id.price)
        description = findViewById(R.id.description)
        classesRecyclerView = findViewById(R.id.classesRecyclerView)
        addClassFab = findViewById(R.id.addClassFab)
    }
    private fun loadClasses() {
        currentCourse?.let { course ->
            classes.clear()
            val loadedClasses = dbHelper.getClassesForCourse(course.id)
            classes.addAll(loadedClasses.sortedBy {
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).parse(it.date)?.time ?: 0L
            })
            updateClassesList()
        }
    }
    private fun setupToolbar() {
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun displayCourseDetails() {
        currentCourse?.let { course ->
            supportActionBar?.title = course.type  // Set the title in the app bar
            courseType.text = course.type
            schedule.text = "${course.dayOfWeek} at ${course.timeOfDay}"
            duration.text = "${course.duration} minutes"
            capacity.text = "${course.capacity} people"
            price.text = "£${course.price}"
            description.text = course.description ?: "No description available"
        }
    }
    private val editCourseResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedCourse = result.data?.getParcelableExtra<YogaCourse>("COURSE")
            updatedCourse?.let { newCourse ->
                // Get existing classes for this course
                val existingClasses = dbHelper.getClassesForCourse(newCourse.id)

                when {
                    // If day changed, show day change dialog
                    currentCourse?.dayOfWeek != newCourse.dayOfWeek && existingClasses.isNotEmpty() -> {
                        showDayChangeConfirmationDialog(newCourse, existingClasses)
                    }
                    // Otherwise, just update
                    else -> {
                        updateCourseAndClasses(newCourse, existingClasses)
                    }
                }
            }
        }
    }
    private fun showDayChangeConfirmationDialog(
        newCourse: YogaCourse,
        existingClasses: List<YogaClass>
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Update Scheduled Classes")
            .setMessage(
                "The course day has changed from ${currentCourse?.dayOfWeek} to ${newCourse.dayOfWeek}. " +
                        "This will affect ${existingClasses.size} scheduled classes. " +
                        "Would you like to:\n\n" +
                        "1. Keep existing classes on their current dates\n" +
                        "2. Delete all scheduled classes\n" +
                        "3. Cancel course update"
            )
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Keep Existing") { dialog, _ ->
                updateCourseAndClasses(newCourse, existingClasses)
                dialog.dismiss()
            }
            .setPositiveButton("Delete All") { dialog, _ ->
                // First update the course
                val updateResult = dbHelper.updateCourse(newCourse.id, newCourse)
                if (updateResult > 0) {
                    // Then delete all classes
                    existingClasses.forEach { yogaClass ->
                        dbHelper.deleteClass(yogaClass.id)
                    }
                    currentCourse = newCourse
                    displayCourseDetails()
                    loadClasses()  // This will now properly update the UI
                    setResult(RESULT_OK)
                    Toast.makeText(this, "Course updated and classes cleared", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error updating course", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .show()
    }
    private fun updateCourseAndClasses(
        newCourse: YogaCourse,
        existingClasses: List<YogaClass>
    ) {
        // Update the course
        val updateResult = dbHelper.updateCourse(newCourse.id, newCourse)
        if (updateResult > 0) {
            currentCourse = newCourse
            displayCourseDetails()
            loadClasses()  // This will now properly update the UI
            setResult(RESULT_OK)
            Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error updating course", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupClassesList() {
        classesRecyclerView.layoutManager = LinearLayoutManager(this)
        updateClassesList()
    }
    private fun showClassOptionsPopupMenu(view: View, yogaClass: YogaClass) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.class_options_menu, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.editClass -> {
                        editClass(yogaClass)
                        true
                    }
                    R.id.deleteClass -> {
                        showDeleteClassConfirmationDialog(yogaClass)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun editClass(yogaClass: YogaClass) {
        currentCourse?.let { course ->
            val editSheet = EditClassBottomSheet.newInstance(yogaClass, course.dayOfWeek)
            editSheet.setEditClassListener(object : EditClassBottomSheet.EditClassListener {
                override fun onClassUpdated(updatedClass: YogaClass) {
                    val index = classes.indexOfFirst { it.id == updatedClass.id }
                    if (index != -1) {
                        classes[index] = updatedClass
                        updateClassesList()
                        Toast.makeText(
                            this@CourseDetailActivity,
                            "Class updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            editSheet.show(supportFragmentManager, "editClassBottomSheet")
        }
    }


    private fun setupFab() {
        addClassFab.setOnClickListener {
            currentCourse?.let { course ->
                val bottomSheet = AddClassBottomSheet.newInstance(
                    courseId = course.id,
                    courseDay = course.dayOfWeek
                )
                bottomSheet.setAddClassListener(object : AddClassBottomSheet.AddClassListener {
                    override fun onClassAdded(yogaClass: YogaClass) {
                        val id = dbHelper.insertClass(yogaClass)
                        if (id > 0) {
                            loadClasses()
                            updateClassesList()
                            Toast.makeText(
                                this@CourseDetailActivity,
                                "New class added",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@CourseDetailActivity,
                                "Error adding class",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
                bottomSheet.show(supportFragmentManager, "addClassBottomSheet")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editCourse -> {
                launchEditCourse()
                true
            }
            R.id.deleteCourse -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun launchEditCourse() {
        val intent = Intent(this, CourseFormActivity::class.java).apply {
            putExtra("COURSE_TO_EDIT", currentCourse)
        }
        editCourseResult.launch(intent)
    }
    private fun deleteClass(yogaClass: YogaClass) {
        val result = dbHelper.deleteClass(yogaClass.id)
        if (result > 0) {
            loadClasses()
            updateClassesList()
            Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error deleting class", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateClassesList() {
        if (!::scheduledClassAdapter.isInitialized) {
            scheduledClassAdapter = ScheduledClassAdapter(classes) { view, yogaClass ->
                showClassOptionsPopupMenu(view, yogaClass)
            }
            classesRecyclerView.adapter = scheduledClassAdapter
        } else {
            scheduledClassAdapter.updateClasses(classes)
        }
    }
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course? This will also delete all scheduled classes. This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                currentCourse?.let { course ->
                    val result = dbHelper.deleteCourse(course.id)
                    if (result > 0) {
                        setResult(RESULT_OK)  // Notify MainActivity to reload
                        Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error deleting course", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    private fun showDeleteClassConfirmationDialog(yogaClass: YogaClass) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete this class?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                deleteClass(yogaClass)
                dialog.dismiss()
            }
            .show()
    }
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}