package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.chatinterface.Adapters.TabsAccessorAdapter;
import com.example.chatinterface.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
   private Toolbar mtoolbar;
   private ViewPager myViewpager;
   private TabLayout myTabLayout;
   private TabsAccessorAdapter myTabAccessorAdapter;

   private FirebaseUser currentUser;
   private FirebaseAuth mauth;
   private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("ChatInterface");

        myViewpager=findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewpager.setAdapter(myTabAccessorAdapter);

        myTabLayout=findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewpager);
        mauth=FirebaseAuth.getInstance();
        currentUser=mauth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();


    }


    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null){

            sendUserToLoginActivity();

        }
        else{
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        String currentUserId = mauth.getCurrentUser().getUid();


        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this,"welcome",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.logout){
            mauth.signOut();
            sendUserToLoginActivity();

        }
        else if(item.getItemId() == R.id.main_find_people){
            sendUserToFindFriendActivity();


        }
        else if(item.getItemId() == R.id.settings){
            sendUserToSettingsActivity();


        }
        return true;
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void sendUserToFindFriendActivity() {
        Intent findFriendIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendIntent);

    }
}