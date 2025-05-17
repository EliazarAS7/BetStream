package com.example.betstream

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var fechaNacimiento: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createaccount)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setUpRegisterButton()
        setupFechaNacimientoPicker()

        // ————— Aquí inicializamos el botón de retroceso —————
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior :contentReference[oaicite:0]{index=0}
        }
    }

    private fun setupFechaNacimientoPicker() {
        val fechaNacimientoButton: Button = findViewById(R.id.etFechaNacimiento)

        fechaNacimientoButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    fechaNacimiento = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    fechaNacimientoButton.text = fechaNacimiento
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }

    private fun setUpRegisterButton() {
        val nombreField: EditText = findViewById(R.id.etNombre)
        val apellidosField: EditText = findViewById(R.id.etApellidos)
        val dniField: EditText = findViewById(R.id.etDni)
        val passwordField: EditText = findViewById(R.id.etPassword)
        val registerButton: Button = findViewById(R.id.btnCrearCuenta)

        registerButton.setOnClickListener {
            val nombre = nombreField.text.toString().trim()
            val apellidos = apellidosField.text.toString().trim()
            val dni = dniField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || fechaNacimiento.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "usuario_$dni@betstream.com"

            db.collection("Usuarios")
                .whereEqualTo("dni", dni)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Toast.makeText(this, "Ya existe una cuenta con este DNI", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    saveUserData(userId, nombre, apellidos, dni, fechaNacimiento, password)
                                }
                            } else {
                                when (val exception = task.exception) {
                                    is FirebaseAuthUserCollisionException -> {
                                        Toast.makeText(this, "El email ya está en uso", Toast.LENGTH_SHORT).show()
                                    }
                                    is FirebaseAuthWeakPasswordException -> {
                                        Toast.makeText(this, "Contraseña demasiado débil", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
                                        Log.e("RegistroError", "Error al registrar usuario", exception)
                                    }
                                }
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar usuario existente", Toast.LENGTH_SHORT).show()
                    Log.e("FirestoreError", "Error consultando Firestore: ${e.message}", e)
                }
        }
    }

    private fun saveUserData(userId: String, nombre: String, apellidos: String, dni: String, fechaNacimiento: String, password: String) {
        val userData = hashMapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "dni" to dni,
            "fecha_nac" to fechaNacimiento,
            "password" to password,
            "Saldo" to 0
        )

        db.collection("Usuarios").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Registro", "Datos guardados en Firestore")
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al guardar datos: ${e.message}", e)
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
    }
}
