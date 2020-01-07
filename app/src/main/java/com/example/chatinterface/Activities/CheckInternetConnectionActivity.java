package com.example.chatinterface.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatinterface.R;

public class CheckInternetConnectionActivity extends AppCompatActivity {
    private ImageView imgFailedConnection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_check_internet_connection);

        imgFailedConnection = findViewById(R.id.whoops);

        if(haveNetwork()){
            //proceed with app functionality
            imgFailedConnection.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(CheckInternetConnectionActivity.this,MainActivity.class);
            startActivity(intent);

        }
        else{
            imgFailedConnection.setVisibility(View.VISIBLE);
            //Toast.makeText(this,"Netwok connection not available",Toast.LENGTH_SHORT).show();

        }


    }


    private boolean haveNetwork(){
        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();

        for(NetworkInfo info : networkInfos ){
            if(info.getTypeName().equalsIgnoreCase("WIFI"))
                if(info.isConnected())
                    have_WIFI=true;

            if(info.getTypeName().equalsIgnoreCase("MOBILE"))
                if(info.isConnected())
                    have_MobileData=true;




        }

        return have_MobileData||have_WIFI;



    }

}
