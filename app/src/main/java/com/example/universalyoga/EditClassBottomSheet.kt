package com.example.universalyoga
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditClassBottomSheet : BottomSheetDialogFragment() {
    private var yogaClass: YogaClass? = null
    private var courseDay: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var dbHelper: YogaDBHelper

    interface EditClassListener {
        fun onClassUpdated(yogaClass: YogaClass)
    }

    private var listener: EditClassListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the YogaClass object and course day from the arguments.
        yogaClass = arguments?.getParcelable("CLASS")
        courseDay = arguments?.getString("COURSE_DAY")
        dbHelper = YogaDBHelper(requireContext())

        // Set the selected date to the date of the existing class if it is being edited.
        yogaClass?.let { existingClass ->
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            selectedDate.time = sdf.parse(existingClass.date) ?: Date()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views and set up click listeners
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val dateInput = view.findViewById<TextInputEditText>(R.id.dateInput)
        val teacherInput = view.findViewById<TextInputEditText>(R.id.teacherInput)
        val commentsInput = view.findViewById<TextInputEditText>(R.id.commentsInput)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        // Set the title of the bottom sheet to "Edit Class" if the class is being edited.

        titleTextView.text = if (yogaClass == null) "Add New Class" else "Edit Class"
        yogaClass?.let { existingClass ->
            dateInput.setText(existingClass.date)
            teacherInput.setText(existingClass.teacher)
            commentsInput.setText(existingClass.comments)
        }

        dateInput.setOnClickListener {
            showDatePicker(dateInput)
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val teacher = teacherInput.text?.toString()
            if (teacher.isNullOrBlank()) {
                teacherInput.error = "Teacher is required"
                return@setOnClickListener
            }
            // Update the existing class with the new details and show a confirmation dialog.
            yogaClass?.let { existingClass ->
                val updatedClass = existingClass.copy(
                    date = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        .format(selectedDate.time),
                    teacher = teacher,
                    comments = commentsInput.text?.toString()
                )
                // Show a confirmation dialog before saving the updated class.
                val confirmationMessage = buildString {
                    appendLine("Please confirm the updated class details:")
                    appendLine()
                    appendLine("Date: ${updatedClass.date}")
                    appendLine("Teacher: ${updatedClass.teacher}")
                    if (!updatedClass.comments.isNullOrBlank()) {
                        appendLine("Comments: ${updatedClass.comments}")
                    }
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm Class Update")
                    .setMessage(confirmationMessage)
                    .setPositiveButton("Confirm") { _, _ ->
                        // Update the class in the database and notify the listener.
                        val result = dbHelper.updateClass(existingClass.id, updatedClass)
                        if (result > 0) {
                            listener?.onClassUpdated(updatedClass)
                        }
                        dismiss()
                    }
                    .setNegativeButton("Edit") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNeutralButton("Cancel") { _, _ ->
                        dismiss()
                    }
                    .show()
            }
        }
    }

    private fun showDatePicker(dateInput: TextInputEditText) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance()
        // Create a DateValidator to restrict the selection to the course day and future dates.
        val validator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                if (date < today) return false

                calendar.timeInMillis = date
                // Get the day of the week for the selected date.
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault())
                    .format(calendar.time)
                return courseDay.equals(dayOfWeek, ignoreCase = true) // Compare the course day with the selected day of the week.
            }

            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
            override fun describeContents(): Int = 0
        }
        // Build the constraints for the date picker.
        val constraints = CalendarConstraints.Builder()
            .setStart(today)
            .setValidator(validator)
            .build()

        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedDate.timeInMillis)
            .setCalendarConstraints(constraints)
            .build()

        datePickerDialog.addOnPositiveButtonClickListener { selection ->
            selectedDate.timeInMillis = selection
            dateInput.setText(
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    .format(selectedDate.time)
            )
        }

        datePickerDialog.show(parentFragmentManager, "datePicker")
    }

    fun setEditClassListener(listener: EditClassListener) {
        this.listener = listener
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    companion object {
        // Create a new instance of the EditClassBottomSheet with the provided YogaClass and course day.
        fun newInstance(yogaClass: YogaClass, courseDay: String) = EditClassBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelable("CLASS", yogaClass)
                putString("COURSE_DAY", courseDay)
            }
        }
    }
}