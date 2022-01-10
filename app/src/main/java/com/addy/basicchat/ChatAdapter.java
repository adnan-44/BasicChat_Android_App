package com.addy.basicchat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private Activity activity;
    private List<Pair<String, Message>> allMessages;     //  List to store the model of class Message

    // Firebase stuff
    private String currentUid, uniqueId;
    private DatabaseReference database;

    public ChatAdapter(Context context, Activity activity, List allMessages, String uniqueId){
        this.context = context;
        this.activity = activity;
        this.allMessages = allMessages;
        this.uniqueId = uniqueId;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        /* bind model data with message view
           Change text message view background if its senderUid is same as current Uid (to make diff between sender/receiver messages)
        */

        if(!allMessages.get(position).second.getSenderUid().equals(currentUid)){
            // change background color, make sender message card different that receivers
            holder.messageCard.setCardBackgroundColor(Color.parseColor("#FF6200EE"));
            holder.messageText.setTextColor(Color.parseColor("#FFFFFF"));
            holder.messageTime.setTextColor(Color.parseColor("#FFFFFF"));

            // Change the position of messageCard according to sender/receiver, so that user can differentiate between message cards
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageCard.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.setMargins(10,10,10,0);
            holder.mainMessageLayout.setLayoutParams(layoutParams);

        }

        // if message is already seen, means messageSeen value is true so show the double_tick and hide single_tick
        if(allMessages.get(position).second.getMessageSeen()){
            holder.singleTick.setVisibility(View.GONE);
            holder.doubleTick.setVisibility(View.VISIBLE);
        }

        holder.messageText.setText(allMessages.get(position).second.getMessage());  // Set the message from model
        holder.messageTime.setText(getFormattedTime(allMessages.get(position).second.getTime()));
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate single_message_view and pass it to the ViewHolder
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_view, parent, false);
        database = FirebaseDatabase.getInstance().getReference();
        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        return new ChatViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{
        // GUI stuff
        TextView messageText, messageTime;
        CardView messageCard;
        RelativeLayout mainMessageLayout;
        ImageView singleTick, doubleTick;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mainMessageLayout = itemView.findViewById(R.id.main_message_layout);
            messageText = itemView.findViewById(R.id.message_textView);
            messageTime = itemView.findViewById(R.id.message_time);
            messageCard = itemView.findViewById(R.id.single_message_card);
            singleTick = itemView.findViewById(R.id.single_tick);
            doubleTick = itemView.findViewById(R.id.double_tick);
        }
    }

    // Method to get formatted date string from date-ime stamp
    public static String getFormattedTime(String user_created_time){
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        return formatter.format(Long.valueOf(user_created_time)).toUpperCase();
    }
}
