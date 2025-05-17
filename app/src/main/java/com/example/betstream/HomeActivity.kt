package com.example.betstream

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.ImageButton


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Asegúrate que este sea el nombre correcto

        val nombre = intent.getStringExtra("nombreUsuario")
        val saldo = intent.getDoubleExtra("saldoUsuario", 0.0)

        val usernameText: TextView = findViewById(R.id.usernameText)
        val saldoText: TextView = findViewById(R.id.saldoText)

        usernameText.text = nombre ?: "Usuario"
        saldoText.text = "Saldo: $saldo"

        // <<--- AQUÍ CONECTAS EL BOTÓN DE SLOTS --->
        val slotsButton = findViewById<LinearLayout>(R.id.slotsButton)
        slotsButton.setOnClickListener {
            val intent = Intent(this, SlotsActivity::class.java)
            startActivity(intent)
        }

        // Botón de Volver (Back)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Esto cierra la actividad y regresa a la anterior
        }
    }
}
