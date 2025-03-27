package com.mauleco.todo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauleco.todo.models.Todo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen() // Calls the composable function that builds your UI
        }
    }

    @Composable
    fun MainScreen() {
        val todos by viewModel.todos.collectAsState()

        // Full screen container with black background
        Column(
            modifier = Modifier
                .fillMaxSize()  // Takes up entire screen
                .background(Color.Black) // Set background to black
        ) {
            // Header Section (DATE, TIME)
            HeaderSection()

            //Key section
            TaskOverviewSection()

            //Total and clear section
            TwoColumnSection(todos)

            //Lazy list and add task button
            TaskListSection(todoList = todos)
        }
    }
    @Composable
    fun TaskListSection(todoList: List<Todo>) {
        var showDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Full-screen background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp) // Prevents overlap with the button
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todoList) { todo ->
                        ListItem(todo = todo)
                    }
                }
            }

            // Anchored button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Fixes the button to the bottom
                    .padding(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Green)
                    .clickable { showDialog = true } // Open dialog on click
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add Task",
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.vt323)),
                    color = Color.Black
                )
            }
        }

        // Call the AddTaskDialog when the button is clicked
        AddTaskDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false }
        )
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddTaskDialog(showDialog: Boolean, onDismiss: () -> Unit) {
        var newTaskText by remember { mutableStateOf("") }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text("New Task", color = Color.White) },
                text = {
                    TextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        label = { Text("Enter task") },
                        textStyle = TextStyle(color = Color.White)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                viewModel.addTask(newTaskText)
                                newTaskText = "" // Reset text field
                                onDismiss() // Close dialog
                            } else {
                                Toast.makeText(this, "Please enter task text", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.Blue
            )
        }
    }
    @Composable
    fun HeaderSection() {
        val time = remember { mutableStateOf(getCurrentTime()) }
        val date = remember { mutableStateOf(getCurrentDate()) }

        // Keep updating time and date every second
        LaunchedEffect(Unit) {
            while (true) {
                time.value = getCurrentTime()
                date.value = getCurrentDate()
                delay(1000L) // Update every second
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Image (30% width)
            Image(
                painter = painterResource(id = R.drawable.maule_logo), // Replace with your image resource
                contentDescription = "Header Image",
                modifier = Modifier
                    .weight(0.3f) // 30% width
                    .aspectRatio(4f)
            )

            // Right: Date and Time Display (70% width), aligned to the right
            Row(
                modifier = Modifier
                    .weight(0.7f) // 70% width
                    .fillMaxWidth() // Push text to the end
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.End // Align text to the right
            ) {
                // Display the date
                Text(
                    text = date.value,
                    fontFamily = FontFamily(Font(R.font.vt323)),
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.End,
                )

                Spacer(modifier = Modifier.width(8.dp)) // Spacer between date and time

                // Display the time
                Text(
                    text = time.value,
                    fontFamily = FontFamily(Font(R.font.vt323)),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
    @Composable
    fun TaskOverviewSection() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Today's Tasks" Title - As large as possible in one line
            TaskTitleWithShadow(text = "TODAY'S TASKS")

            Spacer(modifier = Modifier.height(16.dp)) // Space below title

            // Row with 3 columns
            Row(modifier = Modifier.fillMaxWidth()) {
                TaskColumn("Aborted", Color.Red, Modifier.weight(1f))
                TaskColumn("Underway", Color.Yellow, Modifier.weight(1f))
                TaskColumn("Completed", Color.Green, Modifier.weight(1f))
            }
        }
    }
    @Composable
    fun TaskTitleWithShadow(text: String, modifier: Modifier = Modifier) {
        val fontSize by remember { mutableStateOf(48.sp) }

        Box(modifier = modifier) {
            // Shadow layer (black)
            Text(
                text = text,
                fontFamily = FontFamily(Font(R.font.vt323)),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, // Added letter spacing
                color = Color.Black, // Shadow color
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .offset(0.dp, 4.dp) // Offset to create a solid shadow effect
            )

            // Foreground layer (white text)
            Text(
                text = text,
                fontFamily = FontFamily(Font(R.font.vt323)),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, // Added letter spacing
                color = Color.Green, // Main text color
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    @Composable
    fun TwoColumnSection(todos: List<Todo>) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Overall padding for the Row
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Space between columns
            verticalAlignment = Alignment.CenterVertically // Vertically center content in both columns
        ) {
            // First Column: Text centered in the column
            Box(
                modifier = Modifier
                    .weight(0.5f) // 50% width
                    .wrapContentHeight() // Height depends on the content
            ) {
                Text(
                    text = "${todos.size} Total task(s)", // Replace with dynamic text from viewModel if necessary
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.vt323)), // Custom font
                    color = Color.White, // White color for the text
                    textAlign = TextAlign.Center // Ensure text is centered horizontally
                )
            }

            // Second Column: Clickable Box with red background and rounded corners
            Box(
                modifier = Modifier
                    .weight(0.5f) // 50% width
                    .wrapContentHeight() // Height depends on the content
                    .clickable {
                        viewModel.clearAllTodos()
                    }
                    .background(
                        Color.Red,
                        shape = RoundedCornerShape(2.dp)
                    ) // Red background with rounded corners
                    .padding(16.dp), // Padding inside the Box
                contentAlignment = Alignment.Center // Center the content inside the Box
            ) {
                Text(
                    text = "Clear all", // Text inside the clickable Box
                    color = Color.White, // White text color
                    fontFamily = FontFamily(Font(R.font.vt323)), // Custom font
                    fontSize = 18.sp // Optional: Adjust the font size for better text alignment
                )
            }
        }
    }
    @Composable
    fun TaskColumn(label: String, barColor: Color, modifier: Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Colored bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.height(8.dp)) // Space between bar and text
            // Label text
            Text(
                text = label,
                fontFamily = FontFamily(Font(R.font.vt323)),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp),
            )
        }
    }
    @Composable
    fun ListItem(todo: Todo) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .border(2.dp, Color.White, RoundedCornerShape(2.dp)) // White border
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min), // Ensures left box expands dynamically
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Section: Yellow Box with the Number
                Box(
                    modifier = Modifier
                        .requiredWidthIn(min = 40.dp) // Min width 40dp, expands if needed
                        .fillMaxHeight() // Makes sure it takes the full height of the row
                        .background(getStatus(todo.status)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = todo.itemNumber.toString(),
                        fontFamily = FontFamily(Font(R.font.vt323)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp) // Padding inside the yellow box
                    )
                }

                // Right Section: Expanding Message
                Text(
                    text = todo.note,
                    fontFamily = FontFamily(Font(R.font.vt323)),
                    modifier = Modifier
                        .padding(8.dp) // Spacing inside the right section
                        .weight(1f), // Allows text to take all remaining space
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }

}

private fun getStatus(status : Boolean?) : Color {
    return when (status) {
        false -> {
            Color.Red
        }
        true -> {
            Color.Green
        }
        else -> {
            Color.Yellow
        }
    }
}
private fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}
private fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault()) // Format for "Mar 18"
    return formatter.format(Date())
}
