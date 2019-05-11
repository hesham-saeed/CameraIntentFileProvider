package com.example.implicitintentdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 55;
    private static final int READ_STORAGE_PERMISSION_CODE = 102;

    private Uri imageUri;
    private File newImageFile;
    private ImageView imageView;
    private Button takePhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        takePhotoButton = findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
                    } else {
                        dispatchTakePictureIntent();
                    }

                } else {
                    dispatchTakePictureIntent();
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }

    private void dispatchTakePictureIntent() {

        newImageFile = createPublicImageFile();
        if (newImageFile == null) return;
        //imageUri = Uri.fromFile(newFile); // for support devices with sdk < 19
        imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.implicitintentdemo.fileprovider", newImageFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) == null)
            takePhotoButton.setEnabled(false);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        List<ResolveInfo> cameraActivities = getPackageManager().queryIntentActivities(
                cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo activity : cameraActivities) {
            grantUriPermission(activity.activityInfo.packageName,
                    imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            try {

                Uri uri = FileProvider.getUriForFile(this, "com.example.implicitintentdemo.fileprovider", newImageFile);
                revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                Bitmap image = PictureUtils.getScaledBitmap(this, newImageFile.getPath(), this);
                imageView.setImageBitmap(image);

                Log.i("MainActivity", "onActivityResult:" + imageUri.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File createPublicImageFile() {
        //check if external storage exists
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED))
            return null;

        File mediaDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ImplictIntentDemoApp" //new directory
        );

        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.i("MainActivity", "createNewFile: failed to create a new directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date());

        File newFile = new File(mediaDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return newFile;
    }

}
