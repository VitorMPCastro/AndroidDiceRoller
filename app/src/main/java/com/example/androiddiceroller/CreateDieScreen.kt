// In CreateDieScreen.kt or DiceRollerApp.kt
package com.example.androiddiceroller // Ensure this matches your package

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.androiddiceroller.ui.theme.AndroidDiceRollerTheme // Your app's theme

@SuppressLint("StringFormatInvalid")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDieScreen(
    navController: NavController,
    diceViewModel: DiceViewModel
) {
    var dieName by remember { mutableStateOf("") }
    var dieSides by remember { mutableStateOf("") }
    var dieAcronym by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_custom_die_title)) }, // Add to strings.xml: <string name="create_custom_die_title">Create Custom Die</string>
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description) // Add to strings.xml
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Make column scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.create_die_instructions), // Add to strings.xml
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dieAcronym,
                onValueChange = { dieAcronym = it.trim() },
                label = { Text(stringResource(R.string.die_acronym_label)) }, // e.g., "Die Acronym (e.g., d3, d100)"
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text(stringResource(R.string.die_acronym_placeholder)) } // e.g., "d3"
            )

            OutlinedTextField(
                value = dieSides,
                onValueChange = { newValue ->
                    // Allow only digits and ensure it's not excessively long
                    if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                        dieSides = newValue
                    }
                },
                label = { Text(stringResource(R.string.number_of_sides_label)) }, // e.g., "Number of Sides"
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text(stringResource(R.string.number_of_sides_placeholder)) } // e.g., "3"
            )

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom if content is short

            Button(
                onClick = {
                    focusManager.clearFocus() // Hide keyboard
                    if (diceViewModel.addCustomDie(sides = dieSides, acronym = dieAcronym)) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.die_created_success, dieAcronym), // e.g., "Die $dieAcronym created!"
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack() // Go back to the previous screen
                    } else {
                        // Error messages (e.g., invalid sides, duplicate acronym) are logged in ViewModel/Manager
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_to_create_die), // e.g., "Failed to create die. Check inputs or logs."
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dieAcronym.isNotBlank() && dieSides.isNotBlank() && (dieSides.toIntOrNull()
                    ?: 0) > 0
            ) {
                Text(stringResource(R.string.save_die_button)) // e.g., "Save Die"
            }
        }
    }
}

// Add these to your res/values/strings.xml:
/*
<string name="create_custom_die_title">Create Custom Die</string>
<string name="navigate_back_content_description">Navigate back</string>
<string name="create_die_instructions">Define a new die by its acronym (like \"d3\" or \"d100\") and number of sides. The name is optional.</string>
<string name="die_acronym_label">Die Acronym (e.g., d3, d100)</string>
<string name="die_acronym_placeholder">e.g., d3</string>
<string name="number_of_sides_label">Number of Sides</string>
<string name="number_of_sides_placeholder">e.g., 3</string>
<string name="die_name_optional_label">Die Name (Optional)</string>
<string name="custom_d3_placeholder">e.g., My Custom d3</string>
<string name="save_die_button">Save Die</string>
<string name="die_created_success">Die %1$s created!</string>
<string name="failed_to_create_die">Failed to create die. Check inputs or logs.</string>
*/

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun CreateDieScreenPreview() {
    val context = LocalContext.current
    val navController = remember { NavController(context) }

    // Provide a dummy or test ViewModel
    val dummyViewModel = DiceViewModel() // You may need a no-arg constructor or fake data here

    AndroidDiceRollerTheme {
        CreateDieScreen(
            navController = navController,
            diceViewModel = dummyViewModel
        )
    }
}