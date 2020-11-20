package com.example.login1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("ConstantConditions")
public class ViewPage extends Fragment implements View.OnClickListener {

    ArrayList<StorageReference> images = new ArrayList<>();
    final int PICK_IMAGE = 1;
    final int PICK_CAMERA = 0;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 101;

    String tag;

    Task<Uri> downloadUrl;
    GoogleSignInAccount account;

    private StorageReference mStorageRef;
    FirebaseAuth mAuth;

    MaterialButton addImageButton;
    Button logOut;
    Button photo, camera, cancel;
    Uri imageURI;

    LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_view_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addImageButton = getView().findViewById(R.id.addImage);
        logOut = getView().findViewById(R.id.logout);
        photo = getView().findViewById(R.id.photoSelect);
        camera = getView().findViewById(R.id.cameraSelect);
        cancel = getView().findViewById(R.id.cancel);

        addImageButton.setOnClickListener(this);
        logOut.setOnClickListener(this);
        photo.setOnClickListener(this);
        camera.setOnClickListener(this);
        cancel.setOnClickListener(this);
        account = (GoogleSignInAccount) getActivity().getIntent().getExtras().get("account");
        layout = getView().findViewById(R.id.layout);
        Log.d("acc", account.getDisplayName());

        retrieveImages();
        KeyboardUtils.addKeyboardToggleListener(getActivity(), new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {
                if(isVisible)
                {
                    getView().findViewById(R.id.addImage).setVisibility(View.INVISIBLE);
                    getActivity().findViewById(R.id.tabs).setVisibility(View.INVISIBLE);
                }
                else
                {
                    getView().findViewById(R.id.addImage).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.tabs).setVisibility(View.VISIBLE);
                    if(layout.getChildCount() != 0) {
                        uploadCaptions();
                    }
                }
            }
        });
    }

    private void retrieveImages() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://login1-1604103237674.appspot.com/");
        StorageReference pathReference = storageRef.child("images/" + account.getEmail() + "/");

        pathReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        for (StorageReference item : listResult.getItems()) {
                            item.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                                // Use the bytes to display the image
                                images.add(item);
                                Log.d("success", "success getting Image");
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                //Getting MetaData
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // Metadata now contains the metadata for 'images/forest.jpg'
                                        Log.d("success", "success getting metadata");
                                        addImageToLayout(bmp, storageMetadata.getCustomMetadata("caption"));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d("error", "error getting metadata");
                                    }
                                });

                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        Log.d("error", "retrieving image error");
                    }
                });

    }

    private void addImageToLayout(Bitmap bmp, String caption)
    {
        ImageView imageView = new ImageView(getContext());
        imageView.setId(layout.getChildCount());
        imageView.setPadding(20, 30, 20, 30);
        imageView.setImageBitmap(bmp);
        imageView.setMinimumWidth(layout.getWidth());
        imageView.setMinimumHeight(1000);
        layout.addView(imageView);

        addCaption(caption);
    }

    private void addImageToLayout(Uri uri, String caption)
    {
        ImageView imageView = new ImageView(getContext());
        imageView.setId(layout.getChildCount());
        imageView.setPadding(20, 150, 20, 30);
        imageView.setImageURI(uri);
        imageView.setMinimumWidth(layout.getWidth());
        imageView.setMinimumHeight(1000);
        layout.addView(imageView);

        addCaption(caption);
    }

    private void addCaption(String captionText)
    {
        EditText caption = new EditText(getContext());
        caption.setId(layout.getChildCount());
        caption.setPadding(2, 30, 20, 30);
        caption.setHint("Enter Caption");
        caption.setHintTextColor(Color.GRAY);
        caption.setTextColor(Color.LTGRAY);
        caption.setTextSize(20);
        caption.setImeOptions(EditorInfo.IME_ACTION_DONE);
        caption.setSingleLine(true);
        caption.setWidth(layout.getWidth() - 300);
        caption.setText(captionText);
        if(Build.VERSION.SDK_INT >= 21) {
            caption.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(50,205,50)));
        }


        layout.addView(caption);

    }

    private void uploadCaptions()
    {
        Log.d("layout", layout.getChildCount() + " ");
        for(int i = 0; i < layout.getChildCount() / 2; i++)
        {
            Log.d("i", i + " ");
            String captionText = ((EditText) layout.getChildAt(2*i + 1)).getText().toString();
            StorageMetadata caption = new StorageMetadata.Builder()
                    .setContentType(images.get(i).getPath())
                    .setCustomMetadata("caption", captionText)
                    .build();
            Log.d("path", images.get(i).getPath());
            Task<StorageMetadata> storageMetadataTask = images.get(i).updateMetadata(caption)
                    .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            // Updated metadata is in storageMetadata
                            Log.d("success", "success uploading caption");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d("error", "error uploading caption");
                        }
                    });
        }
    }

    public void onAddImageClick() {
        getView().findViewById(R.id.cameraSelect).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.photoSelect).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.cancel).setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onChoice(String tag) {
        Log.d("tag", tag);
        switch (tag) {
            case "camera":

                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                }
                break;

            case "photo":
                if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                } else {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
                }
                break;
            case "cancel":
                getView().findViewById(R.id.cameraSelect).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.photoSelect).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.cancel).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && tag.equals("camera")) {
                Toast.makeText(getContext(), "Camera Permission Granted", Toast.LENGTH_LONG).show();
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            } else {
                Toast.makeText(getContext(), "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && tag.equals("photo")) {
                Toast.makeText(getContext(), "Storage Permission Granted", Toast.LENGTH_LONG).show();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
            } else {
                Toast.makeText(getContext(), "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            imageURI = data.getData();

            uploadImage(imageURI);
        } else if (requestCode == PICK_CAMERA && resultCode == Activity.RESULT_OK && null != data) {
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
            imageURI = getImageUri(getContext(), photoBitmap);

            uploadImage(imageURI);
        } else {
            Log.d("cancel", "Cancelled");
        }

        getView().findViewById(R.id.cameraSelect).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.photoSelect).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.cancel).setVisibility(View.INVISIBLE);
    }

    private void uploadImage(Uri photoURI) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        StorageReference storage = mStorageRef.child("images/" + account.getEmail() + "/" + currentTime);
        images.add(storage);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            storage.putFile(photoURI)
                    .addOnSuccessListener(taskSnapshot -> {

                        downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        addImageToLayout(photoURI, "");
                    })
                    .addOnFailureListener(exception -> {
                        Log.d("url", "fail: " + downloadUrl.toString());
                        // Handle unsuccessful uploads
                        // ...
                    });
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public void logout() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addImage:
                onAddImageClick();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.photoSelect:
                onChoice("photo");
                break;
            case R.id.cameraSelect:
                onChoice("camera");
                break;
            case R.id.cancel:
                onChoice("cancel");
                break;
        }
    }


}