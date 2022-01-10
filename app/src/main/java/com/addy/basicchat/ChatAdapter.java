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
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;

public class ChatAdapter extends FirebaseRecyclerAdapter<Message, ChatAdapter.ChatViewHolder> {

    private Context context;
    private String currentUid;
    private Activity activity;

    public ChatAdapter(@NonNull FirebaseRecyclerOptions<Message> options, Context context, Activity activity) {
        super(options);
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Message model) {
        /* bind model data with message view
           Change text message view background if its senderUid is same as current Uid (to make diff between sender/receiver messages)
        */
        if(!model.getSenderUid().equals(currentUid)){
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
        if(model.getMessageSeen()){
            holder.singleTick.setVisibility(View.GONE);
            holder.doubleTick.setVisibility(View.VISIBLE);
        }

        holder.messageText.setText(model.getMessage());  // Set the message from model
        holder.messageTime.setText(getFormattedTime(model.getTime()));
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate single_message_view and pass it to the ViewHolder
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_view, parent, false);
        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        return new ChatViewHolder(view);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

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
