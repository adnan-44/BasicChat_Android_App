package com.addy.basicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddNewChatActivity extends AppCompatActivity {

    // GUI stuff
    private EditText email;
    private Button search;
    private Toolbar toolbar;
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
        email = findViewById(R.id.person_email);
        search = findViewById(R.id.search_button);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        allUsers = new ArrayList<>();

        // Set our custom toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase init
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference(); // database to point to root /

        // search the given account onclick of search button
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only proceed if user is not searching itself
                if (!fAuth.getCurrentUser().getEmail().equals(email.getText().toString())) {
                    searchUsers();  // Call searchUsers() method to search users
                }
            }
        });
    }

    // Method to take care of searching users within realtime database
    private void searchUsers(){
        // check if the provided users exits in platform databases or not, in realtime database, all users are located at /all_user_info location
        progressBar.setVisibility(View.VISIBLE);    // show loading till data is fetched
        database.child("all_users_info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();   // clear any previous data
                for( DataSnapshot snap : snapshot.getChildren()){
                    // add each user from snapshot into the list
                    allUsers.add(snap.getValue(UserInformation.class));
                }

                // now iterate over users list and check if given email exists or not
                for(UserInformation user : allUsers){
                    if (user.getEmail().equals(email.getText().toString())){
                        userFound = true;	// set this true to keep track
                        // First check current users known contact from root -> p2p_users -> uid
                        userAlreadyKnown(user);
                        break;
                    }
                }

                // remove the loading and show toast if user not found
                if(!userFound){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddNewChatActivity.this, "User not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Method to check whether that user is already known or not
    private void userAlreadyKnown(UserInformation user){
        // If user is not in known list, then only userFoundDialog will be shown
        database.child("p2p_users/"+fAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get all the users contact in a list
                final ArrayList<String> knownUsers = new ArrayList<>();
                boolean alreadyKnown = false;   // To keep track whether the user is already in contact with current user
                for (DataSnapshot snap : snapshot.getChildren()){
                    knownUsers.add(snap.getValue(String.class));
                    // If user is found in known users list, then set alreadyKnown to true
                    if(snap.getKey().equals(user.getUid())){
                        // User is found, set alreadyKnown to true so that userFound dialog will not show
                        alreadyKnown = true;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddNewChatActivity.this, "User already exists", Toast.LENGTH_LONG).show();
                        // Stop here, don't check rest of the list
                        break;
                    }
                }

                // show userFoundDialog stuff if user is not in known user
                if (!alreadyKnown) {
                    progressBar.setVisibility(View.GONE);

                    // Show Dialog with user information, so that user can add that "user" or cancel operation
                    showUserFoundDialog(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Method to take care of user found dialog box, Dialog box is used to show information about user
    public void showUserFoundDialog(UserInformation userInformation){
        // Create and setup dialog for AddNewChatActivity
        Dialog userFoundDialog = new Dialog(AddNewChatActivity.this);

        // Use our custom layout for dialog box and set window height, width parameters as wrap_content
        userFoundDialog.setContentView(R.layout.user_found_dialog);
        userFoundDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userFoundDialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        // Get reference to user_found_dialog views and update them with userInformation
        final TextView fullName = userFoundDialog.findViewById(R.id.full_name);
        final TextView personEmail = userFoundDialog.findViewById(R.id.person_email);
        final TextView personBio = userFoundDialog.findViewById(R.id.person_bio);
        final ImageView personImage = userFoundDialog.findViewById(R.id.person_image);
        fullName.setText(userInformation.getFull_name());
        personEmail.setText(userInformation.getEmail());
        personBio.setText(userInformation.getBio());

        // If getImage_url() returns other value than "default", it means user have an profile image already or using default image
        if (!userInformation.getImage_url().equals("default")){
            // Use Glide library to get and set image (using Uri, automatically) in personImage imageView
            Glide.with(AddNewChatActivity.this).load(userInformation.getImage_url()).into(personImage);
        }

        // click listener for "add" choice button
        userFoundDialog.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);    // start loading till the process completes

                /* Once a user add another user to their contact/chats, then create a node in p2p_users for both
                *  the users, like this, take uniqueId = user_uid+other_uid
                *  p2p_users -> user_uid -> other_uid -> uniqueId and
                *  p2p_users -> other_uid -> user_uid -> uniqueId
                */
                String uniqueId = fAuth.getCurrentUser().getUid() + userInformation.getUid();

                // Create a new child node in (root->p2p_users->user_uid-> uid), so that it'll be available fo known user
                Map<String, Object> user = new HashMap<>();
                user.put(userInformation.getUid(), uniqueId);

                database.child("p2p_users").child(fAuth.getCurrentUser().getUid()).updateChildren(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // if task is completed successfully
                        if (task.isSuccessful()){
                            // now save the same uniqueUid for otherUid too
                            Map<String, Object> otherUser = new HashMap<>();
                            otherUser.put(fAuth.getCurrentUser().getUid(), uniqueId);
                            database.child("p2p_users").child(userInformation.getUid()).updateChildren(otherUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // if task is successful then show "user found" toast
                                    if (task.isSuccessful()){
                                        Toast.makeText(AddNewChatActivity.this, "User added", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        userFoundDialog.dismiss();
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(AddNewChatActivity.this, "Failed to add", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddNewChatActivity.this, "Failed to add", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        // dismiss the dialog if "cancel" button is selected
        userFoundDialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog on click
                userFoundDialog.dismiss();
            }
        });

        // show the dialog
        userFoundDialog.show();
    }
}
