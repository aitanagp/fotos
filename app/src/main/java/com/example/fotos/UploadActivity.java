package com.example.fotos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadActivity<ApiService> extends AppCompatActivity {

    private EditText editTextMatricula;
    private Button buttonTakePhoto;
    private LinearLayout photoContainer;
    private ArrayList<Uri> fotos;
    private LocationManager locationManager;
    private Location currentLocation;
    private ExecutorService executorService;
    private ListView listViewMatriculas;
    private ArrayAdapter<String> matriculasAdapter;
    private ArrayList<String> listaMatriculas;
    private static final int MAX_FOTOS = 5;
    private static final String FTP_SERVER = "192.168.10.120";
    private static final String FTP_USER = "ekon";
    private static final String FTP_PASS = ".CcsCcs";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String BASE_URL = "https://192.168.10.120:9080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        fotos = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        executorService = Executors.newSingleThreadExecutor();

        editTextMatricula = findViewById(R.id.editTextMatricula);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        photoContainer = findViewById(R.id.photoContainer);

        // Inicializa la lista de matrículas
        listViewMatriculas = findViewById(R.id.listViewMatriculas);
        listaMatriculas = new ArrayList<>();
        matriculasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMatriculas);
        listViewMatriculas.setAdapter(matriculasAdapter);

        // Llamar a la API para obtener matrículas
        fetchMatriculasFromApi();

        // Solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }

        // Inicializa la cámara
        ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imgBitmap = (Bitmap) extras.get("data");

                        // Guarda la imagen
                        executorService.execute(() -> {
                            String locationName = getLocationName(currentLocation);
                            Bitmap watermarkedBitmap = addWatermarkToBitmap(imgBitmap, locationName);
                            String matricula = editTextMatricula.getText().toString();
                            Uri photoUri = savePhotoToInternalStorage(watermarkedBitmap, matricula);
                            if (photoUri != null) {
                                runOnUiThread(() -> {
                                    fotos.add(photoUri);

                                    // Crea una vista para la foto
                                    ImageView newImageView = new ImageView(this);
                                    newImageView.setImageBitmap(watermarkedBitmap);
                                    newImageView.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    ));
                                    photoContainer.addView(newImageView);

                                    // Sube la imagen al FTP
                                    new Thread(() -> uploadPhotoToFTP(photoUri)).start();
                                });
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, "Error guardando la imagen", Toast.LENGTH_SHORT).show());
                            }
                        });
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

    private void fetchMatriculasFromApi() {
        Cleaner GsonConverterFactory;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Pasa los parámetros correctamente
        Call<List<String>> call = apiService.getClass("2024", "25");

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    List<String> matriculas = response.body();
                    if (matriculas != null) {
                        listaMatriculas.clear(); // Limpia la lista antes de agregar nuevos datos
                        listaMatriculas.addAll(matriculas);
                        matriculasAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(UploadActivity.this, "Error en la respuesta de la API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "Error en la API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        currentLocation = location;
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getLocationName(Location location) {
        if (location == null) {
            return "Unknown Location";
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality() + ", " + address.getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

    private Bitmap addWatermarkToBitmap(Bitmap originalBitmap, String locationName) {
        Bitmap result = originalBitmap.copy(originalBitmap.getConfig(), true);
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(40); // Ajuste del tamaño del texto
        paint.setAntiAlias(true);
        paint.setUnderlineText(false);

        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
        String watermarkText = timestamp + " " + locationName;

        canvas.drawText(watermarkText, 10, result.getHeight() - 10, paint);

        return result;
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

        // Ponerle nombre a la imagen usando la matrícula
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

    private void uploadPhotoToFTP(Uri photoUri) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER);
            ftpClient.login(FTP_USER, FTP_PASS);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Nombre del archivo en el servidor
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
