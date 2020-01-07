package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private static final int GalleryPic = 1;

    private String currentUserId;
    private Toolbar settingToolbar;


    private FirebaseAuth mauth;
    private DatabaseReference rootRef;
    private StorageReference UserProfileImagesRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mauth = FirebaseAuth.getInstance();
        currentUserId = mauth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        initializeFields();

        userName.setVisibility(View.INVISIBLE);


        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic);


            }
        });

    }


    private void initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this,R.style.MyAlertDialogStyle);

        settingToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPic && resultCode == RESULT_OK && data != null) {

            Uri imgUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);


            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("set Profile Image");
                loadingBar.setMessage("please wait, your profile image is being uploaded");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                //resultUri contains the cropped image

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImagesRef.child(currentUserId + ".jpg");


                //storing resultUri into Firebase db storage


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SettingsActivity.this, "Profile picture updated successfully...", Toast.LENGTH_SHORT).show();

                            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override

                                public void onComplete(@NonNull Task<Uri> task) {


                                    final String downloadUrl = task.getResult().toString();


                                    rootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {


                                                Toast.makeText(SettingsActivity.this, "images in database added successfullly", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {

                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();


                                            }

                                        }
                                    });


                                }
                            });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();


                        }


                    }
                });
            }
        }
    }

    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "please write your username first...", Toast.LENGTH_SHORT).show();

        }

        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "please write your status first...", Toast.LENGTH_SHORT).show();

        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "profile updated successfully...", Toast.LENGTH_SHORT).show();
                    } else {

                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();


                    }
                }
            });
        }
    }

    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();


                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);
                    Log.d("img",retrieveProfileImage);

                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();


                    userName.setText(retrieveUsername);
                    userStatus.setText(retrieveStatus);

                } else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
