package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddNewChatActivity extends AppCompatActivity {

    // GUI stuff
    private EditText personFullName;
    private Toolbar toolbar;
    private RecyclerView allUsersRecycler;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    // Other stuff
    private ArrayList<UserInformation> allUsers;    // to store all users info temporarily
    private boolean userFound;  // To keep track whether user found or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_chat);

        // Init vars
        personFullName = findViewById(R.id.person_full_name);
        toolbar = findViewById(R.id.toolbar);
        allUsersRecycler = findViewById(R.id.all_users_recycler);
        progressBar = findViewById(R.id.progress_bar);
        allUsers = new ArrayList<>();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference(); // database to point to root /

        // Set LinearLayout as layout manager for our recyclerView
        allUsersRecycler.setLayoutManager(new LinearLayoutManager(AddNewChatActivity.this));

        // Search the users onTextChange in person_full_name editText
        personFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Whenever user type or remove something in editText, thing method will be automatically called
                searchUserByName(s.toString().toLowerCase().trim()); // call searchUserByName() method with lowercase string
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    // Method to search users by name in realtime database
    private void searchUserByName(String name){
        progressBar.setVisibility(View.VISIBLE);

        // Prepare a Query to search using fullName field
        final Query searchQuery = database.child("all_users_info").orderByChild("full_name");
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear(); // clear any previous data

                // Get data from snapshot, user info is store in nested form all_users_info -> uid
                for (DataSnapshot snap : snapshot.getChildren()){
                    // only add users to the list if they have name equal to user's input
                    UserInformation userInfo = snap.getValue(UserInformation.class);
                    if (userInfo.getFull_name().toLowerCase().contains(name) && !name.equals("") &&
                             !userInfo.getUid().equals(fAuth.getCurrentUser().getUid())){
                        allUsers.add(userInfo);
                    }
                }

                // Create adapter and attach to users recyclerView and pass allUsers list to it
                final ShowAllUsersAdapter adapter = new ShowAllUsersAdapter(AddNewChatActivity.this, allUsers, progressBar);
                allUsersRecycler.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);   // Dismiss progress bar
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
