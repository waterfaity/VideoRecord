package com.waterfairy.videorecord;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2018/3/16
 * @Description:
 */

public class PermissionTool {
    public static int REQUEST_CAMERA_PERMISSION = 100;
    public static int REQUEST_RECORD_PERMISSION = 101;
    public static int REQUEST_STORAGE_PERMISSION = 102;

    public static boolean checkCameraPermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.CAMERA};
        String permission = Manifest.permission.CAMERA;
        return requestPermission(activity, permissions, permission, REQUEST_CAMERA_PERMISSION);
    }

    public static boolean checkRecordPermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        String permission = Manifest.permission.RECORD_AUDIO;
        return requestPermission(activity, permissions, permission, REQUEST_RECORD_PERMISSION);
    }

    public static boolean checkStoragePermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE};
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        return requestPermission(activity, permissions, permission, REQUEST_STORAGE_PERMISSION);
    }


    /**
     * @param activity    activity
     * @param permissions 权限组
     * @param permission  权限
     * @param requestCode requestCode  Activity中 会返回权限申请状态(类似startActivityForResult)
     */

    public static boolean requestPermission(Activity activity,
                                            @NonNull String[] permissions,
                                            @NonNull String permission,
                                            int requestCode) {
        int permissionCode = checkPermission(activity, permission);
        boolean hasPermission = false;
        if (!(hasPermission = (permissionCode == PackageManager.PERMISSION_GRANTED))) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
        return hasPermission;
    }

    /**
     * 检查权限
     *
     * @param context    activity
     * @param permission 某个权限
     * @return {
     */
    public static int checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission);
    }
}
