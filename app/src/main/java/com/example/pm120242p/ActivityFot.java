package com.example.pm120242p;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ActivityFot extends AppCompatActivity {

    static final int peticion_acceso_camara = 101;
    static final int peticion_captura_foto = 102;
    static final int peticion_captura_video = 103;
    String currentPhotoPath,currentVideoPath;
    ImageView ObjectImage;
    Button btnCaptura, btnCaptureVideo;
    String pathImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ObjectImage = findViewById(R.id.imageView);
        btnCaptura = findViewById(R.id.btntakefoto);
        btnCaptura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });
        btnCaptureVideo = findViewById(R.id.btnVideoCapture);
        btnCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVideo();
            }
        });
    }

    private void permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, peticion_acceso_camara);
        }else{
            TomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == peticion_acceso_camara){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                TomarFoto();
            }else{
                Toast.makeText(getApplicationContext(), "Acceso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, peticion_captura_foto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == peticion_captura_foto && resultCode == RESULT_OK){
            if(data != null){
                dispatchTakePictureIntent();
                Bitmap imagen = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                Log.d("Base64 convert : ",ConvertImageToBase64(imagen));
                ObjectImage.setImageBitmap(imagen);
            }
        }
        if(requestCode == peticion_captura_video && resultCode == RESULT_OK){
            if(data != null){
                save();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm0120242p.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //startActivityForResult(takePictureIntent, peticion_captura_foto);
                File file = new File(currentPhotoPath);
                if(file.exists()) {
                    Toast.makeText(getApplicationContext(), "Foto guardada correctamente path: " + currentPhotoPath, Toast.LENGTH_LONG).show();
                    Log.d("path", currentPhotoPath);
                }
            }
        }
    }

    private String ConvertImageToBase64(Bitmap map){
        ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.JPEG, 50, byteImage);
        byte[] byteArray = byteImage.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void recordVideo(){
        Intent record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivity(record);
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    private void save(){
        Intent record = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (record.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
            }
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this,
                        "com.example.pm0120242p.fileprovider",
                        videoFile);
                record.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                Log.i("Video path", currentVideoPath);
            }
        }
    }

}