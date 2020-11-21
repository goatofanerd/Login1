package com.example.login1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;

public class CreateProblem extends AppCompatActivity {

    EditText problemEnter;
    EditText captionEnter;

    String tag;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;

    Problem problem;

    final int PICK_IMAGE = 1;
    final int PICK_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_problem);

        getSupportActionBar().setTitle("Create New Problem");
        problem = new Problem("Unnamed Problem", "");

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if(isVisible)
                {
                    findViewById(R.id.submitButton).setVisibility(View.INVISIBLE);
                }
                else
                {
                    findViewById(R.id.submitButton).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onImageClick(View view)
    {
        findViewById(R.id.options).setVisibility(View.VISIBLE);
        findViewById(R.id.cameraSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.photoSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onChoice(View view) {
        Log.d("tag", view.getTag().toString());
        tag = view.getTag().toString();
        switch (tag) {
            case "camera":

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                }
                break;

            case "photo":
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                } else {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
                }
                break;
            case "cancel":
                findViewById(R.id.options).setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && tag.equals("camera")) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && tag.equals("photo")) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_LONG).show();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri imageURI = data.getData();

            problem.addImage(imageURI);
        } else if (requestCode == PICK_CAMERA && resultCode == Activity.RESULT_OK && null != data) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            Uri imageURI = getImageUri(this, photoBitmap);

            problem.addImage(imageURI);
        } else {
            Log.d("cancel", "Cancelled");
        }

        findViewById(R.id.cameraSelect).setVisibility(View.INVISIBLE);
        findViewById(R.id.photoSelect).setVisibility(View.INVISIBLE);
        findViewById(R.id.cancel).setVisibility(View.INVISIBLE);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}