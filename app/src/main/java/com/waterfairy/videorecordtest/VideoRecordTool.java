package com.waterfairy.videorecordtest;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;


/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2018/3/12
 * @Description:
 */

public class VideoRecordTool {
    private static final String TAG = "VideoRecordTool";
    private MediaRecorder mediaRecorder;
    private Camera camera;
    private SurfaceHolder mHolder;
    private static VideoRecordTool videoRecordTool;
    private OnVideoRecordListener onVideoRecordListener;
    private HolderCallBack holderCallBack;
    private CamcorderProfile camcorderProfile;
    private String filePath;
    private boolean isRecording;


    private int ERROR_FILE_NOT_EXIST = 1;
    private int ERROR_MEDIA_RECORD_PREPARE = 2;
    private int WARM_IS_RECORDING = 3;
    private int WARM_MEDIA_RECORDER_IS_NULL = 4;
    private Camera.Parameters mParameters;
    private SurfaceView sufaceView;
    private Handler handler;
    private int currentTime = 0;


    public VideoRecordTool() {

    }

    public static VideoRecordTool getInstance() {
        if (videoRecordTool == null) videoRecordTool = new VideoRecordTool();
        return videoRecordTool;
    }

    public VideoRecordTool initViewAndPath(SurfaceView surfaceView, String videoPath) {
        if (!new File(filePath = videoPath).exists()) {
            if (onVideoRecordListener != null) {
                onVideoRecordListener.onRecordVideoError(ERROR_FILE_NOT_EXIST, "文件不存在");
            }
        } else {
            this.sufaceView = surfaceView;
        }
        return this;
    }

    public VideoRecordTool init() {
        initHolder();
        initCamera();
//        initMediaRecord();
        return this;
    }


    public void setCamcorderProfile(int camcorderProfile) {
        this.camcorderProfile = CamcorderProfile.get(camcorderProfile);
    }

    /**
     * 1
     */
    private void initHolder() {
        mHolder = sufaceView.getHolder();
        mHolder.addCallback(holderCallBack == null ? holderCallBack = new HolderCallBack() : holderCallBack);
    }

    /**
     * 2
     */
    private void initCamera() {
        camera = Camera.open();
        mParameters = camera.getParameters();
        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        camera.setParameters(mParameters);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
//                    Log.d(TAG, "自动对焦成功");
                }
            }
        });
        //下面这个方法能帮我们获取到相机预览帧，我们可以在这里实时地处理每一帧
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
//                Log.i(TAG, "获取预览帧...");
//                new ProcessFrameAsyncTask(new File(filePath).getParent()).execute(data);
//                Log.d(TAG, "预览帧大小：" + String.valueOf(data.length));
            }
        });
    }

    /**
     * 3
     */
    private boolean initMediaRecord() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);//设置camera
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//音频输入源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//视频输入源
        mediaRecorder.setProfile(camcorderProfile == null ?//设置质量 默认720p
                camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P) :
                camcorderProfile);
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mediaRecorder.setOutputFile(filePath);
        try {
            mediaRecorder.prepare();
            return true;
        } catch (IOException e) {
            releaseMediaRecorder();
            e.printStackTrace();
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoError(ERROR_MEDIA_RECORD_PREPARE, e.getMessage());
            return false;
        }
    }


    public VideoRecordTool setOnVideoRecordListener(OnVideoRecordListener onVideoRecordListener) {
        this.onVideoRecordListener = onVideoRecordListener;
        return this;
    }

    /**
     * 开始录制
     */
    public void start() {
        if (!isRecording) {
            if (isRecording = initMediaRecord()) {
                mediaRecorder.start();
                if (handler == null) handler = getHandler();
                currentTime = 0;
                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0, 0);
                if (onVideoRecordListener != null) onVideoRecordListener.onRecordVideoStart();
            } else {
                if (onVideoRecordListener != null) {
                    onVideoRecordListener.onRecordVideoError(ERROR_MEDIA_RECORD_PREPARE, "mediaRecorder prepare error");
                }
            }
        } else {
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoWarm(WARM_IS_RECORDING, "录制中");
        }
    }

    /**
     * 结束录制
     */
    public void onPause() {
        stop(false);
    }


    /**
     * 停止
     */
    public void stop() {
        stop(true);
    }

    private void stop(boolean pause) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoEnd(filePath, pause);
            handler.removeMessages(0);
        } else {
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoWarm(WARM_MEDIA_RECORDER_IS_NULL, "停止失败,未初始化mediaRecorder");
        }
        isRecording = false;

    }

    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (onVideoRecordListener != null)
                    onVideoRecordListener.onRecordingVideo(currentTime);
                currentTime++;
                handler.sendEmptyMessageDelayed(0, 1000);
            }
        };
    }


    public int getCurrentTime() {
        return currentTime;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    /**
     * SurfaceView  作为camera的预览
     * surfaceView  作为录制的资源
     */
    private class HolderCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            holder.removeCallback(holderCallBack);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
