package com.example.fotos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.ref.Cleaner;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ApiService apiService;
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

        buttonLogin.setOnClickListener(v -> {
            String usuario = editTextUsuario.getText().toString();
            String contrasena = editTextContrasena.getText().toString();
            String ip = editTextIp.getText().toString();

            if (usuario.equals("ekon") && contrasena.equals(".CcsCcs")) {
                // Guardar estado de inicio de sesión y datos del servidor FTP
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("ftpUser", usuario);
                editor.putString("ftpPass", contrasena);
                editor.putString("ftpServer", ip);
                editor.apply();

                startUploadActivity();
            } else {
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        Cleaner GsonConverterFactory;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.10.120:9080/")  // Cambia esto a la base URL de tu servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        fetchInspecciones("2024", "25");
    }

    private void startUploadActivity() {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchInspecciones(String anyo, String estacion) {
        Call<List<Inspeccion>> call = apiService.getInspeccion(anyo, estacion);

        call.enqueue(new Callback<List<Inspeccion>>() {
            @Override
            public void onResponse(Call<List<Inspeccion>> call, Response<List<Inspeccion>> response) {
                if (response.isSuccessful()) {
                    List<Inspeccion> inspecciones = response.body();
                    // Procesar la lista de inspecciones
                    for (Inspeccion inspeccion : inspecciones) {
                        Log.d("MainActivity", "Inspeccion: " + inspeccion.getMatricula());
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Inspeccion>> call, Throwable t) {
                Log.e("MainActivity", "Error en la solicitud: " + t.getMessage());
            }
        });
    }
}
