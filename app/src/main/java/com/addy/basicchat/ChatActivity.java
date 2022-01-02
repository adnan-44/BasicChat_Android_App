package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    // GUI stuff
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private ChatAdapter adapter;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;
    private FirebaseRecyclerOptions<Message> options;

    // Other stuff
    private String userUid;
    private String uniqueId;    // To get chats from unique location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init vars
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_box_input);
        sendButton = findViewById(R.id.send_message);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        userUid = getIntent().getStringExtra("userUid");    // Get chat user's Uid from calling intent
        uniqueId = getIntent().getStringExtra("uniqueId");    // Get chat uniqueId from calling intent

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarTitle();  // Method to set user "full name" as toolbar title
        progressBar.setVisibility(View.VISIBLE);

        // Use Query class to create Query to fetch data from Firebase database of chat, add valueEventListener
        /* User chats are located at p2p_chats -> uniqueId
        *  where uniqueId = userUid+otherUid
        */

        final Query query = FirebaseDatabase.getInstance().getReference("p2p_chats/"+uniqueId)
                .limitToLast(100);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Build FirebaseRecycler options for Accounts Model using fetch query, it will be used by FirebaseRecyclerAdapter
        options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build();

        // Recycler view, set Linear layout manager, create our custom adapter using Firebase options, and attach adapter to recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(options, getApplicationContext(), ChatActivity.this);
        chatRecyclerView.setAdapter(adapter);

        // click listeners implementation for send button onclick
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call sendMessage to send text message to cloud database
                sendMessage();
            }
        });
    }

    // Method to set user "Full name" as title
    private void setToolbarTitle(){
        // get the user name from database using usersUid
        database.child("all_users_info/"+userUid+ "/full_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get the full_name from snapshot and update the toolbar title
                toolbar.setTitle(snapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Method to take care sending of messages
    private void sendMessage(){
        // Create and Message object, which will have message, time and senderUid

        final Message message = new Message();
        message.setMessage(messageInput.getText().toString());  // Message
        message.setSenderUid(userUid);  // User's Uid

        // Get current time
        message.setTime(String.valueOf(System.currentTimeMillis()));

        // save the messages in root->uid->userUid->chats
        database.child("p2p_chats/" + uniqueId).push().setValue(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // if message is uploaded successfully then show toast
                        if (task.isSuccessful()){
                            // Once message is uploaded, clear the editText so that new message can get compose
                            messageInput.getText().clear();
                        } else {
                            Toast.makeText(ChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();       // Start listen to data onStart of activity
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();        // Stop listening to data onStop of activity
    }

}