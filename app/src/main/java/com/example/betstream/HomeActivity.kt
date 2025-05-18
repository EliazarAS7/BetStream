package com.example.betstream

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userDocId: String
    private lateinit var saldoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1) Inicializa Firestore y lee el userDocId de los extras
        db = FirebaseFirestore.getInstance()
        val nombre    = intent.getStringExtra("nombreUsuario")
        userDocId     = intent.getStringExtra("userDocId") ?: ""
        val saldoInit = intent.getDoubleExtra("saldoUsuario", 0.0)

        // 2) Muestra nombre y saldo inicial
        findViewById<TextView>(R.id.usernameText).text = nombre ?: "Usuario"
        saldoTextView = findViewById(R.id.saldoText)
        saldoTextView.text = "Saldo: $saldoInit"

        // 3) Botón “Ruleta”
        /*findViewById<LinearLayout>(R.id.ruletaButton).setOnClickListener {
            startActivity(Intent(this, RuletaActivity::class.java))
        }*/

        // 4) Botón “Blackjack”
        findViewById<LinearLayout>(R.id.blackjackButton).setOnClickListener {
            startActivity(Intent(this, BlackjackActivity::class.java))
        }

        // 5) Botón “Slots”
        findViewById<LinearLayout>(R.id.slotsButton).setOnClickListener {
            startActivity(Intent(this, SlotsActivity::class.java))
        }

        // 6) Botón “Ajustes”
        findViewById<LinearLayout>(R.id.settingsButton).setOnClickListener {
            Intent(this, SettingsActivity::class.java).also { intent ->
                intent.putExtra("userDocId", userDocId)
                startActivity(intent)
            }
        }

        // 7) Flecha “volver atrás”
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que regresa aquí, recarga el saldo desde Firestore
        if (userDocId.isNotEmpty()) {
            db.collection("Usuarios")
                .document(userDocId)
                .get()
                .addOnSuccessListener { doc ->
                    val nuevoSaldo = (doc.get("Saldo") as? Number)?.toDouble() ?: 0.0
                    saldoTextView.text = "Saldo: $nuevoSaldo"
                }
        }
    }
}
