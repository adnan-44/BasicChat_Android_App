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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                holder.username.setText(snapshot.getValue(UserInformation.class).getFull_name());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Views from single_user_item layout
        TextView username;
        CardView single_user_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.person_username);
            single_user_layout = itemView.findViewById(R.id.single_user_layout);
        }
    }
}
