package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
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
