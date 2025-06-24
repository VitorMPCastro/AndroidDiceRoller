package com.example.androiddiceroller

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDiceRollScreen(
    navController: NavController,
    diceViewModel: DiceViewModel
) {
    val availableDice by diceViewModel.availableDice.collectAsState()
    val context = LocalContext.current

    val dieQuantities = remember { mutableStateMapOf<Die, Int>() }
    var rollResults by remember { mutableStateOf<Map<Die, List<Int>>>(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi Dice Roller") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Select dice and quantities to roll:")

            availableDice.forEach { die ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = die.acronym, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = (dieQuantities[die] ?: 0).toString(),
                        onValueChange = {
                            val qty = it.toIntOrNull() ?: 0
                            dieQuantities[die] = qty.coerceAtLeast(0)
                        },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Qty") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val results = mutableMapOf<Die, List<Int>>()
                    dieQuantities.forEach { (die, qty) ->
                        if (qty > 0) {
                            val rolls = List(qty) { (1..die.sides).random() }
                            results[die] = rolls
                        }
                    }
                    rollResults = results
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Roll All")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (rollResults.isNotEmpty()) {
                Text("Results:", style = MaterialTheme.typography.titleMedium)
                rollResults.forEach { (die, results) ->
                    Text("${die.acronym} (${results.size}x): ${results.joinToString(", ")}")
                }
                val total = rollResults.values.flatten().sum()
                Text("Total: $total", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}
