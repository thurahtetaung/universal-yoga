import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.universalyoga.R
import com.example.universalyoga.YogaCourse

class YogaCourseAdapter(
    private val courses: List<YogaCourse>,
    private val onCourseClick: (YogaCourse) -> Unit
) : RecyclerView.Adapter<YogaCourseAdapter.YogaCourseViewHolder>() {

    class YogaCourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseType: TextView = itemView.findViewById(R.id.courseType)
        private val scheduleText: TextView = itemView.findViewById(R.id.scheduleText)
        private val capacityText: TextView = itemView.findViewById(R.id.capacityText)

        fun bind(yogaCourse: YogaCourse) {
            courseType.text = yogaCourse.type
            scheduleText.text = "${yogaCourse.dayOfWeek} at ${yogaCourse.timeOfDay}"
            capacityText.text = "Capacity: ${yogaCourse.capacity} people"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YogaCourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_yoga_course, parent, false)  // we'll update layout name later
        return YogaCourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: YogaCourseViewHolder, position: Int) {
        val yogaCourse = courses[position]
        holder.bind(yogaCourse)
        holder.itemView.setOnClickListener { onCourseClick(yogaCourse) }
    }

    override fun getItemCount() = courses.size
}