package com.example.betstream


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainpage)

        // Configurar botones
        val btnIniciarSesion: Button = findViewById(R.id.iniciar)
        val btnCrearCuenta: Button = findViewById(R.id.crear)

//         Acciones para el botón "Iniciar sesión"
        btnIniciarSesion.setOnClickListener {
            // Aquí deberías agregar la lógica para iniciar sesión, por ejemplo, abrir una nueva actividad
            val intent = Intent(this, IniciarSesionActivity::class.java)
            startActivity(intent)
        }

//         Acciones para el botón "Crear Cuenta"
        btnCrearCuenta.setOnClickListener {
           // Aquí deberías agregar la lógica para crear una cuenta, por ejemplo, abrir una nueva actividad
           val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }
}
