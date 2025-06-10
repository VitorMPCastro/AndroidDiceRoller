package com.example.androiddiceroller // Ensure this matches your package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // For text size
import com.example.androiddiceroller.ui.theme.AndroidDiceRollerTheme // Ensure this is your correct theme
import kotlinx.coroutines.delay // Import for delay
import kotlinx.coroutines.launch // Import for launch

class DiceRollerApp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidDiceRollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiceRollerScreen()
                }
            }
        }
    }
}

@Composable
fun DiceRollerScreen(modifier: Modifier = Modifier) {
    // State to hold the current displayed dice value
    var displayedRoll by remember { mutableStateOf(1) }
    // State to manage if the dice is currently "rolling" (animating)
    var isRolling by remember { mutableStateOf(false) }

    // Remember a CoroutineScope that is lifecycle-aware
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = displayedRoll.toString(),
            fontSize = 100.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                if (!isRolling) { // Prevent multiple clicks while rolling
                    coroutineScope.launch {
                        isRolling = true
                        val actualRoll = (1..6).random() // Determine the actual result first

                        // Animation phase: quickly change numbers
                        val animationDurationMillis = 500L // Total duration of "shuffling"
                        val changeIntervalMillis = 50L  // How often to change the number
                        var elapsedTime = 0L

                        while (elapsedTime < animationDurationMillis) {
                            displayedRoll = (1..6).random() // Show a random intermediate value
                            delay(changeIntervalMillis)
                            elapsedTime += changeIntervalMillis
                        }

                        displayedRoll = actualRoll // Set the final actual roll
                        isRolling = false
                    }
                }
            },
            enabled = !isRolling // Disable button while rolling
        ) {
            Text(text = stringResource(if (isRolling) R.string.rolling_button_text else R.string.roll_button_text))
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
fun DiceRollerScreenPreview() {
    AndroidDiceRollerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DiceRollerScreen()
        }
    }
}