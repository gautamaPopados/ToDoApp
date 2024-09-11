import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tsu.todoapp.R
import com.tsu.todoapp.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit ) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskStatus: CheckBox = itemView.findViewById(R.id.checkBoxStatus)
        val taskDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val clickableLayout: LinearLayout = itemView.findViewById(R.id.taskLinearLayout)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(task: Task) {
            taskDescription.text = task.title
            taskStatus.isChecked = task.isCompleted

            taskStatus.setOnCheckedChangeListener { _, isCompleted ->
                task.isCompleted = isCompleted
            }

            clickableLayout.setOnClickListener {
                onEditClick(task)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}


