package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    // GUI stuff
    private RecyclerView homeRecyclerView;
    private FloatingActionButton addNewChat;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    // Other stuff
    private ShowUsersAdapter adapter;
    private ArrayList<DataSnapshot> chatUsers;   // to store chat users info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Init vars
        homeRecyclerView = findViewById(R.id.home_recycler_view);
        addNewChat = findViewById(R.id.add_new_chat);
        chatUsers = new ArrayList<>();
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Set LinearLayout as layout manager for our recyclerView
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

        // call showUsers method to take care of recycler adapter stuff
        showUsers();

        // open AddNewChat Activity to add new chat
        addNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open AddNewChat activity to add new users to your chat
                Intent intent = new Intent(HomeActivity.this, AddNewChatActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to get and set chat users info into chatUsers list
    private void showUsers(){
        // get the users info from (p2p_users->uid->)
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        adapter = new ShowUsersAdapter(HomeActivity.this, getApplicationContext(), chatUsers);
        homeRecyclerView.setAdapter(adapter);
    }
}