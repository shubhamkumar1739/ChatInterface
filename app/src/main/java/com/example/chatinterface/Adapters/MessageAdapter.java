package com.example.chatinterface.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatinterface.R;
import com.example.chatinterface.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.BLACK;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;

    }



    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView message_sender_picture,message_receiver_picture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_msg_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_msg_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            message_sender_picture = itemView.findViewById(R.id.msg_sender_image_view);
            message_receiver_picture = itemView.findViewById(R.id.msg_receiver_image_view);


        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_msgs_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);


    }




    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int i) {
         String messageSenderId = mAuth.getCurrentUser().getUid();
         Messages messages = userMessageList.get(i);


         String fromUserId = messages.getFrom();
         String frommessageType = messages.getType();


         usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {

                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.message_sender_picture.setVisibility(View.GONE);
        holder.message_receiver_picture.setVisibility(View.GONE);


        if (frommessageType.equals("text"))
        {


            if (fromUserId.equals(messageSenderId))
            {

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_msgs_layout);
                holder.senderMessageText.setTextColor(BLACK);
                holder.senderMessageText.setText(messages.getMessage()  + "\n\n" + messages.getTime());



            }
            else {


                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_msgs_layout);
                holder.receiverMessageText.setTextColor(BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n" + messages.getTime());


            }
        }


    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
