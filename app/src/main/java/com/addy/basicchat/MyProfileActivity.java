package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyProfileActivity extends AppCompatActivity {

    // GUI stuff
    private TextView fullName, email, bio;
    private Button edit;
    private Toolbar toolbar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // Init vars
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        bio = findViewById(R.id.bio);
        edit = findViewById(R.id.edit_profile);
        toolbar = findViewById(R.id.toolbar);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("all_users_info");   // data is located at all_users_info

        // Toolbar stuff
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Call setProfileInfo() method to get profile values from database and set them in views
        setProfileInfo();

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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}