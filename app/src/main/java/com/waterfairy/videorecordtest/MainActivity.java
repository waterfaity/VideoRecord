package com.waterfairy.videorecordtest;

import android.media.CamcorderProfile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnVideoRecordListener {
    VideoRecordTool videoRecordTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_CAMERA);
//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_RECORD);
//        PermissionUtils.requestPermission(this, PermissionUtils.REQUEST_STORAGE);

        Log.i("VideoRecordTool", "onCreate: 1111");

    }

    @Override
    protected void onResume() {
        super.onResume();
        initOk();
    }

    private void initOk() {
        File file = new File("/sdcard/jjj.mp4");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (file.exists()) {
            SurfaceView surfaceView = findViewById(R.id.surface_view);
            videoRecordTool = VideoRecordTool.getInstance();
            videoRecordTool.setOnVideoRecordListener(this);
            videoRecordTool.initViewAndPath(surfaceView, file.getAbsolutePath());
            videoRecordTool.setCamcorderProfile(CamcorderProfile.QUALITY_480P);
            videoRecordTool.init();
        }
    }

    @Override
    public void onRecordVideoWarm(int code, String warmMsg) {

    }

    @Override
    public void onRecordVideoError(int code, String errMsg) {

    }

    @Override
    public void onRecordVideoStart() {

    }

    @Override
    public void onRecordingVideo(long time) {

    }

    @Override
    public void onRecordVideoEnd(String filePath, boolean pause) {

    }


    public void onClick(View view) {

    }

    public void start(View view) {
        videoRecordTool.start();
    }

    public void end(View view) {
        videoRecordTool.stop();
    }
}
