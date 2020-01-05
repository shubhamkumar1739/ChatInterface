package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatinterface.Adapters.MessageAdapter;
import com.example.chatinterface.R;
import com.example.chatinterface.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String msgReceiverId, msgReceiverName, messageSenderId;
    //private String msgReceiverImage;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton;
    private EditText messageInputText;
    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;

     private RecyclerView UserMessagesList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        msgReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        msgReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        //msgReceiverImage = getIntent().get("visit_user_image").toString();

        initialiseControllers();


        userName.setText(msgReceiverName);
        //Picasso.get().load(msgReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        Picasso.get().load(R.drawable.profile_image).into(userImage); //for now, till the time images are added into firebase

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMesage();
            }
        });


    }

    private void initialiseControllers() {


        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        sendMessageButton = findViewById(R.id.send_message_btn);
        messageInputText = findViewById(R.id.input_message);

        adapter = new MessageAdapter(messagesList);

        UserMessagesList = findViewById(R.id.private_messages_list_of_users);

        linearLayoutManager = new LinearLayoutManager(this);

        UserMessagesList.setLayoutManager(linearLayoutManager);

        UserMessagesList.setAdapter(adapter);
        DispalyLastSeen();


    }





    private void DispalyLastSeen(){
        RootRef.child("Users").child(messageSenderId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("UserState").hasChild("state")) {

                    String state = dataSnapshot.child("UserState").child("state").getValue().toString();
                    String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                    Log.d("date",date);

                    String time = dataSnapshot.child("UserState").child("time").getValue().toString();
                    Log.d("time",time);

                    if (state.equals("online")) {
                        userLastSeen.setText("online");


                    } else if (state.equals("offline")) {
                        userLastSeen.setText(" Last seen: " + date + " " + time);


                    }

                } else {

                    userLastSeen.setText("offline");


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onStart() {


        super.onStart();


        RootRef.child("Message").child(messageSenderId).child(msgReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                adapter.notifyDataSetChanged();
                UserMessagesList.smoothScrollToPosition(UserMessagesList.getAdapter().getItemCount());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMesage() {
        String messageTxt = messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageTxt)) {
            Toast.makeText(this, "First type a message", Toast.LENGTH_SHORT).show();


        } else {
            String msgSenderRef = "Message/" + messageSenderId + "/" + msgReceiverId;
            String msgReceiverRef = "Message/" + msgReceiverId + "/" + messageSenderId;

            DatabaseReference UserMessageKeyRef = RootRef.child("Messages").child(messageSenderId).
                    child(msgReceiverId).push();

            String messagePushId = UserMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageTxt); //for now, we are working only on the text msgs
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(msgSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(msgReceiverRef + "/" + messagePushId, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();


                    }
                    messageInputText.setText("");


                }
            });


        }


    }

}
