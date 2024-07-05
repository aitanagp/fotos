package com.example.fotos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsuario;
    private EditText editTextContrasena;
    private EditText editTextIp;
    private Button buttonLogin;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_FILE = "com.example.fotos.PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsuario = findViewById(R.id.usuario);
        editTextContrasena = findViewById(R.id.contrasena);
        editTextIp = findViewById(R.id.ip);
        buttonLogin = findViewById(R.id.btn_login);

        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        // Verificar si ya hay una sesión activa
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            // Si está logueado, ir directamente a UploadActivity
            startUploadActivity();
        }else {
            // No hay una sesión activa, mostrar el formulario de inicio de sesión
            Intent intent = new Intent(this, MainActivity.class);
        }

        buttonLogin.setOnClickListener(v -> {
            String usuario = editTextUsuario.getText().toString();
            String contrasena = editTextContrasena.getText().toString();

            if (usuario.equals("ekon") && contrasena.equals(".CcsCcs")) {
                // Guardar estado de inicio de sesión
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                startUploadActivity();
            } else {
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startUploadActivity() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
        finish();
    }
}
