package com.example.betstream

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class RuletaActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var spinnerTipoApuesta: Spinner
    private lateinit var etCantidadApuesta: EditText
    private lateinit var etNumeroEspecifico: EditText
    private lateinit var btnApostar: Button
    private lateinit var btnGirar: Button
    private lateinit var tvResultadoRuleta: TextView
    private lateinit var tvSaldo: TextView

    private lateinit var userDocId: String
    private var saldoActual: Double = 0.0
    private val db = FirebaseFirestore.getInstance()

    private val apuestas = listOf("Rojo", "Negro", "Par", "Impar", "Número específico")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruleta)

        // Obtener ID de usuario
        userDocId = intent.getStringExtra("userDocId") ?: ""

        // Inicializar vistas
        backButton = findViewById(R.id.backButton)
        spinnerTipoApuesta = findViewById(R.id.spinnerTipoApuesta)
        etCantidadApuesta = findViewById(R.id.etCantidadApuesta)
        etNumeroEspecifico = findViewById(R.id.etNumeroEspecifico)
        btnApostar = findViewById(R.id.btnApostar)
        btnGirar = findViewById(R.id.btnGirar)
        tvResultadoRuleta = findViewById(R.id.tvResultadoRuleta)
        tvSaldo = findViewById(R.id.tvSaldo)

        // Ocultar el campo al inicio
        etNumeroEspecifico.visibility = EditText.GONE

        // Adaptador del spinner
        spinnerTipoApuesta.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, apuestas)

        // Mostrar campo solo si es "Número específico"
        spinnerTipoApuesta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val tipo = apuestas[position]
                etNumeroEspecifico.visibility = if (tipo == "Número específico") EditText.VISIBLE else EditText.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listeners
        backButton.setOnClickListener { finish() }
        btnApostar.setOnClickListener { realizarApuesta() }
        btnGirar.setOnClickListener { girarRuleta() }

        obtenerSaldoDesdeFirestore()
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

    private fun realizarApuesta() {
        val cantidadStr = etCantidadApuesta.text.toString()
        val cantidad = cantidadStr.toDoubleOrNull()

        if (cantidad == null || cantidad <= 0.0) {
            Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        if (cantidad > saldoActual) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        btnApostar.isEnabled = false
        btnGirar.isEnabled = true
        Toast.makeText(this, "Apuesta realizada. ¡Gira la ruleta!", Toast.LENGTH_SHORT).show()
    }

    private fun girarRuleta() {
        val tipoApuesta = spinnerTipoApuesta.selectedItem.toString()
        val cantidad = etCantidadApuesta.text.toString().toDoubleOrNull() ?: 0.0

        val resultado = Random.nextInt(0, 37)
        val esRojo = resultado in listOf(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
        )

        val resultadoTexto = if (resultado == 0) "0 (Verde)" else "$resultado (${if (esRojo) "Rojo" else "Negro"})"
        tvResultadoRuleta.text = "Resultado: $resultadoTexto"

        var ganancia = 0.0
        val gana = when (tipoApuesta) {
            "Rojo" -> resultado != 0 && esRojo
            "Negro" -> resultado != 0 && !esRojo
            "Par" -> resultado != 0 && resultado % 2 == 0
            "Impar" -> resultado != 0 && resultado % 2 != 0
            "Número específico" -> {
                val numeroStr = etNumeroEspecifico.text.toString()
                val numeroElegido = numeroStr.toIntOrNull()

                if (numeroElegido == null || numeroElegido !in 0..36) {
                    Toast.makeText(this, "Introduce un número válido (0-36)", Toast.LENGTH_SHORT).show()
                    btnApostar.isEnabled = true
                    btnGirar.isEnabled = false
                    return
                }

                numeroElegido == resultado
            }
            else -> false
        }

        if (gana) {
            ganancia = when (tipoApuesta) {
                "Número específico" -> cantidad * 36
                else -> cantidad * 2
            }
            Toast.makeText(this, "¡Ganaste! +€%.2f".format(ganancia), Toast.LENGTH_LONG).show()
        } else {
            ganancia = -cantidad
            Toast.makeText(this, "Perdiste €%.2f".format(cantidad), Toast.LENGTH_SHORT).show()
        }

        actualizarSaldo(ganancia)
        btnApostar.isEnabled = true
        btnGirar.isEnabled = false
    }

    private fun actualizarSaldo(cambio: Double) {
        val nuevoSaldo = saldoActual + cambio
        db.collection("Usuarios").document(userDocId)
            .update("Saldo", nuevoSaldo)
            .addOnSuccessListener {
                saldoActual = nuevoSaldo
                actualizarTextoSaldo()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar saldo", Toast.LENGTH_SHORT).show()
            }
    }
}
