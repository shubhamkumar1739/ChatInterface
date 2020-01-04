package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Trace;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserId,current_State,senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineRequestButton;
    private DatabaseReference userRef,chatRequestRef,contactsRef;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");


        mAuth = FirebaseAuth.getInstance();


        recieverUserId=getIntent().getExtras().get("visit_user_id").toString();
        senderUserId=mAuth.getCurrentUser().getUid();


        userProfileImage=findViewById(R.id.visit_profile_image);
        userProfileName=findViewById(R.id.visit_profile_name);
        userProfileStatus=findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=findViewById(R.id.send_message_request_button);
        declineRequestButton=findViewById(R.id.decline_message_request_button);
        current_State = "new";

        retrieveUserInfo();



    }

    private void retrieveUserInfo() {
        userRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) &&  (dataSnapshot.hasChild("image"))){
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                }
                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(recieverUserId)){

                    String req_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                    if(req_type.equals("sent")){
                        current_State="request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if(req_type.equals("received")){
                        current_State="request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelChatRequest();
                            }
                        });






                    }
                }

                else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(recieverUserId)){
                                current_State="friends";
                                sendMessageRequestButton.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!senderUserId.equals(recieverUserId)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);

                    if(current_State.equals("new")){
                        sendChatRequest();
                    }

                    if(current_State.equals("request_sent")){
                        cancelChatRequest();


                    }

                    if(current_State.equals("request_received")){
                        acceptChatRequest();


                    }

                    if(current_State.equals("friends")){
                       removeSpecificContact();

                    }




                }
            });

        }
        else{
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }



    }

    private void removeSpecificContact() {

        contactsRef.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    contactsRef.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                sendMessageRequestButton.setEnabled(true);
                                current_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);





                            }


                        }
                    });
                }


            }
        });


    }

    private void acceptChatRequest() {

        contactsRef.child(senderUserId).child(recieverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    contactsRef.child(recieverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                chatRequestRef.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            chatRequestRef.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    sendMessageRequestButton.setEnabled(true);
                                                    current_State="friends";
                                                    sendMessageRequestButton.setText("Remove this contact");

                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                    declineRequestButton.setEnabled(false);



                                                }

                                            });
                                        }





                                    }
                                });
                            }

                        }
                    });
                }

            }
        });



    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    chatRequestRef.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                sendMessageRequestButton.setEnabled(true);
                                current_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);





                            }


                        }
                    });
                }


            }
        });

    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserId).child(recieverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(recieverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                current_State="request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");


                            }

                        }
                    });
                }

            }
        });



    }
}
