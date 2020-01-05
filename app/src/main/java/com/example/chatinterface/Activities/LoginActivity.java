package com.example.chatinterface.Activities;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.text.LoginFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mauth;
    private ProgressDialog loadingBar;

    private Button loginButton, phoneLoginButton;
    private EditText userPassword, userEmail;
    private TextView needNewAccountLink, forgetPasswordLink;


    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        initializeFields();


        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneloginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneloginIntent);
            }
        });

    }

    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email..", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();

        } else {
            loadingBar.setTitle("Sign in");
            loadingBar.setMessage("please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mauth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        String currentUserId = mauth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        UsersRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    sendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this,"Logged in successfully!",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();

                                }

                            }
                        });


                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();


                    }

                }
            });


        }


    }


    private void initializeFields() {
        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.need_new_account_link);
        forgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);


    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }


}
