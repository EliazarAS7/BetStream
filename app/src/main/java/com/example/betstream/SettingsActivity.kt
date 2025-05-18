package com.example.betstream

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var userDocId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // 1) Leer el userDocId que vino de HomeActivity
        userDocId = intent.getStringExtra("userDocId") ?: ""

        // 2) Flecha atrás
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 3) Botón “Depositar”
        findViewById<Button>(R.id.depositar).setOnClickListener {
            Intent(this, DepositarActivity::class.java).also { intent ->
                intent.putExtra("userDocId", userDocId)
                startActivity(intent)
            }
        }

        // 4) Botón “Retirar” (si tienes RetirarActivity)
        findViewById<Button>(R.id.retirar).setOnClickListener {
            Intent(this, RetirarActivity::class.java).also { intent ->
                intent.putExtra("userDocId", userDocId)
                startActivity(intent)
            }
        }

        // 5) Botón “Logout”
        findViewById<Button>(R.id.logout).setOnClickListener {
            // Si usas FirebaseAuth:
            FirebaseAuth.getInstance().signOut()
            // Volver a la pantalla de login y limpiar el stack
            Intent(this, IniciarSesionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }.also {
                startActivity(it)
                finish()
            }
        }
    }
}
