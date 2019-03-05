package com.waterfairy.videorecord;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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


    /**
     * Quality level corresponding to the lowest available resolution.
     */
    public static final int QUALITY_LOW = 0;

    /**
     * Quality level corresponding to the highest available resolution.
     */
    public static final int QUALITY_HIGH = 1;

    /**
     * Quality level corresponding to the qcif (176 x 144) resolution.
     */
    public static final int QUALITY_QCIF = 2;

    /**
     * Quality level corresponding to the cif (352 x 288) resolution.
     */
    public static final int QUALITY_CIF = 3;

    /**
     * Quality level corresponding to the 480p (720 x 480) resolution.
     * Note that the horizontal resolution for 480p can also be other
     * values, such as 640 or 704, instead of 720.
     */
    public static final int QUALITY_480P = 4;

    /**
     * Quality level corresponding to the 720p (1280 x 720) resolution.
     */
    public static final int QUALITY_720P = 5;

    /**
     * Quality level corresponding to the 1080p (1920 x 1080) resolution.
     * Note that the vertical resolution for 1080p can also be 1088,
     * instead of 1080 (used by some vendors to avoid cropping during
     * video playback).
     */
    public static final int QUALITY_1080P = 6;

    /**
     * Quality level corresponding to the QVGA (320x240) resolution.
     */
    public static final int QUALITY_QVGA = 7;

    /**
     * Quality level corresponding to the 2160p (3840x2160) resolution.
     */
    public static final int QUALITY_2160P = 8;


    //activity 状态
    private int mActivityState;
    private static final int STATE_RESUME = 1;
    private static final int STATE_PAUSE = 2;

    //intent_str
    public static final String STR_QUALITY = "record_video_quality";
    public static final String STR_VIDEO_WIDTH = "record_video_width";
    public static final String STR_VIDEO_HEIGHT = "record_video_height";
    public static final String STR_VIDEO_PATH = "record_video_path";//绝对路径
    public static final String STR_VIDEO_SAVE_PATH = "record_video_cache_path";//文件夹
    public static final String STR_VIDEO_DURATION = "record_video_duration";
    public static final String STR_FOR_RESULT = "result_str";
    public static final String RESULT_STR_VIDEO_PATH = "data";

    private boolean canFinish;
    private View mIVChangeCamera;//前置后置摄像头切换
    //视频参数
    private int mVideoWidth;//视频宽
    private int mVideoHeight;//视频高
    private int mDuration = 60;
    private String mStrResult;//返回的字段
    private int mQuality = -1;//质量
    private String mVideoPath;//视频路径
    private String mVideoCachePath;//视频文件夹

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
        mVideoWidth = intent.getIntExtra(STR_VIDEO_WIDTH, 0);
        mVideoHeight = intent.getIntExtra(STR_VIDEO_HEIGHT, 0);
        mQuality = intent.getIntExtra(STR_QUALITY, (mVideoHeight == 0 || mVideoWidth == 0) ? QUALITY_720P : -1);
    }

    private void findView() {
        mBTRecord = findViewById(R.id.tb_record);
        mTVTime = findViewById(R.id.time);
        mSurfaceView = findViewById(R.id.surface_view);
        mIVChangeCamera = findViewById(R.id.chang_camera);
    }

    private void initView() {
        mBTRecord.setOnCheckedChangeListener(this);
        mIVChangeCamera.setOnClickListener(this);

    }

    private void initData() {
        mVideoRecordTool = VideoRecordTool.getInstance();
        mVideoRecordTool.setOnVideoRecordListener(this);
        mVideoRecordTool.initCamcorderProfile(mQuality);
        mVideoRecordTool.initVideoWidth(mVideoWidth);
        mVideoRecordTool.initVideoHeight(mVideoHeight);
        mVideoRecordTool.setMaxLenTime(mDuration);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mVideoRecordTool.setAngle(90);
        }
        initFilePath();
        mVideoRecordTool.initViewAndPath(mSurfaceView, mVideoPath);
        mVideoRecordTool.init();

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
