package com.example.login1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateProblem extends AppCompatActivity {

    EditText problemEnter;
    EditText captionEnter;

    String tag;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;

    Problem problem;

    final int PICK_IMAGE = 1;
    final int PICK_CAMERA = 0;

    LinearLayout imagesLayout;

    private StorageReference mStorageRef;
    FirebaseAuth mAuth;

    GoogleSignInAccount account;

    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_problem);

        getSupportActionBar().setTitle("Create New Problem");
        problem = new Problem("Unnamed Problem", "");
        account = (GoogleSignInAccount) getIntent().getExtras().get("account");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        bar = (ProgressBar) findViewById(R.id.progressBar);
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

        problemEnter = findViewById(R.id.problem);
        captionEnter = findViewById(R.id.caption);
        imagesLayout = findViewById(R.id.imagesLayout);
    }

    public void onImageClick(View view)
    {
        findViewById(R.id.options).setVisibility(View.VISIBLE);
        findViewById(R.id.cameraSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.photoSelect).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
    }

    public void addImageToLayout(Uri image)
    {
        ImageView imageView = new ImageView(this);
        imageView.setId(imagesLayout.getChildCount());
        imageView.setPadding(0, 20, 0, 20);
        imageView.setImageURI(image);

        imageView.setMinimumHeight(imagesLayout.getHeight());
        imageView.setMinimumWidth(600);
        imagesLayout.addView(imageView);

        Toast.makeText(this, "Image Successfully Added", Toast.LENGTH_LONG).show();

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
            addImageToLayout(imageURI);
        } else if (requestCode == PICK_CAMERA && resultCode == Activity.RESULT_OK && null != data) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            Uri imageURI = getImageUri(this, photoBitmap);

            problem.addImage(imageURI);
            addImageToLayout(imageURI);
        } else {
            Log.d("cancel", "Cancelled");
        }

        findViewById(R.id.cameraSelect).setVisibility(View.INVISIBLE);
        findViewById(R.id.photoSelect).setVisibility(View.INVISIBLE);
        findViewById(R.id.cancel).setVisibility(View.INVISIBLE);
    }

    public void onSubmit(View view)
    {
        problem.setProblem(problemEnter.getText().toString());
        problem.setCaption(captionEnter.getText().toString());

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        uploadProblem();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have attached " + problem.getImages().size() + " images, submit the problem?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void uploadProblem() {
        Log.d("upload", "Problem: " + problem.getProblem() + "    Caption: " + problem.getCaption());
        uploadImages();
    }

    private void uploadImages() {
        bar.setVisibility(View.VISIBLE);
        findViewById(R.id.submitButton).setVisibility(View.INVISIBLE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        StorageReference storage = mStorageRef.child("images/" + account.getEmail() + "/ProblemAt " + currentTime + "/");
        FirebaseUser user = mAuth.getCurrentUser();

        StorageMetadata metaData = new StorageMetadata.Builder()
                .setCustomMetadata("problem", problem.getProblem())
                .setCustomMetadata("caption", problem.getCaption())
                .build();
        if (user != null && problem.getImages().size() != 0) {
            for(int i = 0; i < problem.getImages().size(); i++) {
                storage.child("" + i).putFile(problem.getImages().get(i))
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d("success", "success uploading images");
                            bar.setProgress(bar.getProgress() + 100/problem.getImages().size());
                        })
                        .addOnFailureListener(exception -> {
                            Log.d("url", "fail uploading");
                            // Handle unsuccessful uploads
                            // ...
                        });

            }
            storage.child("metadata").putFile(problem.getImages().get(problem.getImages().size() - 1), metaData)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("success", "success uploading problem and caption");
                        bar.setProgress(100);
                    })
                    .addOnFailureListener(exception -> {
                        Log.d("url", "fail uploading problem and caption");
                        // Handle unsuccessful uploads
                        // ...
                    });
        }


        try {
            Thread.sleep(300);
        }
        catch(Exception e)
        {
            Log.d("error", "error sleeping");
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}