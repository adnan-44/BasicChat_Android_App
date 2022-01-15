package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MyProfileActivity extends AppCompatActivity {

    // GUI stuff
    private TextView fullName, email, bio;
    private Button edit;
    private Toolbar toolbar;
    private ImageView profileImage, selectImage;
    private ProgressDialog progressDialog;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;
    private StorageReference storage;   // TO work with firebase storage
    private Uri profileImageUri;

    // Other stuff
    private static final int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // Init vars
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        bio = findViewById(R.id.bio);
        edit = findViewById(R.id.edit_profile);
        profileImage = findViewById(R.id.profile_image);
        selectImage = findViewById(R.id.select_image);
        toolbar = findViewById(R.id.toolbar);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("all_users_info");   // data is located at all_users_info
        storage = FirebaseStorage.getInstance().getReference("profile_images");   // profile images are stored at profile_images

        // Toolbar stuff
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Call setProfileInfo() method to get profile values from database and set them in views
        setProfileInfo();

        // Progress Dialog stuff
        progressDialog = new ProgressDialog(MyProfileActivity.this);
        progressDialog.setTitle("Uploading image");

        // Open intent to select image for profile onClick of select_profile_image button
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open intent in ACTION_PICK to get image URI, which will later be used for profile image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");  // Because we need only image
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        // Open EditProfileActivity onClick of edit button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to take care of getting user info from realtime database and set them in views
    private void setProfileInfo() {
        // User data is located at all_users_info -> Uid
        database.child(fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the data in UserInformation model from snapshot
                UserInformation userInfo = snapshot.getValue(UserInformation.class);

                // Set the values into textViews now
                fullName.setText(userInfo.getFull_name());
                email.setText(userInfo.getEmail());
                bio.setText(userInfo.getBio());

                // Use Glide library to get and set image (using Uri, automatically) in profileImage
                if( !userInfo.getImage_url().equals("default")){
                    // If user's profile image is not "default"
                    Glide.with(getApplicationContext()).load(userInfo.getImage_url()).into(profileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && data != null && data.getData() != null){
            // If data is not null, then get Uri from data
            progressDialog.show();  // show the progress dialog
            profileImageUri = data.getData();
            uploadProfileImage(data.getData());
        }
    }

    // Method to take care of uploading image file to firebase storage
    private void uploadProfileImage(Uri imageUri){
        // Save the profile image like, profile -> uid
        storage.child(fAuth.getCurrentUser().getUid()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the downloadable Uri from taskSnapshot
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Call updateUserInfo to update user info with latest profile image Uri
                        updateUserInfo(uri);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                progressDialog.dismiss();   // dismiss the progress dialog once operation is finished
            }
        });
    }

    // Method to take care of updating user info database for profile image
    private void updateUserInfo(Uri downloadableImageUri) {
        // Create a Map for imageUrl to update user info
        Map<String, Object> info = new HashMap<>();
        info.put("image_url", downloadableImageUri.toString());

        // Users information is store at all_users_info -> Uid
        database.child(fAuth.getCurrentUser().getUid()).updateChildren(info).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // If task is successful, then set profile_image with latest image Uri
                if(task.isSuccessful()){
                    profileImage.setImageURI(profileImageUri);
                    Toast.makeText(MyProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}