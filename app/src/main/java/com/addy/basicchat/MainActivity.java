package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // GUI stuff
    private Button login;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Other stuff
    private static final int ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init vars
        login = findViewById(R.id.login_button);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);

        // Login firebase user onclick on login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open LoginActivity to login firebase user
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_menu:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(MainActivity.this, "User logged out", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // inflate our custom menu item
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            // Start same activity using intents then finish them. Better than recreate
            Intent refresh_intent = new Intent(this, MainActivity.class);
            startActivity(refresh_intent);
            finish();
        }
    }
}
