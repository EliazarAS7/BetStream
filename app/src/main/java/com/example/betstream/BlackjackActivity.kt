package com.example.betstream

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

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
    private lateinit var tvSaldo: TextView

    private lateinit var userDocId: String
    private var saldoActual: Double = 0.0
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackjack)

        // Recoger userDocId
        userDocId = intent.getStringExtra("userDocId") ?: ""

        // Referencias a vistas
        backButton    = findViewById(R.id.backButton)
        tvDealerCards = findViewById(R.id.tvDealerCards)
        tvDealerSum   = findViewById(R.id.tvDealerSum)
        tvPlayerCards = findViewById(R.id.tvPlayerCards)
        tvPlayerSum   = findViewById(R.id.tvPlayerSum)
        btnHit        = findViewById(R.id.btnHit)
        btnStand      = findViewById(R.id.btnStand)
        btnRestart    = findViewById(R.id.btnRestart)
        tvSaldo       = findViewById(R.id.tvSaldo)

        // Listeners
        backButton.setOnClickListener { finish() }
        btnHit.setOnClickListener { playerHit() }
        btnStand.setOnClickListener { playerStand() }
        btnRestart.setOnClickListener { startGame() }

        obtenerSaldoDesdeFirestore()
        startGame()
    }

    private fun obtenerSaldoDesdeFirestore() {
        db.collection("Usuarios").document(userDocId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    saldoActual = doc.getDouble("Saldo") ?: 0.0
                    actualizarTextoSaldo()

                    // Solo empieza el juego si tiene saldo suficiente
                    if (saldoActual >= 10.0) {
                        startGame()
                    } else {
                        Toast.makeText(this, "Saldo insuficiente para jugar", Toast.LENGTH_LONG).show()
                        btnHit.isEnabled = false
                        btnStand.isEnabled = false
                    }
                } else {
                    tvSaldo.text = "Saldo no encontrado"
                }
            }
            .addOnFailureListener {
                tvSaldo.text = "Error al cargar saldo"
            }
    }


    private fun actualizarTextoSaldo() {
        tvSaldo.text = "Saldo: €%.2f".format(saldoActual)
    }

    private fun startGame() {
        if (saldoActual < 10.0) {
            Toast.makeText(this, "Saldo insuficiente para jugar (mínimo €10)", Toast.LENGTH_LONG).show()
            btnHit.isEnabled = false
            btnStand.isEnabled = false
            return
        }

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
            endGame("¡Te has pasado! Has perdido.", saldoCambio = -10.0)
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

        val result: String
        val saldoCambio: Double

        when {
            dealerTotal > 21 -> {
                result = "¡El dealer se pasa! ¡Ganaste!"
                saldoCambio = 10.0
            }
            dealerTotal == playerTotal -> {
                result = "Empate."
                saldoCambio = 0.0
            }
            playerTotal > dealerTotal -> {
                result = "¡Ganaste!"
                saldoCambio = 10.0
            }
            else -> {
                result = "Has perdido."
                saldoCambio = -10.0
            }
        }

        endGame(result, saldoCambio)
    }

    private fun endGame(message: String, saldoCambio: Double) {
        updateUI(showDealerAll = true)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        btnHit.isEnabled = false
        btnStand.isEnabled = false

        // Actualiza el saldo si hubo cambios
        if (saldoCambio != 0.0) {
            val nuevoSaldo = saldoActual + saldoCambio
            db.collection("Usuarios").document(userDocId)
                .update("Saldo", nuevoSaldo)
                .addOnSuccessListener {
                    saldoActual = nuevoSaldo
                    actualizarTextoSaldo()
                }
        }
    }
}
