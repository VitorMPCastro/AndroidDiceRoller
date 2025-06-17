package com.example.androiddiceroller // Or your appropriate package

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the selection of dice and the rolling process.
 */
class DiceRollManager {

    val standardDiceTypes: List<Die> = listOf(
        Die(4, "d4"),
        Die(6, "d6"),
        Die(8, "d8"),
        Die(10, "d10"),
        Die(12, "d12"),
        Die(20, "d20"),
        Die(100, "d100")
    )

    private val _selectedDie = MutableStateFlow(standardDiceTypes.find { it.acronym == "d6" } ?: standardDiceTypes.first())
    val selectedDie: StateFlow<Die> = _selectedDie.asStateFlow()

    fun selectDie(die: Die) {
        _selectedDie.value = die
    }

    fun selectDieBySides(sides: Int): Boolean {
        val die = standardDiceTypes.find { it.sides == sides }
        return if (die != null) {
            _selectedDie.value = die
            true
        } else {
            false
        }
    }

    /**
     * Rolls the currently selected die.
     * This is a simple roll; animation logic would be separate or added here.
     * @return The result of the roll.
     */
    fun rollCurrentDie(): Int {
        return _selectedDie.value.roll()
    }
}