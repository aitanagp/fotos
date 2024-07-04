package com.example.fotos;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private EditText editTextMatricula;
    private Button buttonTakePhoto;
    private ArrayList<String> fotos;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MAX_FOTOS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        fotos = new ArrayList<>();

        editTextMatricula = findViewById(R.id.editTextMatricula);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);

        buttonTakePhoto.setOnClickListener(v -> {
            if(fotos.size() < MAX_FOTOS) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(UploadActivity.this, "Ya has capturado el máximo de fotos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para procesar el resultado de la captura de la foto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // procesar la foto capturada, por ejemplo, guardarla, mostrarla, etc.
            // solo mostraremos un mensaje con la ruta temporal de la foto
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                String photoPath = extras.get("data").toString();
                fotos.add(photoPath);
                Toast.makeText(this, "Foto capturada: " + photoPath, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
