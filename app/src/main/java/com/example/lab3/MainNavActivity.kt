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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab3.ui.theme.Lab3Theme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab3.data.DatabaseProvider
import com.example.lab3.data.repository.TaskRepository
import com.example.lab3.viewmodel.TaskViewModel
import com.example.lab3.viewmodel.TaskViewModelFactory


class MainNavActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab3Theme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val database = DatabaseProvider.getDatabase(context)
                val repository = TaskRepository(database.taskDao())
                val vm: TaskViewModel = viewModel(factory = TaskViewModelFactory(repository))
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
                                DetailsTaskScreen(taskId = id, vm = vm, onBack = { navController.popBackStack() }, onEdit = { navController.navigate("edit/$id") })
                            }
                        }
                        composable("edit/{taskId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                            if (id != null) {
                                EditTaskScreen(taskId = id, vm = vm, onUpdate = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
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
    val items by vm.items.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Tasks", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onAdd) { Text("+ Add Task") }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(items) { task ->
                Column(Modifier.fillMaxWidth().clickable { onOpen(task.id) }.padding(12.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text("Priority: ${task.priority}", style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
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
        Text("Add New Task", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Priority:", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(16.dp))
            DropdownMenuExample(selected = priority, onSelect = { priority = it })
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (title.isNotBlank()) onCreate(title, desc, priority) }) { Text("Create") }
        }
    }
}

@Composable
fun EditTaskScreen(taskId: Int, vm: TaskViewModel, onUpdate: () -> Unit, onCancel: () -> Unit) {
    val task by vm.getTaskById(taskId).collectAsState(initial = null)
    
    task?.let { t ->
        var title by remember { mutableStateOf(t.title) }
        var desc by remember { mutableStateOf(t.description) }
        var priority by remember { mutableStateOf(t.priority) }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Edit Task", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Priority:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(16.dp))
                DropdownMenuExample(selected = priority, onSelect = { priority = it })
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { 
                    if (title.isNotBlank()) {
                        vm.updateTask(t.copy(title = title, description = desc, priority = priority))
                        onUpdate()
                    }
                }) { Text("Save Changes") }
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun DetailsTaskScreen(taskId: Int, vm: TaskViewModel, onBack: () -> Unit, onEdit: () -> Unit) {
    val task by vm.getTaskById(taskId).collectAsState(initial = null)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Task Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        task?.let { t ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Title: ${t.title}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Description: ${t.description}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Priority: ${t.priority}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = t.isCompleted, onCheckedChange = { checked -> vm.updateTask(t.copy(isCompleted = checked)) })
                        Text("Completed", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onEdit) { Text("Edit") }
                Button(onClick = { vm.deleteTask(t); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        } ?: Text("Task not found")
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
