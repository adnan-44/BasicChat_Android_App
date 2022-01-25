package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UpdatePasswordActivity extends AppCompatActivity {

    // GUI stuff
    private EditText currentPassword, newPassword, newConfirmPassword;
    private MaterialButton changePassword;
    private MaterialTextView forgot;
    private MaterialToolbar toolbar;
    private CircularProgressIndicator progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        // Init vars
        currentPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.new_password);
        newConfirmPassword = findViewById(R.id.confirm_new_password);
        changePassword = findViewById(R.id.change_password);
        forgot = findViewById(R.id.forgot);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        // Toolbar stuff
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // onClick listener for changePassword button
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call authenticateUser() method to re-authenticate current user (else its unable to change password)
                authenticateUser(currentPassword.getText().toString()); // Pass currentPassword, to re-authenticate
            }
        });

        // OnClick listener for forgot textView
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call sendEmail() method to send password reset email, pass current user's email
                sendEmail(fAuth.getCurrentUser().getEmail());
            }
        });
    }

    // Method to take care of changing current user's password
    private void changeNewPassword(String newPassword, String newConfirmPassword){
        // Only proceed if both "password" input field are same and are not empty
        if (currentPassword.equals("") || newPassword.equals("") || !newPassword.equals(newConfirmPassword)){
            Toast.makeText(UpdatePasswordActivity.this, "Password fields must be sa", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);   // Disable progress bar
        } else {
            // Call firebase updatePassword method to update user's password
            fAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // If task is successful then call updateDatabase() method to update newPassword in user's info
                    if (task.isSuccessful()){
                        // Call updateDatabase() method to update newPassword in all_users_info realtime database
                        updateDatabase(newPassword);
                    } else {
                        progressBar.setVisibility(View.GONE);   // Disable progress bar
                        Toast.makeText(UpdatePasswordActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Method to re-authenticate current user (it's required) to change password (if user haven't signed for a while)
    private void authenticateUser(String currentPassword){
        // Only proceed if currentPassword field is not empty
        if(currentPassword.equals("")){
            // Show current password require toast
            Toast.makeText(UpdatePasswordActivity.this, "Can't proceed without current password", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);    // Show progress bar
            // Create credential object for Email/password
            final AuthCredential credential = EmailAuthProvider.getCredential(fAuth.getCurrentUser().getEmail(), currentPassword);
            fAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // If task is successful means user is re-authenticated
                    if (task.isSuccessful()){
                        // Now call changeNewPassword to change password, pass newPassword, newConfirmPassword to method
                        changeNewPassword(newPassword.getText().toString(), newConfirmPassword.getText().toString());
                    } else {
                        // Else show the firebase error in Toast
                        progressBar.setVisibility(View.GONE);   // Disable progress bar
                        Toast.makeText(UpdatePasswordActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Method to take care of updating user's password field in realtime database
    private void updateDatabase(String newPassword){
        // Create an Map to update user's password field
        final Map<String, Object> info = new HashMap<>();
        info.put("password", newPassword);

        // All users data is stored as all_users_info -> Uid
        database.child("all_users_info/" +fAuth.getCurrentUser().getUid()).updateChildren(info).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Show password changed toast if task is successful
                if (task.isSuccessful()){
                    // Close current activity after toast
                    Toast.makeText(UpdatePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UpdatePasswordActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);   // Disable progress bar
            }
        });
    }

    // Method to take care of sending password reset email to current user's registered email
    private void sendEmail(String emailAddress){
        // call firebase sendPasswordResetEmail() method to send password reset email to current user
        progressBar.setVisibility(View.VISIBLE);
        fAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Show "Email sent successfully" toast to the user
                if (task.isSuccessful()){
                    Toast.makeText(UpdatePasswordActivity.this, "Password reset email sent to "+emailAddress, Toast.LENGTH_SHORT).show();
                    finish();   // Close current activity
                } else {
                    Toast.makeText(UpdatePasswordActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE); // Disable the progress bar
            }
        });
    }
}