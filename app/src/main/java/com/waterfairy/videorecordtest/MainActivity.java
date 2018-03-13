package com.waterfairy.videorecordtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.waterfairy.videorecord.VideoRecordActivity;
import com.waterfairy.videorecord.VideoRecordPortActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_CAMERA);
//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_RECORD);
//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_STORAGE);



    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    public void onClick(View view) {
        Intent intent = new Intent(this, VideoRecordPortActivity.class);
        intent.putExtra(VideoRecordActivity.STR_QUALITY,VideoRecordActivity.QUALITY_480P);
        startActivity(intent);

    }

}
