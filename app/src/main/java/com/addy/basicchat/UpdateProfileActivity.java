package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    // GUI stuff
    private EditText fullName, bio;
    private Button updateProfile;
    private Toolbar toolbar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Init vars
        fullName = findViewById(R.id.full_name);
        bio = findViewById(R.id.bio);
        updateProfile = findViewById(R.id.update_profile);
        toolbar = findViewById(R.id.toolbar);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("all_users_info");   // data is located at all_users_info

        // Toolbar stuff
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Call getAndSetValues() method to get values from database and update views
        getAndSetValues();

        // updateProfile button functionality, call updateUserProfile() method
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });
    }

    // Method to take updating user's information in realtime database
    private void updateUserProfile() {
        // Create an Map object to update user information, get fullName and bio for now
        Map<String, Object> updatedInfo = new HashMap<>();
        updatedInfo.put("full_name", fullName.getText().toString().trim());
        updatedInfo.put("bio", bio.getText().toString().trim());

        // Users data is located at all_users_info -> Uid
        database.child(fAuth.getCurrentUser().getUid()).updateChildren(updatedInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // If information is successfully updated, then show "Profile updated Successfully" toast
                if (task.isSuccessful()){
                    Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Failed to update profile", Toast.LENGTH_LONG).show();
                }
                finish(); // Close the UpdateProfileActivity
            }
        });

    }

    // Method to take care of getting values from database and update views accordingly
    private void getAndSetValues() {
        // Users data is located at all_users_info -> Uid
        database.child(fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get the value from snapshot and insert in UserInformation model class
                UserInformation userInfo = snapshot.getValue(UserInformation.class);

                // Update fullName, bio editText with userInfo, So that EditText will have current values
                fullName.setText(userInfo.getFull_name());
                bio.setText(userInfo.getBio());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}