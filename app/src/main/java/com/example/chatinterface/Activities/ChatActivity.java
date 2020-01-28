package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.chatinterface.Adapters.MessageAdapter;
import com.example.chatinterface.R;
import com.example.chatinterface.model.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.RECORD_AUDIO;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "chat_activity";
    private String msgReceiverId, msgReceiverName, messageSenderId;
    private String msgReceiverImage;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;

    private ImageButton sendMessageButton, sendFilesButton;
    private ImageButton sendVoiceNote=null;


    private EditText messageInputText;

    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;

    private RecyclerView UserMessagesList;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    // Last Seen
    private long lastSeenInMilliseconds;
    private String userLastSeenString;


    private FirebaseUser currentUserId;
    private boolean mStartRecording = true;
    public static final int RECORD_AUDIO = 0;
    public static final int SAVE_AUDIO = 1;

    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private Intent intent;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getInstance().getCurrentUser();

        msgReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        msgReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        msgReceiverImage = getIntent().getExtras().get("visit_user_image").toString();
        Log.d("img", msgReceiverImage);

        initialiseControllers();


        userName.setText(msgReceiverName);


        Picasso.get().load(msgReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMesage();
            }
        });

        DispalyLastSeen();


        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                        "Image",
                        "PDF Files",
                        "MS Word Files"

                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }


                        if (i == 1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);

                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);

                        }


                    }
                });


                builder.show();
            }
        });


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
        sendFilesButton = findViewById(R.id.send_files_btn);
        sendVoiceNote = findViewById(R.id.send_voice_note);


        UserMessagesList = findViewById(R.id.private_messages_list_of_users);

        adapter = new MessageAdapter(messagesList);


        linearLayoutManager = new LinearLayoutManager(this);
        UserMessagesList.setLayoutManager(linearLayoutManager);

        UserMessagesList.setAdapter(adapter);

        loadingBar = new ProgressDialog(this, R.style.MyAlertDialogStyle);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        sendVoiceNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
                } else {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                }

            }
        });


    }

    private void onRecord(boolean mStartRecording) {
        intent = new Intent(ChatActivity.this, RecordingService.class);

        if (mStartRecording) {
            sendVoiceNote.setImageResource(R.drawable.stop);
            Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                folder.mkdir();
            }


            intent.putExtra("user_id", currentUserId.getUid());
            startService(intent);

            //allow the screen to turn off again once recording is finished
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        } else {
            //stop recording
            sendVoiceNote.setImageResource(R.drawable.microphn);
            Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();

            if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_AUDIO);

            } else {
                //start RecordingService
                ChatActivity.this.stopService(intent);
                //keep screen on while recording
                ChatActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;

            } else {
                Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == SAVE_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ChatActivity.this.stopService(intent);
                //allow the screen to turn off again once recording is finished
                ChatActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            } else {
                //User denied Permission.
                Toast.makeText(ChatActivity.this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {


            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("please wait, while we are sending...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            fileUri = data.getData();
            if (!checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String msgSenderRef = "Message/" + messageSenderId + "/" + msgReceiverId;
                final String msgReceiverRef = "Message/" + msgReceiverId + "/" + messageSenderId;

                //storing inside the storage


                DatabaseReference UserMessageKeyRef = RootRef.child("Messages").child(messageSenderId).
                        child(msgReceiverId).push();

                //creating random key to save the file link into db

                final String messagePushId = UserMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);


                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if ((task.isSuccessful())) {

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", msgReceiverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(msgSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(msgReceiverRef + "/" + messagePushId, messageTextBody);


                            RootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();


                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + "% Uploading...");


                    }
                });


            } else if (checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String msgSenderRef = "Message/" + messageSenderId + "/" + msgReceiverId;
                final String msgReceiverRef = "Message/" + msgReceiverId + "/" + messageSenderId;

                DatabaseReference UserMessageKeyRef = RootRef.child("Messages").child(messageSenderId).
                        child(msgReceiverId).push();

                final String messagePushId = UserMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());//for now, we are working only on the text msgs
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", msgReceiverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(msgSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(msgReceiverRef + "/" + messagePushId, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();


                                    }
                                    messageInputText.setText("");


                                }
                            });


                        }


                    }
                });


            } else {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private void DispalyLastSeen() {
        RootRef.child("Users").child(msgReceiverId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("UserState").hasChild("state")) {

                    String state = dataSnapshot.child("UserState").child("state").getValue().toString();
                    String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                    Log.d("date", date);

                    String time = dataSnapshot.child("UserState").child("time").getValue().toString();
                    Log.d("time", time);


                    try {
                        long timeInMilliseconds = lastSeenInMilliseconds(time);
                        userLastSeenString = lastSeenTime(String.valueOf(timeInMilliseconds));
//                        if (timeInMilliseconds != 0){
//                            Log.d(TAG, "Time In Milliseconds: "+ timeInMilliseconds);
//                            userLastSeenString = getFormattedLastSeen(timeInMilliseconds);
//                        }else {
//                            Log.d(TAG, "Time In Milliseconds 0!");
//                        }

                    } catch (Exception e) {
                        Log.d(TAG, "User Last Seen Exception: " + e.toString());
                    }


                    if (state.equals("online")) {
                        userLastSeen.setText("online");


                    } else if (state.equals("offline")) {
                        Log.d(TAG, "User Offline!");
                        if (!TextUtils.isEmpty(userLastSeenString)) {
                            userLastSeen.setText(" Last seen: " + date + " " + userLastSeenString);
                            Log.d(TAG, "User Formatted Last Seen Available!");
                        } else {
                            userLastSeen.setText(" Last seen: " + date + " " + time);
                            Log.d(TAG, "User Formatted Last Seen Not Available!");
                        }


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
            messageTextBody.put("to", msgReceiverId);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(msgSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(msgReceiverRef + "/" + messagePushId, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        //  Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();


                    }
                    messageInputText.setText("");


                }
            });


        }


    }

    private long lastSeenInMilliseconds(String time) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = simpleDateFormat.parse(time);
            Calendar calendar = Calendar.getInstance();
            assert date != null;
            calendar.setTime(date);
            int hourToSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
            int minutesToSeconds = calendar.get(Calendar.MINUTE) * 60;

            int totalSeconds = hourToSeconds + minutesToSeconds;
            lastSeenInMilliseconds = new GregorianCalendar().getTimeInMillis() + totalSeconds * 1000;
            Log.d(TAG, "lastSeenInMilliseconds: " + String.valueOf(lastSeenInMilliseconds));
        } catch (ParseException e) {
            Log.d(TAG, "Time Conversion Exception: " + e.toString());
        }
        return lastSeenInMilliseconds;

    }

    private String getFormattedLastSeen(long smsTimeInMilliseconds) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilliseconds);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "Today " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
    }

    private String lastSeenTime(String timeInMilliseconds) {
        Calendar now = Calendar.getInstance();
        return String.valueOf(DateUtils.getRelativeTimeSpanString(Long.parseLong(timeInMilliseconds), now.getTimeInMillis(), DateUtils.DAY_IN_MILLIS));
    }

}
