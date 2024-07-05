package com.example.fotos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {
    public void logout(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public EditText editTextMatricula;
    public Button buttonTakePhoto;
    public LinearLayout photoContainer;
    public ArrayList<Uri> fotos;

    private static final int MAX_FOTOS = 5;
    private static final String FTP_SERVER = "192.168.10.120";
    private static final String FTP_USER = "ekon";
    private static final String FTP_PASS = ".CcsCcs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        fotos = new ArrayList<>();

        editTextMatricula = findViewById(R.id.editTextMatricula);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        photoContainer = findViewById(R.id.photoContainer);

        // Inicializa la cámara
        ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imgBitmap = (Bitmap) extras.get("data");

                        // Guarda la imagen
                        String matricula = editTextMatricula.getText().toString();
                        Uri photoUri = savePhotoToInternalStorage(imgBitmap, matricula);
                        if (photoUri != null) {
                            fotos.add(photoUri);

                            // Crea una vista para la foto
                            ImageView newImageView = new ImageView(this);
                            newImageView.setImageBitmap(imgBitmap);
                            newImageView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));

                            photoContainer.addView(newImageView);

                            // Sube la imagen al FTP
                            new Thread(() -> uploadPhotoToFTP(photoUri, matricula)).start();
                        } else {
                            Toast.makeText(this, "Error guardando la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        buttonTakePhoto.setOnClickListener(v -> {
            if (fotos.size() < MAX_FOTOS) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePhotoLauncher.launch(takePhotoIntent);
            } else {
                Toast.makeText(this, "Máximo 5 fotos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para volver a MainActivity
    public void returnToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Uri savePhotoToInternalStorage(Bitmap bitmap, String matricula) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        // Ponerle nombre a la imagen
        int photoIndex = 1;
        File photoFile;
        do {
            String fileName = matricula + "(" + photoIndex + ").jpg";
            photoFile = new File(directory, fileName);
            photoIndex++;
        } while (photoFile.exists());

        try (OutputStream fos = new FileOutputStream(photoFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return Uri.fromFile(photoFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadPhotoToFTP(Uri photoUri, String matricula) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER);
            ftpClient.login(FTP_USER, FTP_PASS);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String remoteFilePath = "/fotos/" + new File(photoUri.getPath()).getName();
            try (FileInputStream fis = new FileInputStream(new File(photoUri.getPath()))) {
                boolean done = ftpClient.storeFile(remoteFilePath, fis);
                if (done) {
                    runOnUiThread(() -> Toast.makeText(this, "Imagen subida: " + remoteFilePath, Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
                }
            }
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error subiendo la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
