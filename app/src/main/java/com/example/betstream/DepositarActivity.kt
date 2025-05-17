package com.example.betstream

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DepositarActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userDocId: String
    private var currentSaldo: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depositar)

        // 1) Instancia Firestore
        db = FirebaseFirestore.getInstance()

        // 2) Leer extras
        userDocId    = intent.getStringExtra("userDocId") ?: ""
        currentSaldo = intent.getDoubleExtra("currentSaldo", 0.0)

        // 3) Botón “volver atrás”
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 4) Referencias UI
        val inputCantidad = findViewById<EditText>(R.id.inputCantidad)
        val depositarBtn  = findViewById<Button>(R.id.depositar)

        // 5) Lógica de depósito
        depositarBtn.setOnClickListener {
            val texto = inputCantidad.text.toString().trim()
            val cantidad = texto.toDoubleOrNull()
            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizamos usando increment para evitar conflictos
            db.collection("Usuarios")
                .document(userDocId)
                .update("Saldo", FieldValue.increment(cantidad))
                .addOnSuccessListener {
                    Toast.makeText(this, "Has depositado $$cantidad", Toast.LENGTH_SHORT).show()
                    finish()  // Regresa a HomeActivity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar saldo", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
        }
    }
}
