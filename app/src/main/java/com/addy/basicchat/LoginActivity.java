package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // GUI stuff
    private EditText email, password;
    private TextView signup;
    private Button login;
    private Toolbar toolbar;

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
        toolbar = findViewById(R.id.toolbar);
        fAuth = FirebaseAuth.getInstance();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Allow login button to sign firebase user into app
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign firebase user into app using email password
                fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    // close current activity after user logged in successfully
                                    // open MainActivity on successful account login
                                    Toast.makeText(LoginActivity.this, "User Logged in successfully", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK, getIntent());
                                    finish();   // finish login activity
                                } else {
                                    Toast.makeText(LoginActivity.this, "Failed to sign-in", Toast.LENGTH_LONG).show();
                                }
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
    }
}
