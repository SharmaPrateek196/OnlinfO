package com.example.onlinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_image);

        imageView=(ImageView)findViewById(R.id.iv_download);
        button=(Button)findViewById(R.id.btn_download);

        final String url=getIntent().getStringExtra("url");
        Glide.with(this).load(url).into(imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(permission()) {
                    long currentTimeMillis = System.currentTimeMillis();
                    File file = new File(new File(Environment.getExternalStorageDirectory(),"Onlinfo"), "image" + currentTimeMillis);
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request;
                    request = new DownloadManager.Request(Uri.parse(url)).setTitle("File Downloading").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE).setDestinationUri(Uri.fromFile(file));
                    downloadManager.enqueue(request);
                    Toast.makeText(DownloadActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean permission() {
        if(ActivityCompat.checkSelfPermission(DownloadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PermissionChecker.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DownloadActivity.this,new String[]{Manifest.permission.   WRITE_EXTERNAL_STORAGE},1);
            return false;
        }
        return true;
    }
}
