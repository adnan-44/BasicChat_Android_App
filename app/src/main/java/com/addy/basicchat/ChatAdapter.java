package com.addy.basicchat;

import android.app.Activity;
import android.content.Context;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private Activity activity;
    private List<Pair<String, Message>> allMessages;     //  List to store the model of class Message

    // Firebase stuff
    private String currentUid, uniqueId;
    private DatabaseReference database;

    // Other stuff
    private static final int LEFT_SIDE = 1;
    private static final int RIGHT_SIDE = 0;

    public ChatAdapter(Context context, Activity activity, List allMessages, String uniqueId){
        this.context = context;
        this.activity = activity;
        this.allMessages = allMessages;
        this.uniqueId = uniqueId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // If viewType is equals to LEFT_SIDE, then use the left_message_view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if(viewType == RIGHT_SIDE){
            // Then inflate right_message_view, because its card is placed in left side of parent
            view = inflater.inflate(R.layout.right_message_view, parent, false);
        } else {
            // Else inflate the left_message_view, because its card is placed in right side of parent
            view = inflater.inflate(R.layout.left_message_view, parent, false);
        }

        // Get some firebase reference
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference();
        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // Bind the data (from lists) to the message cards

        // Only show single/double tick in imageView (if message is sent by currentUser)
        if (holder.getItemViewType() == RIGHT_SIDE){
            holder.tickImage.setVisibility(View.VISIBLE);
        }

        /* If currentUid and senderUid (message) are not equal, means receiver is seeing the message
         * so simply update the messageSeen field to true, to indicate that message is seen */

        if (!currentUid.equals(allMessages.get(position).second.getSenderUid())){
            // Create an map to change messageSeen to set true (to show that message is seen by users)
            Map<String, Object> updateMessageSeen = new HashMap<>();
            updateMessageSeen.put("messageSeen", Boolean.valueOf("true"));
            database.child("p2p_chats/" + uniqueId).child(allMessages.get(position).first).updateChildren(updateMessageSeen);
        }

        // Update tickImage according to messageSeen value, if messageSeen is true, then show double_tick
        if (allMessages.get(position).second.getMessageSeen()){
            holder.tickImage.setImageResource(R.drawable.ic_double_tick);
        } else {
            holder.tickImage.setImageResource(R.drawable.ic_single_tick);   // set single tick
        }

        holder.messageText.setText(allMessages.get(position).second.getMessage());  // Set the message from model
        holder.messageTime.setText(getFormattedTime(allMessages.get(position).second.getTime()));
    }

    @Override
    public int getItemViewType(int position) {
        // Return itemViewType according to sender receiver Uid
        if (allMessages.get(position).second.getSenderUid().equals(currentUid)){
            // If the currentUid is same as senderUid(in message), then use right_message_view
            return RIGHT_SIDE;
        } else {
            // else use the receiver_message_view
            return LEFT_SIDE;
        }
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
        ImageView tickImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mainMessageLayout = itemView.findViewById(R.id.main_message_layout);
            messageText = itemView.findViewById(R.id.message_textView);
            messageTime = itemView.findViewById(R.id.message_time);
            messageCard = itemView.findViewById(R.id.single_message_card);
            tickImage = itemView.findViewById(R.id.tick_image);
        }
    }

    // Method to get formatted date string from date-ime stamp
    public static String getFormattedTime(String user_created_time){
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        return formatter.format(Long.valueOf(user_created_time)).toUpperCase();
    }
}
