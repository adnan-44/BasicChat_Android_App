package com.addy.basicchat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    // GUI stuff
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private ChatAdapter adapter;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private List<Pair<String, Message>> allMessages;  // To store the message from realtime database, will you in adapter
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    // Other stuff
    private UserInformation userInfo;
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
        allMessages = new ArrayList<>();    // initiate the list as arraylist

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUserInfo();  // Method to set user "full name" and "status" in Toolbar
        progressBar.setVisibility(View.VISIBLE);

        // calling showMessages method to get messages and show them to recyclerView
        showMessages();

        /* Use Query class to create Query to fetch data from Firebase database of chat, add valueEventListener
         * User chats are located at p2p_chats -> uniqueId
         * where uniqueId = "userUid+otherUid"
         */

        /*
        final Query query = FirebaseDatabase.getInstance().getReference("p2p_chats/"+uniqueId)
                .limitToLast(100);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE); // Dismiss the progress bar
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);  // Dismiss the progress bar
            }
        });

        // Build FirebaseRecycler options for Accounts Model using fetch query, it will be used by FirebaseRecyclerAdapter
        options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build();

        // Recycler view, set Linear layout manager, create our custom adapter using Firebase options
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        layoutManager.setStackFromEnd(true);    // Used to reverse item traversal and layout order.
        chatRecyclerView.setLayoutManager(layoutManager);

        // Attach adapter to recyclerView
        adapter = new ChatAdapter(options, getApplicationContext(), ChatActivity.this, uniqueId);
        chatRecyclerView.setAdapter(adapter);
        */

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
    private void setUserInfo() {
        // get the user name from database using usersUid
        database.child("all_users_info/" + userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the user data in userInfo model class and then update views accordingly
                UserInformation userInfo = snapshot.getValue(UserInformation.class);

                // get the full_name from snapshot and update the toolbar title
                final MaterialTextView personFullName = toolbar.findViewById(R.id.person_full_name);
                personFullName.setText(userInfo.getFull_name());

                // get the image_url from userInfo model, set image using Glide if it's not "default"
                final ImageView personImage = toolbar.findViewById(R.id.person_image);
                if (!userInfo.getImage_url().equals("default")) {
                    Glide.with(getApplicationContext()).load(userInfo.getImage_url()).circleCrop()
                            .into(personImage);
                }

                // Set user status, online or last seen, according to user's "status" field
                final MaterialTextView personStatus = toolbar.findViewById(R.id.person_status);
                if (!userInfo.getStatus().equals("online")) {
                    // If status is other than "online" then prepare a last seen string and set
                    String lastSeen = "last seen at " + ChatAdapter.getFormattedTime(userInfo.getStatus());
                    personStatus.setText(lastSeen);
                } else {
                    personStatus.setText(userInfo.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Method to take care sending of messages
    private void sendMessage() {

        // Don't do anything if message is empty
        if (!TextUtils.isEmpty(messageInput.getText().toString().trim())) {

            // Create and Message object, which will have message, time and senderUid
            final Message message = new Message();
            message.setMessage(messageInput.getText().toString());  // Message
            message.setSenderUid(fAuth.getCurrentUser().getUid());  // current User's Uid
            message.setMessageSeen(false);  // because message is not seen yet :)

            // Get current time
            message.setTime(String.valueOf(System.currentTimeMillis()));

            // save the messages in root->uid->userUid->chats
            database.child("p2p_chats/" + uniqueId).push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // if message is uploaded successfully then show toast
                    if (task.isSuccessful()) {
                        // Once message is uploaded, clear the editText so that new message can get compose
                        messageInput.getText().clear();
                    } else {
                        Toast.makeText(ChatActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Method to take care of getting messages from cloud database and show messages to recyclerView
    private void showMessages() {
        // start the loading animation
        progressBar.setVisibility(View.VISIBLE);

        // Now get the messages with their node key, and store them in list of "Pairs"
        database.child("p2p_chats/" + uniqueId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMessages.clear(); // clear any previous message data from the list

                // Only move forward if we get any data from snapshot
                if (snapshot != null) {
                    // If snapshot is not null, means there are messages in database
                    // now iterate over children snapshots
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        // now create an pair of (snap_key, snap_value)
                        Pair<String, Message> messageData = new Pair<>(snap.getKey(),
                                snap.getValue(Message.class));
                        allMessages.add(messageData);
                    }
                    // Update the adapter with updated data lists
                    progressBar.setVisibility(View.GONE);

                    // Scroll chatRecyclerView to last position on each new message
                    if (allMessages.size() > 0) {
                        // Only scroll if there's at least 1 element present in list
                        chatRecyclerView.smoothScrollToPosition(allMessages.size() - 1);
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Recycler view, set Linear layout manager, create our custom adapter using allMessages list, attach adapter to recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(getApplicationContext(), ChatActivity.this, allMessages, uniqueId);
        chatRecyclerView.setAdapter(adapter);

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