package com.example.studyapp.ui.settings.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val ITEM_HEIGHT = 56.dp
private const val VISIBLE_COUNT = 3
private const val REPEAT_COUNT = 200

@Composable
fun CustomTimePicker(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val hourValues = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minuteValues = remember { (0..55 step 5).map { it.toString().padStart(2, '0') } }
    val hourItems = remember { List(hourValues.size * REPEAT_COUNT) { hourValues[it % hourValues.size] } }
    val minuteItems = remember { List(minuteValues.size * REPEAT_COUNT) { minuteValues[it % minuteValues.size] } }
    val hourMiddleBase = remember {
        val block = (REPEAT_COUNT / 2) * hourValues.size
        block - (block % hourValues.size)
    }
    val minuteMiddleBase = remember {
        val block = (REPEAT_COUNT / 2) * minuteValues.size
        block - (block % minuteValues.size)
    }
    val initialHourIndex = remember(initialHour) { hourMiddleBase + initialHour.coerceIn(0, 23) }
    val normalizedMinute = (initialMinute / 5) * 5
    val initialMinuteIndex = remember(initialMinute) {
        minuteMiddleBase + normalizedMinute.coerceIn(0, 55) / 5
    }

    val hourListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialHourIndex
    )
    val minuteListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialMinuteIndex
    )

    val hourFlingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState)
    val minuteFlingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val current = hourListState.firstVisibleItemIndex % hourValues.size
            val target = hourMiddleBase + current
            if (hourListState.firstVisibleItemIndex != target) {
                hourListState.scrollToItem(target)
            }
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val current = minuteListState.firstVisibleItemIndex % minuteValues.size
            val target = minuteMiddleBase + current
            if (minuteListState.firstVisibleItemIndex != target) {
                minuteListState.scrollToItem(target)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .width(320.dp)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT * VISIBLE_COUNT),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(ITEM_HEIGHT * VISIBLE_COUNT),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.height(ITEM_HEIGHT - 2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                        }

                        LazyColumn(
                            state = hourListState,
                            flingBehavior = hourFlingBehavior,
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(hourItems.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(ITEM_HEIGHT)
                                        .clickable {
                                            coroutineScope.launch {
                                                hourListState.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hourItems[index],
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(ITEM_HEIGHT * VISIBLE_COUNT),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(modifier = Modifier.height(ITEM_HEIGHT - 2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                        }

                        LazyColumn(
                            state = minuteListState,
                            flingBehavior = minuteFlingBehavior,
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(minuteItems.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(ITEM_HEIGHT)
                                        .clickable {
                                            coroutineScope.launch {
                                                minuteListState.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = minuteItems[index],
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            val selectedHour = hourListState.firstVisibleItemIndex % hourValues.size
                            val selectedMinute = minuteListState.firstVisibleItemIndex % minuteValues.size
                            onConfirm(selectedHour, selectedMinute)
                        }
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}