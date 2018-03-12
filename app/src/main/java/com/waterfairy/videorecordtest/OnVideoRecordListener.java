package com.waterfairy.videorecordtest;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2018/3/12
 * @Description:
 */

public interface OnVideoRecordListener {

    void onRecordVideoWarm(int code, String warmMsg);

    void onRecordVideoError(int code, String errMsg);

    void onRecordVideoStart();

    /**
     * @param time   秒
     */
    void onRecordingVideo(long time);

    void onRecordVideoEnd(String filePath, boolean handle);

}
