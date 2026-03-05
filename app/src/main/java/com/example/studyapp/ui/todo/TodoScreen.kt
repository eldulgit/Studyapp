package com.example.studyapp.ui.todo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



@Composable
fun TodoScreen() {

    var todos by remember { mutableStateOf(listOf<TodoItem>()) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var newTodo by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ){
                Text(
                    text = "Todo List",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isDialogOpen = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { padding ->

        // 👇 바로 여기 안에 LazyColumn이 들어감
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            itemsIndexed(todos) { index, todo ->
                TodoRow(
                    todo = todo,
                    onToggle = {
                        todos = todos.toMutableList().also {
                            it[index] = it[index].copy(
                                isDone = !todo.isDone
                            )
                        }
                    }
                )
            }
        }
    }



    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text("할 일 추가") },
            text = {
                OutlinedTextField(
                    value = newTodo,
                    onValueChange = { newTodo = it },
                    placeholder = { Text("할 일을 입력하세요") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTodo.isNotBlank()) {
                            todos = todos + TodoItem(newTodo)
                            newTodo = ""
                            isDialogOpen = false
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text("취소")
                }
            }
        )
    }

}

