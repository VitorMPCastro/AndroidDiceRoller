package com.example.androiddiceroller // Ensure this matches your package name

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.androiddiceroller.ui.theme.AndroidDiceRollerTheme // Ensure this is your correct theme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

class DiceViewModel(private val diceRollManager: DiceRollManager = DiceRollManager()) : ViewModel() {
    val availableDice: List<Die> = diceRollManager.standardDiceTypes
    val selectedDie: StateFlow<Die> = diceRollManager.selectedDie

    fun selectDie(die: Die) {
        diceRollManager.selectDie(die)
    }

    fun rollCurrentDie(): Int {
        return diceRollManager.rollCurrentDie()
    }
}

data class DiceRollData(
    var key: String? = null,
    val roll: Int,
    val dieType: String = "d6",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(null, 0, "", 0)
}

@Composable
fun DiceRollerScreen(
    modifier: Modifier = Modifier,
    diceViewModel: DiceViewModel = remember { DiceViewModel() }
) {
    // State to hold the current displayed dice value
    var displayedRoll by remember { mutableStateOf(1) }
    // State to manage if the dice is currently "rolling" (animating)
    var isRolling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val diceRollsRef: DatabaseReference = Firebase.database.reference.child("diceRolls")
    val availableDice = diceViewModel.availableDice
    Log.d("DiceRollerScreen", "Available dice: $availableDice")
    val selectedDie by diceViewModel.selectedDie.collectAsState()

    var rollHistory by remember { mutableStateOf<List<DiceRollData>>(emptyList()) }

    DisposableEffect(Unit) {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseHistory", "onDataChange triggered. Children count: ${snapshot.childrenCount}")
                val newHistory = mutableListOf<DiceRollData>()
                for (childSnapshot in snapshot.children) {
                    Log.d("FirebaseHistory", "Child key: ${childSnapshot.key}")
                    try {
                        val roll = childSnapshot.getValue(DiceRollData::class.java)
                        if (roll != null) {
                            roll.key = childSnapshot.key // Store the key
                            Log.d("FirebaseHistory", "Read roll: key=${roll.key}, value=${roll.roll}, timestamp=${roll.timestamp}")
                            if (roll.timestamp == 0L && roll.roll != 0) {
                                Log.w("FirebaseHistory", "Timestamp is 0L for roll value: ${roll.roll}, key: ${childSnapshot.key}")
                            }
                            newHistory.add(roll)
                        } else {
                            Log.w("FirebaseHistory", "Failed to parse roll for key: ${childSnapshot.key}. Data: ${childSnapshot.value}")
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseHistory", "Exception parsing roll for key: ${childSnapshot.key}", e)
                    }
                }
                rollHistory = newHistory.reversed()
                Log.d("FirebaseHistory", "Updated rollHistory size: ${rollHistory.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error reading roll history: ", error.toException())
                Toast.makeText(
                    context,
                    "Failed to read roll history: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        diceRollsRef.addValueEventListener(valueEventListener)
        onDispose {
            diceRollsRef.removeEventListener(valueEventListener)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        Text(
            text = stringResource(R.string.select_die_label), // e.g., "Select Die:"
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp), // Increased padding after selector
            horizontalArrangement = Arrangement.Center, // Center the row of buttons
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Consider using FlowRow if you have many dice and want them to wrap
            // e.g., androidx.compose.foundation.layout.FlowRow
            availableDice.forEach { die ->
                Button(
                    onClick = { diceViewModel.selectDie(die) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (die == selectedDie) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (die == selectedDie) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = die.acronym)
                }
            }
        }

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
                        val actualRoll = diceViewModel.rollCurrentDie()

                        // Animation phase: quickly change numbers
                        val animationDurationMillis = 500L // Total duration of "shuffling"
                        val changeIntervalMillis = 50L  // How often to change the number
                        var elapsedTime = 0L

                        while (elapsedTime < animationDurationMillis) {
                            displayedRoll = (1..selectedDie.sides).random()
                            delay(changeIntervalMillis)
                            elapsedTime += changeIntervalMillis
                        }

                        displayedRoll = actualRoll // Set the final actual roll
                        isRolling = false

                        val rollData = DiceRollData(roll = actualRoll, dieType = selectedDie.acronym)
                        diceRollsRef.push().setValue(rollData)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Roll data sent successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error saving roll data: ", e)
                                Toast.makeText(
                                    context,
                                    "Failed to save roll: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            },
            enabled = !isRolling,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(text = stringResource(if (isRolling) R.string.rolling_button_text else R.string.roll_button_text))
        }

        if (rollHistory.isNotEmpty()) {
            Text(
                text = stringResource(R.string.roll_history_header),
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            RollHistoryTable(
                history = rollHistory,
                diceRollsRef = diceRollsRef
            )
        } else {
            Text(
                text = stringResource(R.string.no_roll_history),
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
fun RollHistoryTable(
    history: List<DiceRollData>,
    diceRollsRef: DatabaseReference
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) } // Changed format for clarity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)) {
                    Text("Time", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Roll", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Spacer(Modifier.width(48.dp)) // Space for the delete button icon
                }
                HorizontalDivider()
            }

            items(history, key = { roll -> roll.timestamp }) { rollData -> // Using timestamp as a key

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sdf.format(Date(rollData.timestamp)), Modifier.weight(1f))
                    Text(rollData.roll.toString(), Modifier.weight(0.5f), textAlign = TextAlign.End) // Assuming 'roll' is the field name
                    IconButton(
                        onClick = {
                            diceRollsRef.orderByChild("timestamp").equalTo(rollData.timestamp.toDouble())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (itemSnapshot in snapshot.children) {
                                            itemSnapshot.ref.removeValue() // Delete the item
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("FirebaseDelete", "Error finding item to delete", error.toException())
                                    }
                                })
                        },
                        modifier = Modifier.size(48.dp) // Standard icon button size
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete roll",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                HorizontalDivider()
            }
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