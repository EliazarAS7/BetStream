package com.example.betstream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageButton

class IniciarSesionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ————— Aquí inicializamos el botón de retroceso —————
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior :contentReference[oaicite:0]{index=0}
        }
        // ————————————————————————————————————————————————

        setUpbuttoncrearcuenta()  // Configura enlace a crear cuenta
        login()                   // Configura lógica de login
    }

    private fun setUpbuttoncrearcuenta() {
        findViewById<TextView>(R.id.tvLinkCreateAccount).setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login() {
        val password: EditText = findViewById(R.id.etPassword)
        val dni: EditText = findViewById(R.id.etDni)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val dnitext = dni.text.toString().trim()
            val passwordtext = password.text.toString().trim()

            Log.d("LoginDebug", "DNI ingresado: $dnitext")
            Log.d("LoginDebug", "Password ingresada: $passwordtext")

            userExists(dnitext, passwordtext) { exists, nombre, saldo ->
                Log.d("LoginDebug", "¿Usuario existe? $exists")

                if (exists && nombre != null && saldo != null) {
                    Log.d("LoginDebug", "Login exitoso. Nombre: $nombre, Saldo: $saldo")
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        putExtra("nombreUsuario", nombre)
                        putExtra("saldoUsuario", saldo)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("LoginDebug", "Login fallido. DNI o contraseña incorrectos")
                    Toast.makeText(this, "DNI o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun userExists(dni: String, password: String, callback: (Boolean, String?, Double?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Usuarios")
            .whereEqualTo("dni", dni)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("LoginDebug", "Documentos obtenidos: ${documents.size()}")
                if (documents.isEmpty) {
                    Log.d("LoginDebug", "No se encontró ningún documento")
                    callback(false, null, null)
                } else {
                    val doc = documents.first()
                    val nombre = doc.getString("nombre")
                    val saldoRaw = doc.get("Saldo")
                    Log.d("LoginDebug", "Tipo de saldoRaw: ${saldoRaw?.javaClass?.name}")
                    val saldo = when (saldoRaw) {
                        is Number -> saldoRaw.toDouble()
                        else -> null
                    }


                    Log.d("LoginDebug", "Documento encontrado. Nombre: $nombre, Saldo: $saldo")
                    val exists = nombre != null && saldo != null
                    Log.d("LoginDebug", "¿Usuario existe? $exists")

                    callback(exists, nombre, saldo)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                Log.e("LoginError", "Error on login: $exception")
            }
    }


}
