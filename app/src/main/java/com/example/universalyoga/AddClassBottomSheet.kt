package com.example.universalyoga
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddClassBottomSheet : BottomSheetDialogFragment() {

    private var courseId: Long = -1
    private var courseDay: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private var listener: AddClassListener? = null

    interface AddClassListener {
        fun onClassAdded(yogaClass: YogaClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseId = arguments?.getLong(ARG_COURSE_ID) ?: -1
        courseDay = arguments?.getString(ARG_COURSE_DAY)
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
        val dateInput = view.findViewById<TextInputEditText>(R.id.dateInput)
        val teacherInput = view.findViewById<TextInputEditText>(R.id.teacherInput)
        val commentsInput = view.findViewById<TextInputEditText>(R.id.commentsInput)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        dateInput.setOnClickListener {
            showDatePicker(dateInput)
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            if (dateInput.text.isNullOrBlank()) {
                dateInput.error = "Date is required"
                return@setOnClickListener
            }

            val teacher = teacherInput.text?.toString()
            if (teacher.isNullOrBlank()) {
                teacherInput.error = "Teacher is required"
                return@setOnClickListener
            }

            // Create class object for confirmation
            val newClass = YogaClass(
                id = System.currentTimeMillis(),
                courseId = courseId,
                date = dateInput.text.toString(),
                teacher = teacher,
                comments = commentsInput.text?.toString()
            )

            showConfirmationDialog(newClass)
        }
    }

    // Show a confirmation dialog before saving the class
    private fun showConfirmationDialog(yogaClass: YogaClass) {
        val confirmationMessage = buildString {
            appendLine("Please confirm the class details:")
            appendLine()
            appendLine("Date: ${yogaClass.date}")
            appendLine("Teacher: ${yogaClass.teacher}")
            if (!yogaClass.comments.isNullOrBlank()) {
                appendLine("Comments: ${yogaClass.comments}")
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Class Details")
            .setMessage(confirmationMessage)
            .setPositiveButton("Confirm") { _, _ ->
                // Save the class
                listener?.onClassAdded(yogaClass)
                dismiss()
            }
            .setNegativeButton("Edit") { dialog, _ ->
                // Just close the dialog and return to editing
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { _, _ ->
                // Cancel the whole operation
                dismiss()
            }
            .show()
    }
    // Show the date picker dialog
    private fun showDatePicker(dateInput: TextInputEditText) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance()

        // Create a date validator that only allows dates in the future and on the correct day of the week
        val validator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                if (date < today) return false

                calendar.timeInMillis = date
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault())
                    .format(calendar.time)
                return courseDay.equals(dayOfWeek, ignoreCase = true)
            }

            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
            override fun describeContents(): Int = 0
        }

        // Set the constraints for the date picker
        val constraints = CalendarConstraints.Builder()
            .setStart(today)
            .setValidator(validator)
            .build()

        // Create and show the date picker dialog
        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(selectedDate.timeInMillis)
            .setCalendarConstraints(constraints)
            .build()

        // Update the selected date when the user picks a date
        datePickerDialog.addOnPositiveButtonClickListener { selection ->
            selectedDate.timeInMillis = selection
            dateInput.setText(
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                    .format(selectedDate.time)
            )
        }

        datePickerDialog.show(parentFragmentManager, "datePicker")
    }

    fun setAddClassListener(listener: AddClassListener) {
        this.listener = listener
        Log.d("AddClassBottomSheet", "Listener set")
    }

    // Companion object to create a new instance of the bottom sheet
    companion object {
        private const val ARG_COURSE_ID = "course_id"
        private const val ARG_COURSE_DAY = "course_day"

        // Create a new instance of the bottom sheet with the course ID and day of the week
        fun newInstance(courseId: Long, courseDay: String) = AddClassBottomSheet().apply {
            arguments = Bundle().apply {
                putLong(ARG_COURSE_ID, courseId)
                putString(ARG_COURSE_DAY, courseDay)
            }
        }
    }
}