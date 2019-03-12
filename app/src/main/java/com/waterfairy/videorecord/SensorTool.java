package com.waterfairy.videorecord;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2019/3/12 10:11
 * @info:
 */
public class SensorTool {

    private static final String TAG = "sensorTool";
     //加速度传感器数据
    float accValues[]=new float[3];
    //地磁传感器数据
    float magValues[]=new float[3];
    //旋转矩阵，用来保存磁场和加速度的数据
    float rotateValues[]=new float[9];
    //模拟方向传感器的数据（原始数据为弧度）
    float values[]=new float[3];
    int counter=0;


    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    public void init(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
//            Sensor defaultSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//              sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//              sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            sensorManager.registerListener(getListener(), sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(getListener(), sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private SensorEventListener getListener() {
        if (sensorEventListener == null)
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                        accValues=event.values;
                    }
                    else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                        magValues=event.values;
                    }
                    /**public static boolean getRotationMatrix (float[] R, float[] I, float[] gravity, float[] geomagnetic)
                     * 填充旋转数组r
                     * rotateValues：要填充的旋转数组
                     * I:将磁场数据转换进实际的重力坐标中 一般默认情况下可以设置为null
                     * gravity:加速度传感器数据
                     * geomagnetic：地磁传感器数据
                     */
                    SensorManager.getRotationMatrix(rotateValues, null, accValues, magValues);
                    /**
                     * public static float[] getOrientation (float[] R, float[] values)
                     * R：旋转数组
                     * values ：模拟方向传感器的数据
                     */

                    SensorManager.getOrientation(rotateValues, values);

                    if (counter++ % 10==1){
                        Log.e("DEBUG", "x:"+Math.toDegrees(values[0])+" y:"+Math.toDegrees(values[1])+" z:"+Math.toDegrees(values[2]));
                    }

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    //加速度

                }
            };
        return sensorEventListener;
    }


    public interface OnSensorListener {
        void onSensorChange();
    }

    public void onDestroy() {
        if (sensorEventListener != null)
            if (sensorManager != null) sensorManager.unregisterListener(sensorEventListener);

    }

}
