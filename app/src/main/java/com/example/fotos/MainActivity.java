package com.example.fotos;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextUsuario = findViewById(R.id.usuario);
        editTextContrasena = findViewById(R.id.contrasena);
        editTextIp = findViewById(R.id.ip);
        buttonLogin = findViewById(R.id.btn_login);

        buttonLogin.setOnClickListener(v -> {
            String usuario = editTextUsuario.getText().toString();
            String contrasena = editTextContrasena.getText().toString();
            String ip = editTextIp.getText().toString();

            if(usuario.equals("admin") && contrasena.equals("admin")) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}