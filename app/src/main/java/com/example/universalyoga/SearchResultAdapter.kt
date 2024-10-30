package com.example.universalyoga

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SearchResultAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(SearchResultDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val courseTypeText: TextView = view.findViewById(R.id.courseTypeText)
        private val classDateText: TextView = view.findViewById(R.id.classDateText)
        private val teacherNameText: TextView = view.findViewById(R.id.teacherNameText)
        private val scheduleText: TextView = view.findViewById(R.id.scheduleText)

        fun bind(result: SearchResult) {
            courseTypeText.text = result.courseType
            classDateText.text = result.classDate
            teacherNameText.text = "Teacher: ${result.teacherName}"
            scheduleText.text = "${result.courseDay}s at ${result.courseTime}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result)
        holder.itemView.setOnClickListener { onItemClick(result) }
    }
}

private class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem.classId == newItem.classId
    }

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
        return oldItem == newItem
    }
}