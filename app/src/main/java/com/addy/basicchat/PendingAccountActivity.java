package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PendingAccountActivity extends AppCompatActivity {

    // GUI stuff
    private MaterialButton refreshStatus, resendEmail;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_account);

        // Init vars
        refreshStatus = findViewById(R.id.refresh_status);
        resendEmail = findViewById(R.id.resend_email);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        // Toolbar stuff
        setSupportActionBar(toolbar);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        fAuth.getCurrentUser().reload();    // update the local cache of current user

        // onClick listener for refreshStatus button
        refreshStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call refreshStatus() method onClick of refresh button
                refreshStatus();
            }
        });

        // Onclick listener for resendEmail button
        resendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cal sendVerificationEmail() method to send email verification again
                sendVerificationEmail();
            }
        });
    }

    // Method to take care of sending verification email
    private void sendVerificationEmail(){
        // Call the send verification email method to current firebase user
        fAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Show toast if email was sent successfully
                if (task.isSuccessful()){
                    Toast.makeText(PendingAccountActivity.this, "Verification email send successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Show the toast, why it was unable to send verification email
                    Toast.makeText(PendingAccountActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to refresh user's email verification status
    private void refreshStatus(){
        // Update the firebase user status (reload to get updated values)
        progressBar.setVisibility(View.VISIBLE);
        if (fAuth.getCurrentUser() != null){
            fAuth.getCurrentUser().reload();
            // Check if user's email is verified or not
            if (fAuth.getCurrentUser().isEmailVerified()){
                // Call updateEmailStatus() method to update user's email verification in our realtime database
                updateEmailStatus();

                // Open the MainActivity if user's email is verified, so that they can use app
                Intent intent = new Intent(PendingAccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear previous activity from backstack
                startActivity(intent);
                finish();
            } else {
                // Show toast, that email is not verified yet
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingAccountActivity.this, "Email is not verified yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to update the user's email verification in our realtime database
    private void updateEmailStatus(){
        // Create an Map to update email verification status
        final Map<String, Object> update = new HashMap<>();
        update.put("email_verified", fAuth.getCurrentUser().isEmailVerified());

        // All users information is stored at all_users_info -> Uid -> email_verified
        database.child("all_users_info/" + fAuth.getCurrentUser().getUid()).updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Show database updated toast is task is successful
                if(task.isSuccessful()) {
                    Toast.makeText(PendingAccountActivity.this, "Email verified", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}