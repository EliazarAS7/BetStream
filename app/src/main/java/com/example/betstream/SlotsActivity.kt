package com.example.betstream


import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.betstream.adapter.SlotAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.*

class SlotsActivity : AppCompatActivity() {

    private val slotValues = listOf("7", "🍒", "🍋", "🔔", "🍉")
    private lateinit var slot1: RecyclerView
    private lateinit var slot2: RecyclerView
    private lateinit var slot3: RecyclerView
    private lateinit var spinButton: Button
    private lateinit var tvSaldo: TextView
    private lateinit var userDocId: String
    private var saldoActual: Double = 0.0
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot)

        tvSaldo = findViewById(R.id.tvSaldo)
        userDocId = intent.getStringExtra("userDocId") ?: ""

        obtenerSaldoDesdeFirestore()


        // Referencias
        slot1 = findViewById(R.id.slot1)
        slot2 = findViewById(R.id.slot2)
        slot3 = findViewById(R.id.slot3)
        spinButton = findViewById(R.id.spinButton)

        // Configurar RecyclerViews con adaptadores
        setupRecyclerView(slot1)
        setupRecyclerView(slot2)
        setupRecyclerView(slot3)

        // Acción del botón
        // Acción del botón
        spinButton.setOnClickListener {
            val costoGiro = 1.0
            if (saldoActual < costoGiro) {
                Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Descontar saldo
            saldoActual -= costoGiro
            actualizarTextoSaldo()
            actualizarSaldoEnFirestore()

            // Iniciar giros
            spinSlot(slot1, 3000L)
            spinSlot(slot2, 4000L)
            spinSlot(slot3, 5000L)
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Esto cierra la actividad actual y vuelve a la anterior
        }


    }

    private fun obtenerSaldoDesdeFirestore() {
        db.collection("Usuarios").document(userDocId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    saldoActual = doc.getDouble("Saldo") ?: 0.0
                    actualizarTextoSaldo()
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

    private fun actualizarSaldoEnFirestore() {
        db.collection("Usuarios").document(userDocId)
            .update("Saldo", saldoActual)
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar saldo", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = SlotAdapter(slotValues.shuffled())
        recyclerView.scrollToPosition(Int.MAX_VALUE / 2) // Posición inicial en el centro
    }

    private fun spinSlot(recyclerView: RecyclerView, duration: Long) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        // Elegir el símbolo aleatorio final
        val randomSymbol = slotValues.random()
        val finalIndex = slotValues.indexOf(randomSymbol)

        val startPosition = layoutManager.findFirstVisibleItemPosition()
        val handler = Handler()

        // Calcular el desplazamiento final (simula múltiples vueltas antes de detenerse)
        val spins = 10 // Número de vueltas antes de detenerse
        val targetPosition = startPosition + (spins * slotValues.size) + finalIndex

        val stepTime = 16L // 16 ms por frame (60 FPS)
        val steps = (duration / stepTime).toInt()
        var currentStep = 0

        handler.post(object : Runnable {
            override fun run() {
                if (currentStep < steps) {
                    val fraction = currentStep.toFloat() / steps
                    val offset = (targetPosition - startPosition) * fraction
                    layoutManager.scrollToPositionWithOffset(startPosition + offset.toInt(), 0)
                    currentStep++
                    handler.postDelayed(this, stepTime)
                } else {
                    // Asegurar que termina en el índice exacto
                    layoutManager.scrollToPositionWithOffset(targetPosition, 0)
                }
            }
        })
    }
}
