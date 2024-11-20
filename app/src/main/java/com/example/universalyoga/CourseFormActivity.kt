package com.example.universalyoga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import android.app.TimePickerDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale



class CourseFormActivity : AppCompatActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var typeInput: AutoCompleteTextView
    private lateinit var dayInput: AutoCompleteTextView
    private lateinit var timeInput: TextInputEditText
    private lateinit var durationInput: TextInputEditText
    private lateinit var capacityInput: TextInputEditText
    private lateinit var priceInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText

    private var courseToEdit: YogaCourse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_form)

        initializeViews()
        setupToolbar() // Setup the toolbar
        setupDropdowns() // Setup dropdowns for course type and day
        setupTimePicker() // Setup time picker for course time

        courseToEdit = intent.getParcelableExtra("COURSE_TO_EDIT")
        courseToEdit?.let {
            populateFields(it) // Populate fields if editing an existing course
            supportActionBar?.title = "Edit Course"
        }
    }

    private fun initializeViews() {
        topAppBar = findViewById(R.id.topAppBar)
        typeInput = findViewById(R.id.typeInput)
        dayInput = findViewById(R.id.dayInput)
        timeInput = findViewById(R.id.timeInput)
        durationInput = findViewById(R.id.durationInput)
        capacityInput = findViewById(R.id.capacityInput)
        priceInput = findViewById(R.id.priceInput)
        descriptionInput = findViewById(R.id.descriptionInput)
    }

    private fun setupToolbar() {
        setSupportActionBar(topAppBar) // Set the toolbar as the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title of the action bar based on whether adding a new course or editing an existing one
        supportActionBar?.title = if (courseToEdit != null) "Edit Course" else "Add New Course"

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDropdowns() {
        // Setup course types dropdown
        val types = arrayOf("Flow Yoga", "Aerial Yoga", "Family Yoga")
        val typeAdapter = ArrayAdapter(this, R.layout.list_item, types)
        typeInput.setAdapter(typeAdapter)

        // Setup days dropdown
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayAdapter = ArrayAdapter(this, R.layout.list_item, days)
        dayInput.setAdapter(dayAdapter)
    }

    private fun setupTimePicker() {
        timeInput.setOnClickListener {
            val calendar = Calendar.getInstance()

            // If editing and have an existing time, parse it and set the calendar to that time
            if (!timeInput.text.isNullOrEmpty()) {
                try {
                    val existingTime = timeInput.text.toString()
                    val parseFormat = if (existingTime.contains("AM") || existingTime.contains("PM")) {
                        SimpleDateFormat("h:mm a", Locale.getDefault())
                    } else {
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                    }
                    val date = parseFormat.parse(existingTime)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // If parsing fails, use current time
                    Log.e("CourseFormActivity", "Error parsing time: ${e.message}")
                }
            }

            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    // Convert to 12-hour format
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val timeString = timeFormat.format(calendar.time)
                    timeInput.setText(timeString)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false  // false = 12 hour format
            ).show()
        }
    }

    // Populate fields with existing course details when editing
    private fun populateFields(course: YogaCourse) {
        typeInput.setText(course.type, false)  // false prevents dropdown from showing
        dayInput.setText(course.dayOfWeek, false)
        timeInput.setText(course.timeOfDay)
        durationInput.setText(course.duration.toString())
        capacityInput.setText(course.capacity.toString())
        priceInput.setText(course.price.toString())
        descriptionInput.setText(course.description ?: "")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.form_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                if (validateForm()) {
                    saveCourse()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Validate the form fields
    private fun validateForm(): Boolean {
        var isValid = true

        if (typeInput.text.isNullOrBlank()) {
            typeInput.error = "Type is required"
            isValid = false
        }

        if (dayInput.text.isNullOrBlank()) {
            dayInput.error = "Day is required"
            isValid = false
        }

        if (timeInput.text.isNullOrBlank()) {
            timeInput.error = "Time is required"
            isValid = false
        }

        if (durationInput.text.isNullOrBlank()) {
            durationInput.error = "Duration is required"
            isValid = false
        }

        if (capacityInput.text.isNullOrBlank()) {
            capacityInput.error = "Capacity is required"
            isValid = false
        }

        if (priceInput.text.isNullOrBlank()) {
            priceInput.error = "Price is required"
            isValid = false
        }

        return isValid
    }

    // Save the course details and return to the previous activity
    private fun saveCourse() {
        try {
            val newCourse = YogaCourse(
                id = courseToEdit?.id ?: System.currentTimeMillis(),
                type = typeInput.text.toString(),
                dayOfWeek = dayInput.text.toString(),
                timeOfDay = timeInput.text.toString(),
                duration = durationInput.text.toString().toInt(),
                capacity = capacityInput.text.toString().toInt(),
                price = priceInput.text.toString().toDouble(),
                description = descriptionInput.text.toString().takeIf { it.isNotBlank() }
            )

            // title of the confirmation dialog based on whether editing or adding a new course
            val title = if (courseToEdit != null) {
                "Confirm Course Update"
            } else {
                "Confirm New Course"
            }

            // Build the confirmation message based on the course details and whether editing or adding
            val confirmationMessage = buildString {
                appendLine("Please confirm the ${if (courseToEdit != null) "updated" else "new"} course details:")
                appendLine()
                // Append original and new course details if editing to show the changes to the user
                if (courseToEdit != null) {
                    appendLine("Original Type: ${courseToEdit?.type}")
                    appendLine("New Type: ${newCourse.type}")
                    appendLine()
                    appendLine("Original Day: ${courseToEdit?.dayOfWeek}")
                    appendLine("New Day: ${newCourse.dayOfWeek}")
                    appendLine()
                    appendLine("Original Time: ${courseToEdit?.timeOfDay}")
                    appendLine("New Time: ${newCourse.timeOfDay}")
                    appendLine()
                    appendLine("Original Duration: ${courseToEdit?.duration} minutes")
                    appendLine("New Duration: ${newCourse.duration} minutes")
                    appendLine()
                    appendLine("Original Capacity: ${courseToEdit?.capacity} people")
                    appendLine("New Capacity: ${newCourse.capacity} people")
                    appendLine()
                    appendLine("Original Price: £${String.format("%.2f", courseToEdit?.price)}")
                    appendLine("New Price: £${String.format("%.2f", newCourse.price)}")

                    if (courseToEdit?.description != newCourse.description) {
                        appendLine()
                        appendLine("Original Description: ${courseToEdit?.description ?: "None"}")
                        appendLine("New Description: ${newCourse.description ?: "None"}")
                    }
                } else {
                    appendLine("Type: ${newCourse.type}")
                    appendLine("Day: ${newCourse.dayOfWeek}")
                    appendLine("Time: ${newCourse.timeOfDay}")
                    appendLine("Duration: ${newCourse.duration} minutes")
                    appendLine("Capacity: ${newCourse.capacity} people")
                    appendLine("Price: £${String.format("%.2f", newCourse.price)}")
                    if (!newCourse.description.isNullOrBlank()) {
                        appendLine("Description: ${newCourse.description}")
                    }
                }
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(confirmationMessage)
                .setPositiveButton("Confirm") { _, _ ->
                    val resultIntent = Intent().apply {
                        putExtra("COURSE", newCourse)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .setNegativeButton("Edit") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Cancel") { _, _ ->
                    // Do nothing, just close the dialog
                }
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving course: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}