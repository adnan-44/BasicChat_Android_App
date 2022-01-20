package com.addy.basicchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowAllUsersAdapter extends RecyclerView.Adapter<ShowAllUsersAdapter.ViewHolder> {

    private Activity activity;
    private ProgressBar progressBar;
    private ArrayList<UserInformation> allUsers;    // List to store all users from realtime database

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    public ShowAllUsersAdapter(Activity activity, ArrayList allUsers, ProgressBar progressBar) {
        this.activity = activity;
        this.allUsers = allUsers;
        this.progressBar = progressBar;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_info_item, parent, false);
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference(); // get reference to our realtime database
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Bind data with views, get data from allUsers list
        holder.personFullName.setText(allUsers.get(position).getFull_name());
        holder.personBio.setText(allUsers.get(position).getBio());
        holder.personEmail.setText(allUsers.get(position).getEmail());

        // Use Glide library to get and set image (using image_url Uri, automatically)
        if (!allUsers.get(position).getImage_url().equals("default")) {
            // Only set if user don't have default image
            Glide.with(activity.getApplicationContext()).load(allUsers.get(position).getImage_url()).centerCrop()
                    .into(holder.profileImage);
        }

        // Open userFoundDialog on click of cards, so that user can add users to known chat or cancel operation
        holder.single_user_info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call userAlreadyKnown() method to check if user is already known or not, show Dialog accordingly
                userAlreadyKnown(allUsers.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return allUsers.size();
    }

    // Method to check whether that user is already known or not
    private void userAlreadyKnown(UserInformation user) {
        // If user is not in known list, then only userFoundDialog will be shown
        database.child("p2p_users/" + fAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get all the users contact in a list
                final ArrayList<String> knownUsers = new ArrayList<>();
                boolean alreadyKnown = false;   // To keep track whether the user is already in contact with current user
                for (DataSnapshot snap : snapshot.getChildren()) {
                    knownUsers.add(snap.getValue(String.class));
                    // If user is found in known users list, then set alreadyKnown to true
                    if (snap.getKey().equals(user.getUid())) {
                        // User is found, set alreadyKnown to true so that userFound dialog will not show
                        alreadyKnown = true;
                        progressBar.setVisibility(View.GONE);
                        // Stop here, don't check rest of the list
                        break;
                    }
                }

                // show userFoundDialog stuff according to alreadyKnown boolean
                showUserFoundDialog(user, alreadyKnown);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Method to take care of user found dialog box, Dialog box is used to show information about user
    public void showUserFoundDialog(UserInformation userInformation, boolean alreadyKnown) {
        // Create and setup dialog for AddNewChatActivity
        Dialog userFoundDialog = new Dialog(activity);

        // Use our custom layout for dialog box and set window height, width parameters as wrap_content
        userFoundDialog.setContentView(R.layout.user_found_dialog);
        userFoundDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userFoundDialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        // Get reference to user_found_dialog views and update them with userInformation
        final MaterialTextView fullName = userFoundDialog.findViewById(R.id.full_name);
        final MaterialTextView personEmail = userFoundDialog.findViewById(R.id.person_email);
        final MaterialTextView personBio = userFoundDialog.findViewById(R.id.person_bio);
        final ImageView personImage = userFoundDialog.findViewById(R.id.person_image);
        fullName.setText(userInformation.getFull_name());
        personEmail.setText(userInformation.getEmail());
        personBio.setText(userInformation.getBio());

        // If getImage_url() returns other value than "default", it means user have an profile image already or using default image
        if (!userInformation.getImage_url().equals("default")) {
            // Use Glide library to get and set image (using Uri, automatically) in personImage imageView
            Glide.with(activity).load(userInformation.getImage_url()).centerCrop().into(personImage);
        }

        // Disable the "Add" button is user is already added
        if (alreadyKnown) {
            // Get reference to the add_button, change button text to "Already added" and disbale the button
            final MaterialButton addButton = userFoundDialog.findViewById(R.id.add_button);
            addButton.setText("Already added");
            addButton.setEnabled(false);
        } else {
            // else add click listener to add_button to add user to known users list in database
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
                            if (task.isSuccessful()) {
                                // now save the same uniqueUid for otherUid too
                                Map<String, Object> otherUser = new HashMap<>();
                                otherUser.put(fAuth.getCurrentUser().getUid(), uniqueId);
                                database.child("p2p_users").child(userInformation.getUid()).updateChildren(otherUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // if task is successful then show "user found" toast
                                        if (task.isSuccessful()) {
                                            Toast.makeText(activity, "User added", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                            userFoundDialog.dismiss();
                                            activity.finish();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(activity, "Failed to add", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(activity, "Failed to add", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Views from single_user_info layout
        MaterialTextView personFullName, personBio, personEmail;
        MaterialCardView single_user_info_layout;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personFullName = itemView.findViewById(R.id.person_full_name);
            personBio = itemView.findViewById(R.id.person_bio);
            personEmail = itemView.findViewById(R.id.person_email);
            profileImage = itemView.findViewById(R.id.person_image);
            single_user_info_layout = itemView.findViewById(R.id.single_user_info_layout);
        }
    }
}
