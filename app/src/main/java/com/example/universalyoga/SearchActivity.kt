package com.example.universalyoga

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    private lateinit var dbHelper: YogaDBHelper
    private lateinit var searchToolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var teacherSearchLayout: TextInputLayout
    private lateinit var dateSearchLayout: TextInputLayout
    private lateinit var daySearchLayout: TextInputLayout
    private lateinit var teacherSearchInput: TextInputEditText
    private lateinit var dateSearchInput: TextInputEditText
    private lateinit var daySearchInput: AutoCompleteTextView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        dbHelper = YogaDBHelper(this)
        initializeViews()
        setupToolbar()
        setupTabLayout()
        setupSearchInputs()
        setupRecyclerView()
    }

    private fun initializeViews() {
        searchToolbar = findViewById(R.id.searchToolbar)
        tabLayout = findViewById(R.id.searchTabLayout)
        teacherSearchLayout = findViewById(R.id.teacherSearchLayout)
        dateSearchLayout = findViewById(R.id.dateSearchLayout)
        daySearchLayout = findViewById(R.id.daySearchLayout)
        teacherSearchInput = findViewById(R.id.teacherSearchInput)
        dateSearchInput = findViewById(R.id.dateSearchInput)
        daySearchInput = findViewById(R.id.daySearchInput)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
    }

    private fun setupToolbar() {
        setSupportActionBar(searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        searchToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showTeacherSearch()
                    1 -> showDateSearch()
                    2 -> showDaySearch()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchInputs() {
        // Teacher search
        teacherSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 >= 2) { // Search after 2 characters
                    performTeacherSearch(s.toString())
                }
            }
        })

        // Date search
        dateSearchInput.setOnClickListener {
            showDatePicker()
        }

        // Day search
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayAdapter = ArrayAdapter(this, R.layout.list_item, days)
        daySearchInput.setAdapter(dayAdapter)
        daySearchInput.setOnItemClickListener { _, _, _, _ ->
            performDaySearch(daySearchInput.text.toString())
        }
    }

    private fun setupRecyclerView() {
        searchResultsAdapter = SearchResultAdapter { searchResult ->
            val course = dbHelper.getCourseById(searchResult.courseId)
            if (course != null) {
                val intent = Intent(this, CourseDetailActivity::class.java).apply {
                    putExtra("COURSE", course)
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Error: Course not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchResultsAdapter
        }
    }

    private fun showTeacherSearch() {
        teacherSearchLayout.visibility = View.VISIBLE
        dateSearchLayout.visibility = View.GONE
        daySearchLayout.visibility = View.GONE
        clearSearchResults()
    }

    private fun showDateSearch() {
        teacherSearchLayout.visibility = View.GONE
        dateSearchLayout.visibility = View.VISIBLE
        daySearchLayout.visibility = View.GONE
        clearSearchResults()
    }

    private fun showDaySearch() {
        teacherSearchLayout.visibility = View.GONE
        dateSearchLayout.visibility = View.GONE
        daySearchLayout.visibility = View.VISIBLE
        clearSearchResults()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            dateSearchInput.setText(formattedDate)
            performDateSearch(formattedDate)
        }

        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun performTeacherSearch(query: String) {
        val results = dbHelper.searchClassesByTeacher(query)
        searchResultsAdapter.submitList(results)
    }

    private fun performDateSearch(date: String) {
        val results = dbHelper.searchClassesByDate(date)
        searchResultsAdapter.submitList(results)
    }

    private fun performDaySearch(day: String) {
        val results = dbHelper.searchClassesByDayOfWeek(day)
        searchResultsAdapter.submitList(results)
    }

    private fun clearSearchResults() {
        searchResultsAdapter.submitList(emptyList())
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}