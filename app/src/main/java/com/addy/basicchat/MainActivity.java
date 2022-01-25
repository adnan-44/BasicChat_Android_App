package com.addy.basicchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_REQUEST_CODE = 1;
    // GUI stuff
    private MaterialButton login;
    private RecyclerView homeRecyclerView;
    private FloatingActionButton addNewChat;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;
    // Other stuff
    private ShowUsersAdapter adapter;
    private ArrayList<DataSnapshot> chatUsers;   // to store chat users info

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
        if (fAuth.getCurrentUser() == null) {
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

        if (fAuth.getCurrentUser() != null) {
            // Set LinearLayout as layout manager for our recyclerView
            homeRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            // call showUsers method to take care of recycler adapter stuff
            showUsers();

            // open PendingAccount activity if user's email is not verified yet
            if (!fAuth.getCurrentUser().isEmailVerified()){
                Intent intent = new Intent(MainActivity.this, PendingAccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear previous activity from backstack
                startActivity(intent);
            }
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu:
                // Call signOutUser() method to sign-out current user and update their "status" in realtime database
                signOutUser();
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

    // Method to sign-out current user and change their "status" in realtime database
    private void signOutUser(){
        // Change user's status once user is logged out
        if (fAuth.getCurrentUser() != null) {
            // Create a Map object to update user status, get time and update in "status" field
            final Map<String, Object> updateStatus = new HashMap<>();
            updateStatus.put("status", String.valueOf(System.currentTimeMillis()));

            // If we don't update their "status" right now, then it'll gonna show "online" to other users
            // because while MainActivity is loaded (user's status is change to "online", from onStart())
            database.child("all_users_info/" + fAuth.getCurrentUser().getUid()).updateChildren(updateStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // If task is successful, then sign-out then current user
                    if (task.isSuccessful()){
                        // Use firebase signOut() method to sign-out current user, and reload/open MainActivity again
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(MainActivity.this, "User logged out", Toast.LENGTH_SHORT).show();
                    } else {
                        // if task gets failed then show the error to the user
                        Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // If no user is signed-in then show "no user signed-in" toast to the user
            Toast.makeText(MainActivity.this, "No user signed-in", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get and set chat users info into chatUsers list
    private void showUsers() {
        // get the users info from (p2p_users->uid->)
        progressBar.setVisibility(View.VISIBLE);
        database.child("p2p_users/" + fAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear any previous data from the chatUsers list
                chatUsers.clear();

                // Get all the children and insert one by one, first get chat usersUid
                for (DataSnapshot snap : snapshot.getChildren()) {
                    chatUsers.add(snap);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        adapter = new ShowUsersAdapter(MainActivity.this, getApplicationContext(), chatUsers);
        homeRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Start same activity using intents then finish them. Better than recreate
            Intent refresh_intent = new Intent(this, MainActivity.class);
            startActivity(refresh_intent);
            finish();
        }
    }

    // Override onPause and onResume method to update userStatus
    @Override
    protected void onPause() {
        super.onPause();
        // Don't do anything is there is no firebase user signed in already
        if (fAuth.getCurrentUser() != null) {
            // Create a Map object to update user status, get time and update in "status" field
            final Map<String, Object> updateStatus = new HashMap<>();
            updateStatus.put("status", String.valueOf(System.currentTimeMillis()));
            database.child("all_users_info/" + fAuth.getCurrentUser().getUid()).updateChildren(updateStatus);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Don't do anything is there is no firebase user signed in already
        if (fAuth.getCurrentUser() != null) {
            // Create a Map object to update user status to "online" whenever activity starts
            final Map<String, Object> updateStatus = new HashMap<>();
            updateStatus.put("status", "online");

            // all_users_info -> Uid -> status in realtime database
            database.child("all_users_info/" + fAuth.getCurrentUser().getUid()).updateChildren(updateStatus);
        }
    }
}
