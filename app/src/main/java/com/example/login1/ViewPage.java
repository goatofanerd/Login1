package com.example.login1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

//import com.google.android.gms.drive.*;
//import com.google.android.gms.tasks.*;
public class ViewPage extends AppCompatActivity {

    ImageView img;
    Bitmap newImage;
    final int PICK_IMAGE = 1;
    final int PICK_CAMERA = 0;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;

    String tag;

    Task<Uri> downloadUrl;
    GoogleSignInAccount account;

    private StorageReference mStorageRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        getSupportActionBar().hide();
        account = (GoogleSignInAccount) getIntent().getExtras().get("account");
        img = findViewById(R.id.imageView);
        Log.d("acc", account.getDisplayName());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        retrieveImage();
    }

    private void retrieveImage()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://login1-1604103237674.appspot.com/");
        StorageReference pathReference = storageRef.child("images/" + account.getEmail());
        pathReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Log.d("success", "success getting Image");
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(Bitmap.createScaledBitmap(bmp, img.getWidth(), img.getHeight(), false));
                img.setImageBitmap(bmp);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("error", "retrieving image error");
            }
        });
    }

    public void onAddImageClick(View view) {
        ((Button) findViewById(R.id.cameraSelect)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.photoSelect)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.cancel)).setVisibility(View.VISIBLE);
        //Intent chooseImage = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //startActivityForResult(chooseImage, PICK_IMAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onChoice(View view) {
        tag = view.getTag().toString();
        Log.d("tag", tag);
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
                ((Button) findViewById(R.id.cameraSelect)).setVisibility(View.INVISIBLE);
                ((Button) findViewById(R.id.photoSelect)).setVisibility(View.INVISIBLE);
                ((Button) findViewById(R.id.cancel)).setVisibility(View.INVISIBLE);
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
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            img.setImageURI(selectedImage);
            uploadImage(selectedImage);
        } else if (requestCode == PICK_CAMERA && resultCode == RESULT_OK && null != data) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            Uri photoURI = getImageUri(this, photoBitmap);
            img.setImageURI(photoURI);
            uploadImage(photoURI);
        } else {
            Log.d("cancel", "Cancelled");
        }

        ((Button) findViewById(R.id.cameraSelect)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.photoSelect)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.cancel)).setVisibility(View.INVISIBLE);
    }

    private void uploadImage(Uri photoURI) {
        //StorageReference storage = mStorageRef.child(new File(photoURI.getPath()).getAbsolutePath());
        StorageReference storage = mStorageRef.child("images/" + account.getEmail());
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            storage.putFile(photoURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            Log.d("urlUpload", downloadUrl.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("url", "fail: " + downloadUrl.toString());
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        } else {
            signInAnonymously(photoURI);
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private void signInAnonymously(Uri photoURI) {
        mAuth.signOut();
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
                //downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                Log.d("anonSuccess", "success");
                uploadImage(photoURI);

            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("fail", "signInAnonymously:FAILURE", exception);
                    }
                });

    }

    public void logout(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}