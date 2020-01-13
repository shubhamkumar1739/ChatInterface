package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, current_State, senderUserId;
    private ImageView userProfileImage;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;


    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineRequestButton;
    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);


        getSupportActionBar().setTitle(" ");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appBarLayout = findViewById(R.id.app_bar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mAuth = FirebaseAuth.getInstance();


        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();


        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineRequestButton = findViewById(R.id.decline_message_request_button);
        current_State = "new";

        retrieveUserInfo();


    }


    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    final String userImage = dataSnapshot.child("image").getValue().toString();
                    final String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();


                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileName.setShadowLayer(1.6f, 1.5f, 1.3f, Color.BLACK);

                    userProfileStatus.setText(userStatus);

                    appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                        boolean isShow = true;
                        int scrollRange = -1;

                        @Override
                        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                            if (scrollRange == -1) {
                                scrollRange = appBarLayout.getTotalScrollRange();
                            }
                            if (scrollRange + verticalOffset == 0) {

                                collapsingToolbarLayout.setTitle(userName);
                                isShow = true;
                            } else if (isShow) {
                                collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work

                                isShow = false;
                            }

                        }
                    });


                    manageChatRequests();

                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    Picasso.get().load(R.drawable.profile_image).into(userProfileImage);
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

                if (dataSnapshot.hasChild(receiverUserId)) {

                    String req_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (req_type.equals("sent")) {
                        current_State = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    } else if (req_type.equals("received")) {
                        current_State = "request_received";
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
                } else {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserId)) {
                                current_State = "friends";
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

        if (!senderUserId.equals(receiverUserId)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessageRequestButton.setEnabled(false);

                    if (current_State.equals("new")) {
                        sendChatRequest();
                    }

                    if (current_State.equals("request_sent")) {
                        cancelChatRequest();


                    }

                    if (current_State.equals("request_received")) {
                        acceptChatRequest();


                    }

                    if (current_State.equals("friends")) {
                        removeSpecificContact();

                    }


                }
            });

        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }


    }

    private void removeSpecificContact() {

        contactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    contactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                sendMessageRequestButton.setEnabled(true);
                                current_State = "new";
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

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    sendMessageRequestButton.setEnabled(true);
                                                    current_State = "friends";
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
        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                sendMessageRequestButton.setEnabled(true);
                                current_State = "new";
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

        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from", senderUserId);
                                chatNotificationMap.put("type", "request");
                                notificationRef.child(receiverUserId).push().setValue(chatNotificationMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    sendMessageRequestButton.setEnabled(true);
                                                    current_State = "request_sent";
                                                    sendMessageRequestButton.setText("Cancel Chat Request");

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
