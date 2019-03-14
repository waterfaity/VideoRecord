package com.waterfairy.videorecord;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    private int angle = -1;//camera angle
    private int maxLen = 60;//max len  time   default 60s
    private boolean isBackCamera = true;
    private boolean isFrontCameraCanUse = false;
    private boolean isBackCameraCanUse = false;
    private int cameraId = 0;
    private int videoWidth = 1280;
    private int videoHeight = 720;
    private int orientation;
    private float mQuality;//质量


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
        filePath = videoPath;
//        if (!new File(filePath = videoPath).exists()) {
//            if (onVideoRecordListener != null) {
//                onVideoRecordListener.onRecordVideoError(ERROR_FILE_NOT_EXIST, "文件不存在");
//            }
//        } else {
        this.surfaceView = surfaceView;
//        }
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
            //后置摄像头
            if (backId != -1) {
                cameraId = backId;
            } else if (isFrontCameraCanUse) {
                cameraId = frontId;
            }
        } else {
            //前置
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
    }

    /**
     * 自动对焦
     */
    private void doAutoFocus() {
        try {

            mParameters = camera.getParameters();
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //前置摄像头 对焦???
            camera.setParameters(mParameters);
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        try {
                            camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                            if (!Build.MODEL.equals("KORIDY H30")) {
                                mParameters = camera.getParameters();
                                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续自动对焦
                                camera.setParameters(mParameters);
                            } else {
                                mParameters = camera.getParameters();
                                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                camera.setParameters(mParameters);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 3
     */
    private boolean initMediaRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setCamera(camera);//设置camera

        //这两项需要放在setOutputFormat之前
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Set output file format，输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //必须在setEncoder之前
        mediaRecorder.setVideoFrameRate(30);  //帧数  一分钟帧，15帧就够了
        mediaRecorder.setVideoSize(videoWidth, videoHeight);

        // 这两项需要放在setOutputFormat之后
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置所录制视频的编码位率。
        mediaRecorder.setVideoEncodingBitRate((int) (mQuality * videoWidth * videoHeight));//第一个数字越大，清晰度就越高，考虑文件大小的缘故，就调整为1
        if (angle != -1) {
            if (isBackCamera) {
                mediaRecorder.setOrientationHint((angle + orientation) % 360);
            } else {
                mediaRecorder.setOrientationHint((angle + orientation + 180) % 360);
            }
        }
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mediaRecorder.setOutputFile(filePath);
        //准备
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
                    camera.unlock();
                    mediaRecorder.start();
                    if (handler == null) handler = getHandler();
                    currentTime = 0;
                    handler.removeMessages(0);
                    handler.sendEmptyMessageDelayed(0, 0);
                    if (onVideoRecordListener != null)
                        onVideoRecordListener.onRecordVideoStart();
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
        if (handler != null)
            handler.removeMessages(0);
        isRecording = false;
        currentTime = 0;
    }

    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (onVideoRecordListener != null)
                    onVideoRecordListener.onRecordingVideo(currentTime);
                if (currentTime >= maxLen) {
                    //录制结束
                    stop();
                } else {
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

    public void initViewWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void initViewHeight(int videoHeight) {
        this.videoHeight = videoHeight;

    }

    /**
     * 屏幕方向改变
     *
     * @param orientation
     */
    public boolean onOrientationChanged(int orientation) {
        if (camera != null && !isRecording) {
            this.orientation = orientation;
            return true;
        }
        return false;
    }

    public void setQuality(float quality) {
        mQuality = quality;
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
                doAutoFocus();
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
