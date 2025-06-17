package com.example.androiddiceroller // Or your appropriate package

/**
 * Represents a type of die.
 * @property sides The number of sides on the die.
 * @property acronym A short string representation (e.g., "d20", "d6").
 */
data class Die(val sides: Int, val acronym: String) {
    init {
        require(sides > 0) { "A die must have at least 1 side." }
    }

    /**
     * Simulates rolling this die once.
     * @return A random integer between 1 and [sides] (inclusive).
     */
    fun roll(): Int {
        return (1..sides).random()
    }
}