package com.example.imagehandling;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.graphics.BitmapFactory;
import androidx.core.content.FileProvider;

import android.graphics.Matrix; // Matrix 類別的引入
import androidx.exifinterface.media.ExifInterface; // ExifInterface 類別的引入


public class ImageViewActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

    ImageView imageView;
    Button button;

    private String currentPhotoPath;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        button = findViewById(R.id.buttonPanel);
        imageView = (ImageView) findViewById(R.id.imageviewer);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(ImageViewActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ImageViewActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        File directory = getExternalFilesDir(null);
        deleteJPGFiles(directory);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {}

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(ImageViewActivity.this,
                        "com.example.imagehandling.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        Log.d("MyApp","currentPhotoPath: " + currentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            // Rotate bitmap here
            try {
                ExifInterface exif = new ExifInterface(currentPhotoPath);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);

                imageView.setImageBitmap(rotatedBitmap);
            } catch (IOException e) {
                // 處理異常，例如文件不存在
                e.printStackTrace();
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            // 處理 out of memory 錯誤
            return null;
        }
    }

    public void deleteJPGFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list(new FilenameFilter() {
                // Apply filter on each file in directory
                // Return true if file name ends with .jpg or .JPG
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".JPG"));
                }
            });

            for (String child : children) {
                new File(dir, child).delete();
            }
        }
    }


}