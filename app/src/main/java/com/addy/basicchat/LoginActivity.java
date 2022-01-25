package com.addy.basicchat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // GUI stuff
    private EditText email, password;
    private MaterialTextView signup, forgot;
    private MaterialButton login;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init vars
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup_text);
        forgot = findViewById(R.id.forgot);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        fAuth = FirebaseAuth.getInstance();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Allow login button to sign firebase user into app
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);  // show loading screen
                // Sign firebase user into app using email password
                fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // close current activity after user logged in successfully
                                    // open MainActivity on successful account login
                                    Toast.makeText(LoginActivity.this, "User Logged in successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK, getIntent());
                                    finish();   // finish login activity
                                } else {
                                    Toast.makeText(LoginActivity.this, "Failed to sign-in", Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.GONE);   // Hide loading once operation is completed
                            }
                        });
            }
        });

        // Create new user on signup button click
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open CreateNewAccount activity using intents, to create new user
                Intent intent = new Intent(LoginActivity.this, CreateNewAccountActivity.class);
                startActivityForResult(intent, 1);
                finish();
            }
        });

        // show forgotPassword dialog on forgot text click
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // call showForgotPassword() method to take care of forgot password dialog
                showForgotPassword();
            }
        });
    }

    // Method to take care of forgot password dialog box, Dialog box is used to show email editText
    private void showForgotPassword() {
        // Create and setup dialog for current activity context
        Dialog forgotPasswordDialog = new Dialog(LoginActivity.this);

        // Use our custom layout for dialog box and set window height, width parameters as wrap_content
        forgotPasswordDialog.setContentView(R.layout.forgot_password_dialog);
        forgotPasswordDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        forgotPasswordDialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        // Get reference to forgot_password_dialog views
        final TextInputEditText emailInput = forgotPasswordDialog.findViewById(R.id.email_input);
        final MaterialButton forgotButton = forgotPasswordDialog.findViewById(R.id.forgot);

        // Add onClick listener to the "forgot" button in forgot_password_dialog
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Proceed only if email input field in not empty
                if (!emailInput.getText().toString().trim().equals("")){
                    // call sendPasswordReset firebase method to send resend email to given email address
                    fAuth.sendPasswordResetEmail(emailInput.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Show "Email sent successfully" toast to the user
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Password reset email sent to "+emailInput.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                            // Dismiss the dialog box
                            forgotPasswordDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Enter email first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // show the dialog
        forgotPasswordDialog.show();
    }
}
