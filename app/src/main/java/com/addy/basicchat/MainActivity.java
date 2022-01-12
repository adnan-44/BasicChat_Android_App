package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // GUI stuff
    private Button login;
    private RecyclerView homeRecyclerView;
    private FloatingActionButton addNewChat;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    // Other stuff
    private ShowUsersAdapter adapter;
    private ArrayList<DataSnapshot> chatUsers;   // to store chat users info
    private static final int ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init vars
        login = findViewById(R.id.login_button);
        homeRecyclerView = findViewById(R.id.home_recycler_view);
        toolbar = findViewById(R.id.toolbar);
        addNewChat = findViewById(R.id.add_new_chat);
        progressBar = findViewById(R.id.progress_bar);
        chatUsers = new ArrayList<>();
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Only show "Start chat" button if an user is already signed in
        if(fAuth.getCurrentUser() == null){
            login.setVisibility(View.VISIBLE);
            homeRecyclerView.setVisibility(View.GONE);
            addNewChat.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            login.setVisibility(View.GONE);
            homeRecyclerView.setVisibility(View.VISIBLE);
            addNewChat.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

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

        if(fAuth.getCurrentUser() != null){
            // Set LinearLayout as layout manager for our recyclerView
            homeRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            // call showUsers method to take care of recycler adapter stuff
            showUsers();
        }

        // open AddNewChat Activity to add new chat
        addNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open AddNewChat activity to add new users to your chat
                Intent intent = new Intent(MainActivity.this, AddNewChatActivity.class);
                startActivity(intent);
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

            case R.id.profile_menu:
                // Open MyProfileActivity on "My Profile" option select
                Intent myProfile = new Intent(MainActivity.this, MyProfileActivity.class);
                startActivity(myProfile);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Method to get and set chat users info into chatUsers list
    private void showUsers(){
        // get the users info from (p2p_users->uid->)
        progressBar.setVisibility(View.VISIBLE);
        database.child("p2p_users/"+fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear any previous data from the chatUsers list
                chatUsers.clear();

                // Get all the children and insert one by one, first get chat usersUid
                for(DataSnapshot snap : snapshot.getChildren()){
                    chatUsers.add(snap);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        adapter = new ShowUsersAdapter(MainActivity.this, getApplicationContext(), chatUsers);
        homeRecyclerView.setAdapter(adapter);
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
