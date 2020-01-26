package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.chatinterface.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationcodeButton;
    private EditText inputPhoneNumber,inputVerificationCode;

    private CountryCodePicker ccp;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private RelativeLayout relativeLayout;
    private  String phoneNumber = "",checker = "";
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        inputPhoneNumber=findViewById(R.id.phone_number_input);
        inputVerificationCode=findViewById(R.id.phone_verification);

        sendVerificationcodeButton=findViewById(R.id.send_ver_code_button);
        relativeLayout = findViewById(R.id.phoneAuth);

        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(inputPhoneNumber);


        mAuth=FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loadingBar=new ProgressDialog(this,R.style.MyAlertDialogStyle);

        sendVerificationcodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(sendVerificationcodeButton.getText().equals("Submit") || checker.equals("Code Sent")) {
                    String verificationCode = inputVerificationCode.getText().toString();
                    if (verificationCode.equals("")) {
                        Toast.makeText(PhoneLoginActivity.this, "Write verification code first", Toast.LENGTH_SHORT).show();
                    } else {


                        loadingBar.setTitle("Code verification");
                        loadingBar.setMessage("Please wait, we are Verifying your Pass Code...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);


                    }


                }

                    else {


                        phoneNumber = ccp.getFullNumberWithPlus();
                        if (TextUtils.isEmpty(phoneNumber)) {
                            Toast.makeText(PhoneLoginActivity.this, "please enter your phone number first...", Toast.LENGTH_SHORT).show();

                        } else {

                            loadingBar.setTitle("Phone Number verification");
                            loadingBar.setMessage("Please wait, we will be verifying your Phone Number");
                            loadingBar.setCanceledOnTouchOutside(false);
                            loadingBar.show();

                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    phoneNumber,        // Phone number to verify
                                    60,                 // Timeout duration
                                    TimeUnit.SECONDS,   // Unit of timeout
                                    PhoneLoginActivity.this,               // Activity (for callback binding)
                                    callbacks);        // OnVerificationStateChangedCallbacks


                        }
                    }


                }

        });





        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);



            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {

                Toast.makeText(PhoneLoginActivity.this,"invalid,please enter correct phone number with your country code",Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);
                sendVerificationcodeButton.setText("CONTINUE");
                inputVerificationCode.setVisibility(View.GONE);



            }


            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                sendVerificationcodeButton.setText("Submit");
                inputVerificationCode.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"code has been sent, please check and verify...",Toast.LENGTH_SHORT).show();


            }

        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            usersRef.child(currentUserId).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                    }

                                }
                            });


                            sendUserToMainActivity();
                            loadingBar.dismiss();


                        }

                        else {

                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error: "  +  message,Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }






    private void sendUserToMainActivity() {
        Intent mainIntent =new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();



    }
}
