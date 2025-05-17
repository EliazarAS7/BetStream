package com.example.betstream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class IniciarSesionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Botón de retroceso
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        setUpbuttoncrearcuenta()
        login()
    }

    private fun setUpbuttoncrearcuenta() {
        findViewById<TextView>(R.id.tvLinkCreateAccount).setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
        }
    }

    private fun login() {
        val passwordField: EditText = findViewById(R.id.etPassword)
        val dniField: EditText      = findViewById(R.id.etDni)
        val db = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val dniText      = dniField.text.toString().trim()
            val passwordText = passwordField.text.toString().trim()

            Log.d("LoginDebug", "DNI ingresado: $dniText")
            Log.d("LoginDebug", "Password ingresada: $passwordText")

            // Buscamos el usuario
            db.collection("Usuarios")
                .whereEqualTo("dni", dniText)
                .whereEqualTo("password", passwordText)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val doc = documents.first()
                    val docId    = doc.id                              // <-- Extraemos el ID
                    val nombre   = doc.getString("nombre") ?: ""
                    val saldoRaw = doc.get("Saldo")
                    val saldo    = (saldoRaw as? Number)?.toDouble() ?: 0.0

                    Log.d("LoginDebug", "Login exitoso. DocID=$docId, Nombre=$nombre, Saldo=$saldo")

                    // Lanzamos HomeActivity pasando también el userDocId
                    Intent(this, HomeActivity::class.java).apply {
                        putExtra("nombreUsuario", nombre)
                        putExtra("saldoUsuario", saldo)
                        putExtra("userDocId", docId)                  // <-- Aquí lo pasamos
                    }.also {
                        startActivity(it)
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    Log.e("LoginError", "Error on login: $exception")
                }
        }
    }
}
