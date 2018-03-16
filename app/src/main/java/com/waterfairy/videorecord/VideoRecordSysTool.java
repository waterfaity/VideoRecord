package com.waterfairy.videorecord;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2018/3/16
 * @Description: 录制视频占用内存较大 设置有限制 ,不推荐使用
 */

public class VideoRecordSysTool {
    private static VideoRecordSysTool videoRecordSysTool;
    private int limitTime;// s
    private float videoQuality = -1;//质量
    private int limitSize;//字节

    public static int REQUEST_RECORD_SYSTEM_VIDEO = 1001;
    private String videoPath;

    private VideoRecordSysTool() {

    }

    public static VideoRecordSysTool getInstance() {
        if (videoRecordSysTool == null)
            videoRecordSysTool = new VideoRecordSysTool();
        videoRecordSysTool.reset();
        return videoRecordSysTool;
    }

    private void reset() {
        limitTime = 0;//60s 部分设备无用
        videoQuality = -1;//质量
        limitSize = 0;//10M 部分设备无用
    }

    public int getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public float getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(float videoQuality) {
        this.videoQuality = videoQuality;
    }

    public int getLimitSize() {
        return limitSize;
    }

    public void setLimitSize(int limitSize) {
        this.limitSize = limitSize;
    }

    /**
     * 非7.0实用该方法
     *
     * @param activity
     * @throws Exception
     */
    public void openCamera(Activity activity) throws Exception {
        openCamera(activity, null);
    }

    /**
     * 非7.0实用该方法
     *
     * @param activity
     * @param outFilePath
     * @throws Exception
     */
    public void openCamera(Activity activity, String outFilePath) throws Exception {
        openCamera(activity, outFilePath, null);
    }

    /**
     * @param activity
     * @param outFilePath
     * @param authority   7.0
     * @throws Exception
     */
    public void openCamera(Activity activity, String outFilePath, String authority) throws Exception {
        //检查相机和录音权限和文件存储权限
        checkPermission(activity);
        //检查sd卡挂载情况
        checkSD();
        //获取文件,不存在的话创建
        File videoFile = getFile(outFilePath);
        //得到uri
        Uri fileUri = getUri(activity, videoFile, authority);
        //获取intent
        Intent intent = getIntent(fileUri);
        //启动 视频录制
        activity.startActivityForResult(intent, REQUEST_RECORD_SYSTEM_VIDEO);
    }

    private Intent getIntent(Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (limitTime > 0)
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, limitTime); //限制的录制时长 以秒为单位
        if (videoQuality >= 0)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality); //设置拍摄的质量最小是0，最大是1（建议不要设置中间值，不同手机似乎效果不同。。。）
        if (limitSize > 0)
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limitSize);//限制视频文件大小 以字节为单位
        return intent;
    }

    /**
     * 获取地址  7.0 以后使用了FileProvider 权限限制
     *
     * @param activity
     * @param videoFile
     * @return
     */
    private Uri getUri(Activity activity, File videoFile, String authority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(activity, authority, videoFile);
        } else {
            return Uri.fromFile(videoFile);
        }
    }

    /**
     * 检查是否挂载了sd卡
     */
    private void checkSD() throws Exception {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new Exception("未发现存储卡");
        }
    }

    /**
     * 获取文件
     *
     * @param outFilePath
     * @return
     * @throws IOException
     */
    private File getFile(String outFilePath) throws IOException {
        File file = null;
        if (TextUtils.isEmpty(outFilePath)) {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).getAbsoluteFile() + File.separator + "video",
                    getFileName());
        } else {
            file = new File(outFilePath);
        }
        boolean canSave = true;
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                canSave = parentFile.mkdirs();
            }
            if (canSave) {
                canSave = file.createNewFile();
            }
            if (!canSave) {
                throw new IOException("视频文件创建失败");
            }
        }
        videoPath = file.getAbsolutePath();
        return file;
    }

    /**
     * 获取文件名字
     *
     * @return
     */
    private String getFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return "video_" + simpleDateFormat.format(new Date()) + ".mp4";
    }

    /**
     * 检查权限
     *
     * @param activity
     * @throws Exception
     */
    private void checkPermission(Activity activity) throws Exception {
        if (!PermissionTool.checkCameraPermission(activity)) {
            throw new Exception("请开启相机权限");
        }
        if (!PermissionTool.checkRecordPermission(activity)) {
            throw new Exception("请开启录音权限");
        }
        if (!PermissionTool.checkStoragePermission(activity)) {
            throw new Exception("请开启存储读写权限");
        }
    }

    public String getVideoPath() {
        return videoPath;
    }
}
