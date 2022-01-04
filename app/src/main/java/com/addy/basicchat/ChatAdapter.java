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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public class ChatAdapter extends FirebaseRecyclerAdapter<Message, ChatAdapter.ChatViewHolder> {
//
//    private Context context;
//    private String currentUid;
//    private Activity activity;
//
//    public ChatAdapter(@NonNull FirebaseRecyclerOptions<Message> options, Context context, Activity activity) {
//        super(options);
//        this.context = context;
//        this.activity = activity;
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Message model) {
//        /* bind model data with message view
//           Change text message view background if its senderUid is same as current Uid (to make diff between sender/receiver messages)
//        */
//        if(!model.getSenderUid().equals(currentUid)){
//            // change background color
//            holder.messageCard.setCardBackgroundColor(Color.parseColor("#FF6200EE"));
//            holder.messageText.setTextColor(Color.parseColor("#FFFFFF"));
//            holder.messageTime.setTextColor(Color.parseColor("#FFFFFF"));
//
//            // experimental
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageCard.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            layoutParams.setMargins(10,10,10,0);
//            holder.mainMessageLayout.setLayoutParams(layoutParams);
//            // end exp code
//        }
//        holder.messageText.setText(model.getMessage());
//        holder.messageTime.setText(getFormattedTime(model.getTime()));
//    }
//
//    @NonNull
//    @Override
//    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Inflate single_message_view and pass it to the ViewHolder
//        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_view, parent, false);
//        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);
//        return new ChatViewHolder(view);
//    }
//
//    public class ChatViewHolder extends RecyclerView.ViewHolder {
//
//        // GUI stuff
//        TextView messageText, messageTime;
//        CardView messageCard;
//        RelativeLayout mainMessageLayout;
//        ImageView singleTick, doubleTick;
//
//        public ChatViewHolder(@NonNull View itemView) {
//            super(itemView);
//            mainMessageLayout = itemView.findViewById(R.id.main_message_layout);
//            messageText = itemView.findViewById(R.id.message_textView);
//            messageTime = itemView.findViewById(R.id.message_time);
//            messageCard = itemView.findViewById(R.id.single_message_card);
//            singleTick = itemView.findViewById(R.id.single_tick);
//            doubleTick = itemView.findViewById(R.id.double_tick);
//        }
//    }
//
//    // Method to get formatted date string from date-ime stamp
//    public String getFormattedTime(String user_created_time){
//        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
//        return formatter.format(Long.valueOf(user_created_time));
//    }
//}

// Temporary experimental adapter class
//public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
//
//    private Context context;
//    private Activity activity;
//    private List<Pair<String, Message>> allMessages;     //  List to store the model of class Message
//
//    // Firebase stuff
//    private String currentUid, uniqueId;
//    private DatabaseReference database;
//
//    public ChatAdapter(Context context, Activity activity, List allMessages, String uniqueId){
//        this.context = context;
//        this.activity = activity;
//        this.allMessages = allMessages;
//        this.uniqueId = uniqueId;
//    }
//
//    @NonNull
//    @Override
//    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_view, parent, false);
//        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        database = FirebaseDatabase.getInstance().getReference();
//        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);
//        return new ChatViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
//        /* bind model data with message view
//           Change text message view background if its senderUid is same as current Uid (to make diff between sender/receiver messages)
//        */
//
//        if (!currentUid.equals(allMessages.get(position).second.getSenderUid())){
//            // Create an map to change messageSeen to set true (to show that message is seen by users)
//            Map<String, Object> updateMessageSeen = new HashMap<>();
//            updateMessageSeen.put("messageSeen", Boolean.valueOf("true"));
//            database.child("p2p_chats/" + uniqueId).child(allMessages.get(position).first).updateChildren(updateMessageSeen);
//        }
//
//        if(allMessages.get(position).second.getSenderUid().equals(currentUid)){
//            // change background color
//            holder.messageCard.setCardBackgroundColor(Color.parseColor("#FF6200EE"));
//            holder.messageText.setTextColor(Color.parseColor("#FFFFFF"));
//            holder.messageTime.setTextColor(Color.parseColor("#FFFFFF"));
//
//            // experimental
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.messageCard.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            layoutParams.setMargins(10,10,10,0);
//            holder.mainMessageLayout.setLayoutParams(layoutParams);
//            // end exp code
//        }
//
//        // if message is already seen, means messageSeen value is true so show the double tick
//        if(allMessages.get(position).second.getMessageSeen()){
//            holder.singleTick.setVisibility(View.GONE);
//            holder.doubleTick.setVisibility(View.VISIBLE);
//        }
//
//        holder.messageText.setText(allMessages.get(position).second.getMessage());
//        holder.messageTime.setText(getFormattedTime(allMessages.get(position).second.getTime()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return allMessages.size();
//    }
//
//    public class ChatViewHolder extends RecyclerView.ViewHolder{
//        // GUI stuff
//        TextView messageText, messageTime;
//        CardView messageCard;
//        RelativeLayout mainMessageLayout;
//        ImageView singleTick, doubleTick;
//
//        public ChatViewHolder(@NonNull View itemView) {
//            super(itemView);
//            mainMessageLayout = itemView.findViewById(R.id.main_message_layout);
//            messageText = itemView.findViewById(R.id.message_textView);
//            messageTime = itemView.findViewById(R.id.message_time);
//            messageCard = itemView.findViewById(R.id.single_message_card);
//            singleTick = itemView.findViewById(R.id.single_tick);
//            doubleTick = itemView.findViewById(R.id.double_tick);
//        }
//    }
//
//    // Method to get formatted date string from date-ime stamp
//    public static String getFormattedTime(String user_created_time){
//        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
//        return formatter.format(Long.valueOf(user_created_time)).toUpperCase();
//    }
//}

// Temporary experimental adapter class
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private Activity activity;
    private List<Pair<String, Message>> allMessages;     //  List to store the model of class Message

    // Firebase stuff
    private String currentUid, uniqueId;
    private DatabaseReference database;

    // Other stuff
    private static final int SENDER_SIDE = 1;
    private static final int RECEIVER_SIDE = 0;

    public ChatAdapter(Context context, Activity activity, List allMessages, String uniqueId){
        this.context = context;
        this.activity = activity;
        this.allMessages = allMessages;
        this.uniqueId = uniqueId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // If viewType is equals to RECEIVER_SIDE, then use the single_receiver_message_view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if(viewType == RECEIVER_SIDE){
            // Then inflate receiver_message_view, because its card is placed in right side of parent
            view = inflater.inflate(R.layout.single_receiver_message_view, parent, false);
        } else {
            // Else inflate the sender_message_view, because its card is placed in left side of parent
            view = inflater.inflate(R.layout.single_sender_message_view, parent, false);
        }

        // Get some firebase reference
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference();
        activity.findViewById(R.id.progress_bar).setVisibility(View.GONE);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // bind model data with message view

        // Only show single/double tick in imageView (if message is sent by currentUser)
        if (holder.getItemViewType() == SENDER_SIDE){
            holder.tickImage.setVisibility(View.VISIBLE);
        }

        // Update messageSeen to "true", senderUid and currentUid are not equal (means other user seen the message)
        if (!currentUid.equals(allMessages.get(position).second.getSenderUid())){
            // Create an map object to update the messageSeen field in database
            Map<String, Object> updateMessageSeen = new HashMap<>();
            updateMessageSeen.put("messageSeen", Boolean.valueOf("true"));
            database.child("p2p_chats/"+uniqueId).child(allMessages.get(position).first).updateChildren(updateMessageSeen);
        }

        // Update tickImage according to messageSeen value, if messageSeen is true, then show double_tick
        if (allMessages.get(position).second.getMessageSeen()){
            holder.tickImage.setImageResource(R.drawable.ic_double_tick);
        } else {
            holder.tickImage.setImageResource(R.drawable.ic_single_tick);   // set single tick
        }
        
        holder.messageText.setText(allMessages.get(position).second.getMessage());
        holder.messageTime.setText(getFormattedTime(allMessages.get(position).second.getTime()));
    }

    @Override
    public int getItemViewType(int position) {
        // Return itemViewType according to sender receiver Uid
        if (allMessages.get(position).second.getSenderUid().equals(currentUid)){
            // If the currentUid is same as senderUid(in message), then use sender_message_view
            return SENDER_SIDE;
        } else {
            // else use the receiver_message_view
            return RECEIVER_SIDE;
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
