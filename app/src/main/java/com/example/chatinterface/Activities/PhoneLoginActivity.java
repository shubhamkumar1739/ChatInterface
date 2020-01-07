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

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationcodeButton,verifyButton;
    private EditText inputPhoneNumber,inputVerificationCode;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        sendVerificationcodeButton=findViewById(R.id.send_ver_code_button);
        verifyButton=findViewById(R.id.verify_button);
        inputPhoneNumber=findViewById(R.id.phone_number_input);
        inputVerificationCode=findViewById(R.id.phone_verification);
        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this,R.style.MyAlertDialogStyle);

        sendVerificationcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String phoneNumber = inputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this,"please enter your phone number first...",Toast.LENGTH_SHORT).show();

                }
                else{

                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait, we will be authenticating your phone");
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
        });


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendVerificationcodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode= inputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this,"please write verification code first",Toast.LENGTH_SHORT).show();

                }
                else{

                    loadingBar.setTitle("Code verification");
                    loadingBar.setMessage("Please wait, we will be verifying your verification Code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

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

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"invalid,please enter correct phone number with your country code",Toast.LENGTH_SHORT).show();

                sendVerificationcodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);


                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);

            }


            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"code has been sent, please check and verify...",Toast.LENGTH_SHORT).show();

                sendVerificationcodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);


                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);


            }

        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Logged in successfully",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        }

                        else {

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
