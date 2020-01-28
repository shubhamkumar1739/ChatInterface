package com.example.chatinterface.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.RouteInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatinterface.Activities.ImageViewerActivity;
import com.example.chatinterface.Activities.MainActivity;
import com.example.chatinterface.R;
import com.example.chatinterface.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import static android.graphics.Color.green;
import static android.graphics.Color.parseColor;


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
        public ImageView message_sender_picture, message_receiver_picture;


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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);


        String fromUserId = messages.getFrom();
        String frommessageType = messages.getType();


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {

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


        if (frommessageType.equals("text")) {


            if (fromUserId.equals(messageSenderId)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);


                holder.senderMessageText.setBackgroundResource(R.drawable.sender_msgs_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime());


            } else {


                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_msgs_layout);
                holder.receiverMessageText.setTextColor(BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime());


            }
        } else if (frommessageType.equals("image")) {

            if (fromUserId.equals(messageSenderId)) {

                holder.message_sender_picture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.message_sender_picture);


            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.message_receiver_picture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.message_receiver_picture);

            }

        } else if (frommessageType.equals("pdf") || frommessageType.equals("docx")) {

            if (fromUserId.equals(messageSenderId)) {
                holder.message_sender_picture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatinterface-3e19b.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=39779237-bc7e-4919-8628-344a50441581").into(holder.message_sender_picture);

            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.message_receiver_picture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatinterface-3e19b.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=39779237-bc7e-4919-8628-344a50441581").into(holder.message_receiver_picture);

            }

        }


        //code  for deleting a msg,media or text,doc,pdf
        //sender's part


        if (fromUserId.equals(messageSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View this Doc",
                                "Cancel",
                                "Delete for Everyone"


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    DeleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 1) {


                                    //getting issue here!!
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 3) {
                                    DeletedMessagesForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }


                            }
                        });
                        builder.show();


                    } else if (userMessageList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for Everyone"


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    DeleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 2) {
                                    DeletedMessagesForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }


                            }
                        });
                        builder.show();


                    } else if (userMessageList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this Image",
                                "Cancel",
                                "Delete for Everyone"


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    DeleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 3) {
                                    DeletedMessagesForEveryone(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }


                            }
                        });
                        builder.show();


                    }


                }
            });
        }

        //receiver's part
        else {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View this Doc",
                                "Cancel",


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {

                                    DeleteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 1) {


                                    //getting issue here!!
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }


                            }
                        });
                        builder.show();


                    } else if (userMessageList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    DeleteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                }


                            }
                        });
                        builder.show();


                    } else if (userMessageList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this Image",
                                "Cancel",


                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    DeleteReceivedMessages(position, holder);


                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);


                                } else if (i == 1) {

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }


                            }
                        });
                        builder.show();


                    }


                }
            });


        }


    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


    private void DeleteSentMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();


                }


            }
        });


    }


    private void DeleteReceivedMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();


                }


            }
        });


    }

    private void DeletedMessagesForEveryone(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    rootRef.child("Message")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();


                }


            }
        });


    }


}
