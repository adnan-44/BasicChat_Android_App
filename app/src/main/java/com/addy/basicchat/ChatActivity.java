package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    // GUI stuff
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private Toolbar toolbar;
    private ProgressBar progressBar;

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

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
        uniqueId = getIntent().getStringExtra("uniqueId");    // Get chat uniqueId from calling intent

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar.setVisibility(View.VISIBLE);

        // click listeners implementation for send button onclick
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call sendMessage to send text message to cloud database
                sendMessage();
            }
        });
    }

    // Method to take care sending of messages
    private void sendMessage(){

        // Don't do anything if message is empty
        if (!TextUtils.isEmpty(messageInput.getText().toString().trim())){

            // Create and Message object, which will have message, time and senderUid
            final Message message = new Message();
            message.setMessage(messageInput.getText().toString());  // Message
            message.setSenderUid(fAuth.getCurrentUser().getUid());  // current User's Uid

            // Get current time
            message.setTime(String.valueOf(System.currentTimeMillis()));

            // save the messages in root->uid->userUid->chats
            database.child("p2p_chats/" + uniqueId).push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>(){
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
    }
}