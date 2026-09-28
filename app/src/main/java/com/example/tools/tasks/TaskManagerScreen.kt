package com.example.tools.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.TaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val tasks by db.taskDao().getAllTasks().collectAsState(initial = emptyList())

    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, TODO, IN_PROGRESS, COMPLETED
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dialog fields
    var taskTitle by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Design") }
    var taskPriority by remember { mutableStateOf("High") }
    var taskAssignee by remember { mutableStateOf("Priya S.") }
    var taskDueDate by remember { mutableStateOf("Today, 6:00 PM") }

    val completedCount = tasks.count { it.status == "COMPLETED" }
    val progressPercent = if (tasks.isNotEmpty()) (completedCount.toFloat() / tasks.size) else 0f

    val filteredTasks = when (selectedStatusFilter) {
        "TODO" -> tasks.filter { it.status == "TODO" }
        "IN_PROGRESS" -> tasks.filter { it.status == "IN_PROGRESS" }
        "COMPLETED" -> tasks.filter { it.status == "COMPLETED" }
        else -> tasks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Timeline & Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTaskDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Task") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Project Timeline & Sprint Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sprint Release v1.0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Team Milestones & Timeline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Text("${(progressPercent * 100).toInt()}% Done", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${tasks.size} Tasks", fontSize = 12.sp)
                        Text("Completed: $completedCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Filter status tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "TODO", "IN_PROGRESS", "COMPLETED").forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = {
                            Text(
                                when (status) {
                                    "ALL" -> "All (${tasks.size})"
                                    "TODO" -> "To Do"
                                    "IN_PROGRESS" -> "In Progress"
                                    else -> "Completed"
                                },
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tasks in this category", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val isDone = task.status == "COMPLETED"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isDone,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            db.taskDao().updateTask(
                                                task.copy(status = if (checked) "COMPLETED" else "TODO")
                                            )
                                        }
                                    }
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isDone) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Priority badge
                                        val (pBg, pColor) = when (task.priority) {
                                            "Urgent" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
                                            "High" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                                            "Medium" -> Color(0xFFFFFDE7) to Color(0xFFF57F17)
                                            else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(pBg, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(task.priority, color = pColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Assignee initial avatar
                                        if (task.teamMember.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    task.teamMember.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(task.teamMember, fontSize = 11.sp, color = Color.Gray)
                                        }

                                        if (task.dueDate.isNotBlank()) {
                                            Text("• ${task.dueDate}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            db.taskDao().deleteTask(task)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Create New Project Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taskAssignee,
                            onValueChange = { taskAssignee = it },
                            label = { Text("Assignee") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = taskDueDate,
                            onValueChange = { taskDueDate = it },
                            label = { Text("Due Date") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Priority selection
                    Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Low", "Medium", "High", "Urgent").forEach { p ->
                            FilterChip(
                                selected = taskPriority == p,
                                onClick = { taskPriority = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            coroutineScope.launch {
                                db.taskDao().insertTask(
                                    TaskEntity(
                                        title = taskTitle,
                                        teamMember = taskAssignee,
                                        priority = taskPriority,
                                        dueDate = taskDueDate,
                                        status = "TODO"
                                    )
                                )
                                taskTitle = ""
                                showAddTaskDialog = false
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
