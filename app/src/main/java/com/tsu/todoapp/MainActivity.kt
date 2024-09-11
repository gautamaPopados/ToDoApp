package com.tsu.todoapp

import TaskAdapter
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var tasks: MutableList<Task>
    private val mapper = jacksonObjectMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tasks = mutableListOf()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTasks)
        val editTextTask: EditText = findViewById(R.id.editTextTask)
        val buttonAddTask: Button = findViewById(R.id.buttonAddTask)

        taskAdapter = TaskAdapter(tasks, onEditClick = { task ->editTask(task)}, onDeleteClick = { task -> deleteTask(task)})

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        buttonAddTask.setOnClickListener {
            val taskDescription = editTextTask.text.toString()
            if (taskDescription.isNotEmpty()) {
                tasks.add(Task(taskDescription, false))
                taskAdapter.notifyItemInserted(taskAdapter.itemCount + 1)
                editTextTask.text.clear()
                updateTextVisibility()
            }
        }
    }

    private fun updateTextVisibility() {
        val textView: TextView = findViewById(R.id.textView)
        if (taskAdapter.itemCount > 0) {
            textView.visibility = View.INVISIBLE
        }
        else {
            textView.visibility = View.VISIBLE
        }
    }

    private fun editTask(task: Task) {
        val editText = EditText(this)
        editText.setText(task.title)

        AlertDialog.Builder(this)
            .setTitle("Редактировать дело")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                task.title = editText.text.toString()
                taskAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Отменить", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        tasks.remove(task)
        taskAdapter.notifyDataSetChanged()
        updateTextVisibility()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                saveTasks()
                true
            }
            R.id.menu_load -> {
                loadTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { saveTasksToUri(it) }
    }

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { loadTasksFromUri(it) }
    }

    private fun saveTasks() {
        createFileLauncher.launch("delaa.json")
    }

    private fun loadTasks() {
        openFileLauncher.launch(arrayOf("application/json"))
    }

    private fun saveTasksToUri(uri: Uri) {
        val jsonArray = mapper.writeValueAsString(tasks)

        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonArray.toByteArray())
                Toast.makeText(this, "Успешно сохранено", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTasksFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }

                val jsonArray = stringBuilder.toString()

                val taskArray: MutableList<Task> = mapper.readValue(jsonArray)
                tasks.clear()
                tasks.addAll(taskArray)
                taskAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Успешно загружено", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show()
        }
        updateTextVisibility()
    }

}