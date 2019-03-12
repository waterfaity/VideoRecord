package com.waterfairy.videorecord;

import android.app.Activity;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2019/3/12 14:11
 * @info:
 */
public class ScreenOrientationTool {

    private int mOrientation;
    private Activity activity;
    private OnOrientationChangeListener onOrientationChangeListener;
    private OrientationEventListener mOrEventListener;

    public int getOrientation() {
        return mOrientation;
    }

    public ScreenOrientationTool init(Activity activity, OnOrientationChangeListener onOrientationChangeListener) {
        this.onOrientationChangeListener = onOrientationChangeListener;
        this.activity = activity;
        startOrientationChangeListener();
        return this;
    }


    /**
     * 启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向
     */
    private void startOrientationChangeListener() {
        if (activity != null) {
            mOrEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                        return;  //手机平放时，检测不到有效的角度
                    }
                    //只检测是否有四个角度的改变
                    if (orientation > 350 || orientation < 10) { //0度
                        orientation = 0;
                    } else if (orientation > 80 && orientation < 100) { //90度
                        orientation = 90;
                    } else if (orientation > 170 && orientation < 190) { //180度
                        orientation = 180;
                    } else if (orientation > 260 && orientation < 280) { //270度
                        orientation = 270;
                    } else {
                        return;
                    }
                    if (mOrientation != orientation) {
                        mOrientation = orientation;
                        onOrientationChangeListener.onOrientationChanged(mOrientation);
                    }
                }
            };
            if (mOrEventListener.canDetectOrientation())
                mOrEventListener.enable();
            else mOrEventListener.disable();
        }
    }

    public void onDestroy() {
        if (mOrEventListener != null) {
            mOrEventListener.disable();
        }
    }

    public interface OnOrientationChangeListener {
        void onOrientationChanged(int orientation);
    }
}
