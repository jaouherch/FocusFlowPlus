package com.example.focusflowplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusFlowApp()
        }
    }
}

@Composable
fun FocusFlowApp() {
    var task by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "FocusFlow+", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = task,
            onValueChange = { task = it },
            label = { Text("Enter your task") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Available time (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val timeInt = time.toIntOrNull() ?: 0
                val focus = timeInt / 2
                val breakTime = 5
                result = "Focus for $focus minutes, then take a $breakTime min break."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Plan")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = result)
    }
}