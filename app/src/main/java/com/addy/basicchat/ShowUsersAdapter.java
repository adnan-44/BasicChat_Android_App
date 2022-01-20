package com.addy.basicchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowUsersAdapter extends RecyclerView.Adapter<ShowUsersAdapter.ViewHolder> {

    private Activity activity;
    private Context context;
    private ArrayList<DataSnapshot> chatUsers;   // To save chat users for current user

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    public ShowUsersAdapter(Activity activity, Context context, ArrayList chatUsers) {
        this.activity = activity;
        this.context = context;
        this.chatUsers = chatUsers;
    }

    @NonNull
    @Override
    public ShowUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.single_user_item, parent, false);
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference(); // get reference to our realtime database
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowUsersAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // bind the holder with userInformation data , get the user chat's userinfo from realtime database (root->all_users_info)
        database.child("all_users_info").child(chatUsers.get(position).getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // set the user info in userInformation model and update textview
                UserInformation userInfo = snapshot.getValue(UserInformation.class);
                holder.username.setText(userInfo.getFull_name());

                // call getAndSetLastMessage() method to set last message from chats(database)
                // Pass the lastSentMessage & lastSeenMessageStatus reference (to set the text), also pass user full_name
                getAndSetLastMessage(position, holder.lastSentMessage, holder.lastSendMessageTime,
                        holder.lastSentMessageStatus, holder.unreadMessageCount);

                // Call getAndSetProfileImage() method to set User's profile image from firebase storage
                getAndSetProfileImage(holder.profileImage, userInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Open ChatActivity onclick on card
        holder.single_user_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add current user uniqueId and pass them to chatActivity
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.putExtra("userUid", chatUsers.get(position).getKey());
                intent.putExtra("uniqueId", chatUsers.get(position).getValue(String.class));
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    // Method to get the last message, and set message to last_message textview
    private void getAndSetLastMessage(int position, MaterialTextView lastMessageView, MaterialTextView lastMessageTimeView, ImageView lastSentMessageStatus, MaterialTextView unreadMessageCount) {
        database.child("p2p_chats/" + chatUsers.get(position).getValue(String.class)).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                // If snapshot is not null, then get message from snapshot
                if (snap.exists()) {
                    // Going nesting because message are "pushed" by push() method
                    for (DataSnapshot data : snap.getChildren()) {
                        // If the sender of message is currently logged user, then add "You: " prefix
                        Message lastSentMessage = data.getValue(Message.class);
                        if (lastSentMessage.getSenderUid().equals(fAuth.getCurrentUser().getUid())) {
                            lastMessageView.setText("You: " + lastSentMessage.getMessage());
                        } else {
                            lastMessageView.setText(lastSentMessage.getMessage());
                        }
                        // Show last sent message time also
                        lastMessageTimeView.setText(ChatAdapter.getFormattedTime(lastSentMessage.getTime()));

                        // set tick according to message status, if message is seen already, then show double tick
                        if (lastSentMessage.getMessageSeen()) {
                            lastSentMessageStatus.setImageResource(R.drawable.ic_double_tick);
                        } else if (!lastSentMessage.getSenderUid().equals(fAuth.getCurrentUser().getUid())) {
                            // If message is not seen yet, and last message sender is "other" person, then show unread message count
                            showUnreadMessageCount(position, unreadMessageCount, lastMessageView);
                            lastSentMessageStatus.setVisibility(View.INVISIBLE);

                            // Also set accent color to lastSentMessageTime textView
                            lastMessageTimeView.setTextColor(activity.getResources().getColor(R.color.accent));
                        } else {
                            // Else simply show single tick image in message status imageView
                            lastSentMessageStatus.setImageResource(R.drawable.ic_single_tick_left);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Method to get person's profile image and set into person_image
    private void getAndSetProfileImage(ImageView profileImage, UserInformation userInfo) {

        // If getImage_url value is not "default", then show image using Glide library
        if (!userInfo.getImage_url().equals("default")) {
            // Use glide library to get and set image (using Uri, automatically) in profileImage imageView
            Glide.with(context).load(userInfo.getImage_url()).circleCrop().into(profileImage);
        }

    }

    // Method to check how many unread message left, and show them accordingly
    private void showUnreadMessageCount(int position, MaterialTextView unreadMessageCount, MaterialTextView lastMessageView) {
        // Create a query to get only those messages, whose messageSeen is "false" using equalTo() method
        final Query query = database.child("p2p_chats/" + chatUsers.get(position).getValue(String.class)).orderByChild("messageSeen").equalTo(false);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* Get the number of children (no. of messages that are not seen yet)
                   set that value in unreadMessageCount textView, and make that textView Visible */
                unreadMessageCount.setText(String.valueOf(snapshot.getChildrenCount()));
                unreadMessageCount.setVisibility(View.VISIBLE);

                // Change the lastSentMessage textStyle to "bold", and check textColor, so that user can differentiate in unreadMessage Text
                lastMessageView.setTypeface(null, Typeface.BOLD);
                lastMessageView.setTextColor(activity.getResources().getColor(R.color.accent));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Views from single_user_item layout
        MaterialTextView username, lastSentMessage, lastSendMessageTime, unreadMessageCount;
        CardView single_user_layout;
        ImageView lastSentMessageStatus, profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.person_username);
            lastSentMessage = itemView.findViewById(R.id.last_sent_message);
            lastSendMessageTime = itemView.findViewById(R.id.last_sent_message_time);
            lastSentMessageStatus = itemView.findViewById(R.id.last_sent_message_status);
            profileImage = itemView.findViewById(R.id.person_image);
            unreadMessageCount = itemView.findViewById(R.id.unread_message_count);
            single_user_layout = itemView.findViewById(R.id.single_user_layout);
        }
    }
}
