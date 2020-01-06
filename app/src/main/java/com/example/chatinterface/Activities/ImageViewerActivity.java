package com.example.chatinterface.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.PackageInfoCompat;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.chatinterface.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {
    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        imageView = findViewById(R.id.ImageViewer);

        imageUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(imageView);


    }
}
