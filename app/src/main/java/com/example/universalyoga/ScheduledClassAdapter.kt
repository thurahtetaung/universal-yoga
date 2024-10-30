package com.example.universalyoga

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduledClassAdapter(
    private var classes: List<YogaClass>,
    private val onOptionsClick: (View, YogaClass) -> Unit
) : RecyclerView.Adapter<ScheduledClassAdapter.ClassViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val teacherText: TextView = itemView.findViewById(R.id.teacherText)
        private val commentsText: TextView = itemView.findViewById(R.id.commentsText)
        val optionsButton: ImageButton = itemView.findViewById(R.id.classOptionsButton)

        fun bind(yogaClass: YogaClass) {
            dateText.text = yogaClass.date
            teacherText.text = "Teacher: ${yogaClass.teacher}"
            commentsText.text = yogaClass.comments
            commentsText.visibility = if (yogaClass.comments.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scheduled_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val yogaClass = classes[position]
        holder.bind(yogaClass)
        holder.optionsButton.setOnClickListener { view ->
            onOptionsClick(view, yogaClass)
        }
    }

    override fun getItemCount() = classes.size

    fun updateClasses(newClasses: List<YogaClass>) {
        classes = newClasses.sortedBy {
            dateFormat.parse(it.date)?.time ?: 0L
        }
        notifyDataSetChanged()
    }
}