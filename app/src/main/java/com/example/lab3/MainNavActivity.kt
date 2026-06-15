package com.example.lab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab3.ui.theme.Lab3Theme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Simple data model
data class Task(val id: Int, val title: String, val description: String, val priority: String, val done: Boolean = false)

class TaskViewModel : ViewModel() {
    private var nextId = 1
    var tasks = mutableStateListOf<Task>()
        private set

    init {
        // sample data
        addTask("Sample task", "Description", "Medium")
    }

    fun addTask(title: String, desc: String, priority: String) {
        tasks.add(Task(nextId++, title, desc, priority, false))
    }

    fun deleteTask(id: Int) {
        tasks.removeAll { it.id == id }
    }

    fun updateTask(updated: Task) {
        val idx = tasks.indexOfFirst { it.id == updated.id }
        if (idx >= 0) tasks[idx] = updated
    }

    fun getTask(id: Int) = tasks.find { it.id == id }
}

class MainNavActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab3Theme {
                val navController = rememberNavController()
                val vm: TaskViewModel = viewModel()
                Scaffold { innerPadding ->
                    NavHost(navController = navController, startDestination = "list", modifier = Modifier.padding(innerPadding)) {
                        composable("list") {
                            TasksListScreen(vm, onAdd = { navController.navigate("add") }, onOpen = { id -> navController.navigate("details/$id") })
                        }
                        composable("add") {
                            AddTaskScreen(onCreate = { title, desc, prio -> vm.addTask(title, desc, prio); navController.popBackStack() }, onCancel = { navController.popBackStack() })
                        }
                        composable("details/{taskId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                            if (id != null) {
                                DetailsTaskScreen(taskId = id, vm = vm, onBack = { navController.popBackStack() }, onEdit = { /* navigate to edit if implemented */ })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasksListScreen(vm: TaskViewModel, onAdd: () -> Unit, onOpen: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tasks", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onAdd) { Text("+") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(vm.tasks) { task ->
                Column(Modifier.fillMaxWidth().clickable { onOpen(task.id) }.padding(8.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text(task.priority, style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }
    }
}

@Composable
fun AddTaskScreen(onCreate: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Low") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Task", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        Spacer(Modifier.height(8.dp))
        TextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Priority:")
            Spacer(Modifier.width(8.dp))
            DropdownMenuExample(selected = priority, onSelect = { priority = it })
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = { onCreate(title, desc, priority) }) { Text("Create") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
fun DetailsTaskScreen(taskId: Int, vm: TaskViewModel, onBack: () -> Unit, onEdit: () -> Unit) {
    val task = vm.getTask(taskId)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Details", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (task == null) {
            Text("Task not found")
        } else {
            Text("Title: ${task.title}")
            Text("Description: ${task.description}")
            Text("Priority: ${task.priority}")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                var done by remember { mutableStateOf(task.done) }
                Checkbox(checked = done, onCheckedChange = { checked -> done = checked; vm.updateTask(task.copy(done = checked)) })
                Text("Done")
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = onEdit) { Text("Edit") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { vm.deleteTask(taskId); onBack() }) { Text("Delete") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}

@Composable
fun DropdownMenuExample(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Low") }, onClick = { onSelect("Low"); expanded = false })
            DropdownMenuItem(text = { Text("Medium") }, onClick = { onSelect("Medium"); expanded = false })
            DropdownMenuItem(text = { Text("High") }, onClick = { onSelect("High"); expanded = false })
        }
    }
}
