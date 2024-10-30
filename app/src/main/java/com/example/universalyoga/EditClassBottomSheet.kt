package com.example.universalyoga
import android.os.Bundle
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
        yogaClass = arguments?.getParcelable("CLASS")
        courseDay = arguments?.getString("COURSE_DAY")
        dbHelper = YogaDBHelper(requireContext())

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

        val dateInput = view.findViewById<TextInputEditText>(R.id.dateInput)
        val teacherInput = view.findViewById<TextInputEditText>(R.id.teacherInput)
        val commentsInput = view.findViewById<TextInputEditText>(R.id.commentsInput)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

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

            yogaClass?.let { existingClass ->
                val updatedClass = existingClass.copy(
                    date = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        .format(selectedDate.time),
                    teacher = teacher,
                    comments = commentsInput.text?.toString()
                )

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
        fun newInstance(yogaClass: YogaClass, courseDay: String) = EditClassBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelable("CLASS", yogaClass)
                putString("COURSE_DAY", courseDay)
            }
        }
    }
}