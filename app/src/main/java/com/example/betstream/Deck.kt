package com.example.betstream

class Deck {
    private val suits = listOf("♠", "♥", "♦", "♣")
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private val cards = mutableListOf<Card>()

    init {
        for (suit in suits) {
            for (rank in ranks) {
                cards.add(Card(suit, rank))
            }
        }
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun draw(): Card {
        if (cards.isEmpty()) {
            throw IllegalStateException("No quedan cartas en el mazo")
        }
        return cards.removeAt(0)
    }
}
