package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateNewAccountActivity extends AppCompatActivity {

    // GUI stuff
    private EditText fullName, email, password;
    private Button createAccount;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference databaseReference;

    // Other stuff
    private boolean accountCreated = false; // to keep track whether account was created or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);

        // Init vars
        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        toolbar = findViewById(R.id.toolbar);
        createAccount = findViewById(R.id.create_account);
        progressBar = findViewById(R.id.progress_bar);
        fAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("all_users_info");

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create new account on click of createAccount button
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);  // show loading screen
                // First create new firebase account using provided email password
                fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // update accountCreated boolean once account is created successfully
                                if (task.isSuccessful()) {
                                    accountCreated = true;
                                    Toast.makeText(CreateNewAccountActivity.this, "Account created", Toast.LENGTH_SHORT).show();

                                    // if account is successfully created then update provided fullName
                                    if(accountCreated){
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName.getText().toString()). build();
                                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        // show toast on successful
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(CreateNewAccountActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(CreateNewAccountActivity.this, "failed to update profile", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                        // Call addAccountInfo method to add newly created account info into firebase realtime database
                                        addAccountInfo(fullName.getText().toString(),
                                                email.getText().toString(),
                                                password.getText().toString(),
                                                FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    }

                                } else {
                                    accountCreated = false;
                                    Toast.makeText(CreateNewAccountActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                }

                                // close current activity once account get created
                                Intent intent = new Intent(CreateNewAccountActivity.this, MainActivity.class);
                                setResult(RESULT_OK, getIntent());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);   // Hide loading once operation is completed
                                finish();
                            }
                        });
            }
        });
    }

    // Method to update Realtime database with newly created account info
    private void addAccountInfo(String fullName, String email, String password, String uid){
        // Create a Map to store info, and upload them to (root -> all_user_info)
        Map<String, String> info = new HashMap<>();
        info.put("uid", uid);
        info.put("full_name", fullName);
        info.put("email", email);
        info.put("password", password);

        databaseReference.child(uid).setValue(info).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CreateNewAccountActivity.this, "Database updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateNewAccountActivity.this, "Failed to update database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}