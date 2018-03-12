package com.waterfairy.videorecordtest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2018/3/12
 * @Description:
 */

class ProcessFrameAsyncTask extends AsyncTask<byte[], Void, String> {
    private static final String TAG = "ProcessFrameAsyncTask";
    private String savePath;

    public ProcessFrameAsyncTask(String savePath) {
        this.savePath = savePath;
    }

    @Override
    protected String doInBackground(byte[]... params) {
        processFrame(params[0]);
        return null;
    }

    private void processFrame(byte[] frameData) {

        Log.i(TAG, "正在处理预览帧...");
        Log.i(TAG, "预览帧大小" + String.valueOf(frameData.length));
        Log.i(TAG, "预览帧处理完毕...");
//        下面这段注释掉的代码是把预览帧数据输出到sd卡中，以.yuv格式保存

        File dir = new File(savePath + "/FrameTest");
        if (!dir.exists()) {
            dir.mkdir();
        }
        savePath = dir + "/" + "testFrame" + ".yuv";
        File file = new File(savePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(frameData);
            Log.i(TAG, "预览帧处理完毕...");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
