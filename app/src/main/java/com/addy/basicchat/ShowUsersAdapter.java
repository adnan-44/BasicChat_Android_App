package com.addy.basicchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowUsersAdapter extends RecyclerView.Adapter<ShowUsersAdapter.ViewHolder>{

    private Activity activity;
    private Context context;
    private ArrayList<DataSnapshot> chatUsers;   // To save chat users for current user

    // Firebase stuff
    private FirebaseAuth fAuth;
    private DatabaseReference database;

    public ShowUsersAdapter(Activity activity, Context context, ArrayList chatUsers){
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
    public void onBindViewHolder(@NonNull ShowUsersAdapter.ViewHolder holder, int position) {
        // bind the holder with userInformation data , get the user chat's userinfo from realtime database (root->all_users_info)
        database.child("all_users_info").child(chatUsers.get(position).getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // set the user info in userInformation model and update textview
                UserInformation userInfo = snapshot.getValue(UserInformation.class);
                holder.username.setText(userInfo.getFull_name());

                // call getAndSetLastMessage() method to set last message from chats(database)
                // Pass the lastSentMessage reference (to set the text), also pass user full_name
                getAndSetLastMessage(position, holder.lastSentMessage,
                        userInfo.getFull_name(), holder.lastSendMessageTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Views from single_user_item layout
        TextView username, lastSentMessage, lastSendMessageTime;
        CardView single_user_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.person_username);
            lastSentMessage = itemView.findViewById(R.id.last_sent_message);
            lastSendMessageTime = itemView.findViewById(R.id.last_sent_message_time);
            single_user_layout = itemView.findViewById(R.id.single_user_layout);
        }
    }

    // Method to get the last message, and set message to last_message textview
    private void getAndSetLastMessage(int position, TextView lastMessageView, String fullName, TextView lastMessageTimeView){
        database.child("p2p_chats/"+chatUsers.get(position).getValue(String.class)).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                // If snapshot is not null, then get message from snapshot
                if(snap.exists()){
                    // Going nesting because message are "pushed" by push() method
                    for(DataSnapshot data : snap.getChildren()){
                        // If the sender of message is currently logged user, then add "You: " prefix
                        Message lastSentMessage = data.getValue(Message.class);
                        if(lastSentMessage.getSenderUid().equals(fAuth.getCurrentUser().getUid())){
                            lastMessageView.setText("You: "+lastSentMessage.getMessage());
                        } else {
                            lastMessageView.setText(lastSentMessage.getMessage());
                        }
                        // Show last sent message time also
                        lastMessageTimeView.setText(ChatAdapter.getFormattedTime(lastSentMessage.getTime()));
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
