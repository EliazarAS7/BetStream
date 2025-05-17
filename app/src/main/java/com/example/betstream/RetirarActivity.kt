package com.example.betstream

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction

class RetirarActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userDocId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retirar)

        // 1) Instancia de Firestore
        db = FirebaseFirestore.getInstance()

        // 2) Recibir el ID de usuario
        userDocId = intent.getStringExtra("userDocId") ?: ""

        // 3) Botón de retroceso
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 4) Referencias UI
        val inputCantidad = findViewById<EditText>(R.id.inputRetirar)
        val btnRetirar    = findViewById<Button>(R.id.retirar)

        // 5) Lógica de retiro
        btnRetirar.setOnClickListener {
            val texto = inputCantidad.text.toString().trim()
            val cantidad = texto.toDoubleOrNull()
            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(this, "Introduce una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 6) Transacción para leer saldo y restar
            val docRef = db.collection("Usuarios").document(userDocId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val saldoActual = (snapshot.getDouble("Saldo") ?: 0.0)
                if (saldoActual < cantidad) {
                    throw FirebaseFirestoreException(
                        "Saldo insuficiente",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }
                // Resta usando increment negativo
                transaction.update(docRef, "Saldo", saldoActual - cantidad)
            }.addOnSuccessListener {
                Toast.makeText(this, "Has retirado \$${"%.2f".format(cantidad)}", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.ABORTED) {
                    Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al procesar retiro", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
