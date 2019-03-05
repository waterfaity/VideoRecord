package com.waterfairy.videorecord;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
    //单例
    private static VideoRecordTool videoRecordTool;
    //静态定值量
    private int ERROR_FILE_NOT_EXIST = 1;//文件不存在
    private int ERROR_MEDIA_RECORD_PREPARE = 2;//mediaRecorder 准备失败
    private int ERROR_MEDIA_RECORD_START = 3;//mediaRecorder 开始录制失败
    private int WARM_IS_RECORDING = 3;//录制中
    private int WARM_MEDIA_RECORDER_IS_NULL = 4;//空
    private int WARM_MEDIA_NO_RECORDING = 5;//未开始录制
    private int ERROR_NO_CAMERA = 5;//空
    private int ERROR_OPEN_CAMERA = 6;//空
    //相机
    private Camera camera;
    private Camera.Parameters mParameters;
    private int camcorderProfile = -1;
    //surfaceView
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private HolderCallBack holderCallBack;
    //录像
    private MediaRecorder mediaRecorder;
    //监听
    private OnVideoRecordListener onVideoRecordListener;
    //计时
    private Handler handler;
    //data
    private String filePath;
    private boolean isRecording;
    private int currentTime = 0;
    private int angle;//camera angle
    private int maxLen = 60;//max len  time   default 60s
    private boolean isBackCamera = true;
    private boolean isFrontCameraCanUse = false;
    private boolean isBackCameraCanUse = false;
    private int cameraId = 0;
    private int videoWidth;
    private int videoHeight;


    public VideoRecordTool() {
        currentTime = 0;
        angle = 0;
        maxLen = 60;
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
            this.surfaceView = surfaceView;
        }
        return this;
    }

    public VideoRecordTool setMaxLenTime(int maxLen) {
        this.maxLen = maxLen;
        return this;
    }

    public VideoRecordTool init() {
        return init(isBackCamera);
    }

    /**
     * 改变摄像头
     *
     * @param isBackCamera
     * @return
     */
    public VideoRecordTool init(boolean isBackCamera) {
        if (camera != null) {
            releaseMediaRecorder();
        }
        this.isBackCamera = isBackCamera;
        initHolder();
        initCamera();
        return this;
    }


    public void initCamcorderProfile(int camcorderProfile) {
        this.camcorderProfile = camcorderProfile;
    }

    /**
     * 1
     */
    private void initHolder() {
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(holderCallBack == null ? holderCallBack = new HolderCallBack() : holderCallBack);
    }

    /**
     * 2
     */
    private void initCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras == 0) {
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoError(ERROR_NO_CAMERA, "未发现摄像头");
            return;
        }
        int backId = -1, frontId = -1;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                isBackCameraCanUse = true;
                backId = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                isFrontCameraCanUse = true;
                frontId = i;
            }
        }
        if (isBackCamera) {
            if (backId != -1) {
                cameraId = backId;
            } else if (isFrontCameraCanUse) {
                cameraId = frontId;
            }
        } else {
            if (frontId != -1) {
                cameraId = frontId;
            } else if (isBackCameraCanUse) {
                cameraId = backId;
            }
        }
        camera = Camera.open(cameraId);

        if (camera == null) {
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoError(ERROR_OPEN_CAMERA, "摄像头打开失败");
            return;
        }
        //角度
        try {
            if (angle != 0)
                camera.setDisplayOrientation(angle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //自动对焦
        try {
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
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

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
        mediaRecorder.reset();
        camera.unlock();
        mediaRecorder.setCamera(camera);//设置camera
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//音频输入源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//视频输入源

        if (camcorderProfile != -1 && CamcorderProfile.hasProfile(cameraId, camcorderProfile)) {
            //有设置质量
            mediaRecorder.setProfile(CamcorderProfile.get(camcorderProfile));
        } else {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        }
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mediaRecorder.setOutputFile(filePath);
        if (angle != 0) {
            if (isBackCamera)
                mediaRecorder.setOrientationHint(angle);
            else mediaRecorder.setOrientationHint(angle + 180);

        }
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
            if (initMediaRecord()) {
                try {
                    mediaRecorder.start();
                    if (handler == null) handler = getHandler();
                    currentTime = 0;
                    handler.removeMessages(0);
                    handler.sendEmptyMessageDelayed(0, 0);
                    if (onVideoRecordListener != null) onVideoRecordListener.onRecordVideoStart();
                    isRecording = true;
                } catch (Exception e) {
                    isRecording = false;
                    e.printStackTrace();
                    if (onVideoRecordListener != null) {
                        onVideoRecordListener.onRecordVideoError(ERROR_MEDIA_RECORD_START, "视频录制开始失败");
                    }
                }
            } else {
                if (onVideoRecordListener != null) {
                    onVideoRecordListener.onRecordVideoError(ERROR_MEDIA_RECORD_PREPARE, "视频录制准备失败");
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
        Log.i(TAG, "stop: ");
        if (mediaRecorder != null) {
            if (isRecording) {
                mediaRecorder.stop();
                releaseMediaRecorder();
                if (onVideoRecordListener != null)
                    onVideoRecordListener.onRecordVideoEnd(filePath, pause);
                handler.removeMessages(0);
            } else {
                if (onVideoRecordListener != null)
                    onVideoRecordListener.onRecordVideoWarm(WARM_MEDIA_NO_RECORDING, "停止失败,未开始录制");
            }

        } else {
            if (onVideoRecordListener != null)
                onVideoRecordListener.onRecordVideoWarm(WARM_MEDIA_RECORDER_IS_NULL, "停止失败,未初始化视频录制器");
        }
        isRecording = false;
        currentTime = 0;
    }

    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (currentTime >= 60) {
                    //录制结束
                    stop();

                } else {
                    if (onVideoRecordListener != null)
                        onVideoRecordListener.onRecordingVideo(currentTime);
                    currentTime++;
                    handler.sendEmptyMessageDelayed(0, 1000);
                }
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
        }
        if (camera != null) {
            camera.lock();
        }
    }

    public void onDestroy() {
        releaseMediaRecorder();
    }

    /**
     * 大于0
     *
     * @param angle
     */
    public void setAngle(int angle) {
        this.angle = angle;
    }

    public void initVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void initVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;

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
            Log.i(TAG, "surfaceDestroyed: ");
            holder.removeCallback(holderCallBack);
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }


        }

    }

    public boolean isBackCamera() {
        return isBackCamera;
    }

    public boolean isFrontCameraCanUse() {
        return isFrontCameraCanUse;
    }

    public boolean isBackCameraCanUse() {
        return isBackCameraCanUse;
    }
}
