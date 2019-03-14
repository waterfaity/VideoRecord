package com.waterfairy.videorecord;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoRecordActivity extends AppCompatActivity implements OnVideoRecordListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "VideoRecordActivity";
    private TextView mTVTime;
    private SurfaceView mSurfaceView;
    private CheckBox mBTRecord;
    private VideoRecordTool mVideoRecordTool;
    private RelativeLayout mRLTime;


    //activity 状态
    private int mActivityState;
    private static final int STATE_RESUME = 1;
    private static final int STATE_PAUSE = 2;

    //intent_str
    public static final String STR_QUALITY = "record_video_quality";
    public static final String STR_VIDEO_PATH = "record_video_path";//绝对路径
    public static final String STR_VIDEO_SAVE_PATH = "record_video_cache_path";//文件夹
    public static final String STR_VIDEO_DURATION = "record_video_duration";
    public static final String STR_FOR_RESULT = "result_str";
    public static final String RESULT_STR_VIDEO_PATH = "data";

    private boolean canFinish;
    private View mIVChangeCamera;//前置后置摄像头切换
    //视频参数
    private int mDuration = 60;
    private String mStrResult;//返回的字段
    private float mQuality = 1.5F;//质量
    private String mVideoPath;//视频路径
    private String mVideoCachePath;//视频文件夹
    private ScreenOrientationTool screenOrientationTool;
    private int currentOrientation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        getExtra();
        findView();
        initData();
        initView();
    }


    private void getExtra() {
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(STR_VIDEO_PATH);
        mDuration = intent.getIntExtra(STR_VIDEO_DURATION, 60);
        if (mDuration <= 0) mDuration = 60;
        mVideoCachePath = intent.getStringExtra(STR_VIDEO_SAVE_PATH);
        mStrResult = intent.getStringExtra(STR_FOR_RESULT);
        mQuality = intent.getIntExtra(STR_QUALITY, 0);
        if (mQuality == 0)
            mQuality = intent.getFloatExtra(STR_QUALITY, 1.5F);

    }

    private void findView() {
        mBTRecord = findViewById(R.id.tb_record);
        mTVTime = findViewById(R.id.time);
        mRLTime = findViewById(R.id.rel_time);
        mSurfaceView = findViewById(R.id.surface_view);
        mIVChangeCamera = findViewById(R.id.chang_camera);
    }

    private void initView() {
        mBTRecord.setOnCheckedChangeListener(this);
        mIVChangeCamera.setOnClickListener(this);
        mRLTime.setRotation(currentOrientation);
        mIVChangeCamera.setRotation(currentOrientation);
    }


    private void initData() {
        mVideoRecordTool = VideoRecordTool.getInstance();
        mVideoRecordTool.setOnVideoRecordListener(this);
        mVideoRecordTool.setMaxLenTime(mDuration);
        mVideoRecordTool.setQuality(mQuality);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoRecordTool.setAngle(90);
        }
        initFilePath();
        mVideoRecordTool.initViewAndPath(mSurfaceView, mVideoPath);
        mVideoRecordTool.init();
        screenOrientationTool = new ScreenOrientationTool().init(this, new ScreenOrientationTool.OnOrientationChangeListener() {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mVideoRecordTool.onOrientationChanged(orientation)) {
                    currentOrientation = 360 - orientation;
                    mRLTime.setRotation(currentOrientation);
                    mIVChangeCamera.setRotation(currentOrientation);
                }
            }
        });
    }


    /**
     * 初始化文件路径
     *
     * @return
     */
    private void initFilePath() {
        if (TextUtils.isEmpty(mVideoPath)) {
            if (TextUtils.isEmpty(mVideoCachePath)) {
                mVideoCachePath = getExternalCacheDir().getAbsolutePath();
            }
            File file = new File(mVideoCachePath, MD5Utils.getMD5Code(new Date().getTime() + "") + ".mp4");
            mVideoPath = file.getAbsolutePath();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canFinish) {
            finish();
        } else {
            if (mActivityState == STATE_PAUSE) {
                resetView();
            }
            mActivityState = STATE_RESUME;
        }
        if (mVideoRecordTool != null) {
            if (!mVideoRecordTool.isBackCameraCanUse() || !mVideoRecordTool.isFrontCameraCanUse()) {
                //单摄像头
                mIVChangeCamera.setVisibility(View.GONE);
            }
        }
    }

    private void resetView() {
        resetView(mVideoRecordTool.isBackCamera());
    }

    private void resetView(boolean isBackCamera) {
        setContentView(R.layout.activity_video_record);
        findView();
        initView();
        initFilePath();

        mVideoRecordTool.initViewAndPath(mSurfaceView, mVideoPath);
        mVideoRecordTool.init(isBackCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityState = STATE_PAUSE;
        if (!canFinish)
            mVideoRecordTool.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoRecordTool.onDestroy();
        screenOrientationTool.onDestroy();
        mStrResult = null;
        mVideoRecordTool = null;
        mTVTime = null;
        mBTRecord = null;
        mVideoPath = null;
        mSurfaceView = null;
    }

    @Override
    public void onRecordVideoWarm(int code, String warmMsg) {
        Log.i(TAG, "onRecordVideoWarm: " + warmMsg);
    }

    @Override
    public void onRecordVideoError(int code, String errMsg) {
        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
        mBTRecord.setOnCheckedChangeListener(null);
        mBTRecord.setChecked(false);
        mBTRecord.setOnCheckedChangeListener(this);
        finish();
    }

    @Override
    public void onRecordVideoStart() {
        Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
        mIVChangeCamera.setVisibility(View.GONE);
    }

    @Override
    public void onRecordingVideo(long time) {
        mTVTime.setText(new SimpleDateFormat("mm:ss").format(new Date(time * 1000)));
    }

    @Override
    public void onRecordVideoEnd(String filePath, boolean handle) {
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TextUtils.isEmpty(mStrResult) ? RESULT_STR_VIDEO_PATH : mStrResult, filePath);
        setResult(RESULT_OK, resultIntent);
        if (handle) {
            finish();
        }
        if (mActivityState == STATE_PAUSE) {
            canFinish = true;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mVideoRecordTool.start();
        } else {
            mVideoRecordTool.stop();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chang_camera) {
            boolean backCamera = mVideoRecordTool.isBackCamera();
            if (backCamera && mVideoRecordTool.isFrontCameraCanUse()) {
                resetView(false);
            } else if (!backCamera && mVideoRecordTool.isBackCameraCanUse()) {
                resetView(true);
            }
        }
    }

}
