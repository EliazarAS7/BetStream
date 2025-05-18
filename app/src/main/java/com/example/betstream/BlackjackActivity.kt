package com.example.betstream

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BlackjackActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var deck: Deck
    private val playerHand = mutableListOf<Card>()
    private val dealerHand = mutableListOf<Card>()

    private lateinit var tvDealerCards: TextView
    private lateinit var tvDealerSum: TextView
    private lateinit var tvPlayerCards: TextView
    private lateinit var tvPlayerSum: TextView
    private lateinit var btnHit: Button
    private lateinit var btnStand: Button
    private lateinit var btnRestart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackjack)

        // Referencias a vistas
        backButton    = findViewById(R.id.backButton)
        tvDealerCards = findViewById(R.id.tvDealerCards)
        tvDealerSum   = findViewById(R.id.tvDealerSum)
        tvPlayerCards = findViewById(R.id.tvPlayerCards)
        tvPlayerSum   = findViewById(R.id.tvPlayerSum)
        btnHit        = findViewById(R.id.btnHit)
        btnStand      = findViewById(R.id.btnStand)
        btnRestart    = findViewById(R.id.btnRestart)

        // Listeners
        backButton.setOnClickListener { finish() }
        btnHit.setOnClickListener { playerHit() }
        btnStand.setOnClickListener { playerStand() }
        btnRestart.setOnClickListener { startGame() }

        startGame()
    }

    private fun startGame() {
        deck = Deck().apply { shuffle() }
        playerHand.clear()
        dealerHand.clear()

        // Reparto inicial
        playerHand.add(deck.draw())
        dealerHand.add(deck.draw())
        playerHand.add(deck.draw())
        dealerHand.add(deck.draw())

        btnHit.isEnabled = true
        btnStand.isEnabled = true
        updateUI(showDealerAll = false)
    }

    private fun updateUI(showDealerAll: Boolean) {
        val dealerText = dealerHand.mapIndexed { i, card ->
            if (!showDealerAll && i == 1) "?" else card.toString()
        }.joinToString(" ")
        tvDealerCards.text = dealerText

        val visibleSum = if (showDealerAll) {
            getHandValue(dealerHand)
        } else {
            getHandValue(listOf(dealerHand.first()))
        }
        tvDealerSum.text = "Suma: $visibleSum"

        tvPlayerCards.text = playerHand.joinToString(" ") { it.toString() }
        tvPlayerSum.text   = "Suma: ${getHandValue(playerHand)}"
    }

    private fun getHandValue(hand: List<Card>): Int {
        var total = hand.sumOf { it.value }
        val aces = hand.count { it.rank == "A" }
        repeat(aces) {
            if (total + 10 <= 21) total += 10
        }
        return total
    }

    private fun playerHit() {
        playerHand.add(deck.draw())
        updateUI(showDealerAll = false)
        if (getHandValue(playerHand) > 21) {
            endGame("¡Te has pasado! Has perdido.")
        }
    }

    private fun playerStand() {
        while (getHandValue(dealerHand) < 17) {
            dealerHand.add(deck.draw())
        }
        updateUI(showDealerAll = true)
        decideWinner()
    }

    private fun decideWinner() {
        val playerTotal = getHandValue(playerHand)
        val dealerTotal = getHandValue(dealerHand)

        val result = when {
            dealerTotal > 21           -> "¡El dealer se pasa! ¡Ganaste!"
            dealerTotal == playerTotal -> "Empate."
            playerTotal > dealerTotal  -> "¡Ganaste!"
            else                       -> "Has perdido."
        }
        endGame(result)
    }

    private fun endGame(message: String) {
        updateUI(showDealerAll = true)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        btnHit.isEnabled = false
        btnStand.isEnabled = false
    }
}