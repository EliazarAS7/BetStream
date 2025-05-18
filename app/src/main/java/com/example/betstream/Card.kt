package com.example.betstream

data class Card(val suit: String, val rank: String) {
    // El valor base de la carta (As = 1, J/Q/K = 10, numéricas = su número)
    val value: Int
        get() = when (rank) {
            "A" -> 1
            "J", "Q", "K" -> 10
            else -> rank.toInt()
        }

    override fun toString(): String = "$rank$suit"
}
