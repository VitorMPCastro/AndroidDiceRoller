package com.example.androiddiceroller // Or your appropriate package

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages the selection of dice and the rolling process.
 */
class DiceRollManager {
    // Keep standard dice separate if you want to distinguish them
    val standardDiceTypes: List<Die> = listOf(
        Die( sides = 4, acronym = "d4", isCustom = false),
        Die( sides = 6, acronym = "d6", isCustom = false),
        Die( sides = 8, acronym = "d8", isCustom = false),
        Die( sides = 10, acronym = "d10", isCustom = false),
        Die( sides = 12, acronym = "d12", isCustom = false),
        Die( sides = 20, acronym = "d20", isCustom = false)
    )

    private val _customDice = MutableStateFlow<List<Die>>(emptyList())
    private val _availableDice = MutableStateFlow(standardDiceTypes + _customDice.value)
    val availableDice: StateFlow<List<Die>> = _availableDice.asStateFlow()

    private val _selectedDie = MutableStateFlow(
        _availableDice.value.find { it.acronym == "d6" } ?: _availableDice.value.first()
    )
    val selectedDie: StateFlow<Die> = _selectedDie.asStateFlow()

    fun selectDie(die: Die) {
        _selectedDie.value = die
        Log.d("DiceRollManager", "Selected die: ${die.acronym}")
    }

    fun rollCurrentDie(): Int {
        val sides = _selectedDie.value.sides
        if (sides <= 0) return 1
        return (1..sides).random()
    }

    fun addCustomDie(sides: Int, acronym: String): Boolean {
        if (sides <= 0 || acronym.isBlank()) {
            Log.w("DiceRollManager", "Invalid custom die parameters: sides=$sides, acronym=$acronym")
            return false
        }

        if ((standardDiceTypes + _customDice.value).any { it.acronym.equals(acronym, ignoreCase = true) }) {
            Log.w("DiceRollManager", "Custom die with acronym '$acronym' already exists.")
            return false
        }

        val newDie = Die(sides = sides, acronym = acronym)
        _customDice.update { currentList -> currentList + newDie }
        _availableDice.update { standardDiceTypes + _customDice.value } // Update combined list
        Log.d("DiceRollManager", "Added custom die: $newDie. Total custom: ${_customDice.value.size}")
        return true
    }

    fun removeDie(die: Die) {
        if (!die.isCustom) {
            Log.w("DiceRollManager", "Cannot remove standard die: ${die.acronym}")
            return
        }
        _customDice.update { currentList -> currentList.filterNot { it == die } }
        _availableDice.update { standardDiceTypes + _customDice.value }
        // If the removed die was selected, select a default die
        if (_selectedDie.value == die) {
            selectDie(_availableDice.value.find { it.acronym == "d6" } ?: _availableDice.value.first())
        }
        Log.d("DiceRollManager", "Removed die: ${die.acronym}. Total custom: ${_customDice.value.size}")
    }
}