package com.waterfairy.videorecord;


import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2019/3/11 16:33
 * @info:
 */

public class VideoSizeTool {
    private static final String TAG = "videoSizeTool";

    /**
     * 0:preVideoSize 预览大小
     * 1:videoSize 拍照视频大小
     *
     * @return
     */
    public static android.hardware.Camera.Size[] getVideoSize(
            int screenWidth, int screenHeight,
            List<android.hardware.Camera.Size> preSizeList,
            List<android.hardware.Camera.Size> videoSizeList) {
        sort(preSizeList);
        sort(videoSizeList);
        if (preSizeList != null) {
            Camera.Size size = preSizeList.get(0);
            int max = 0, min = 0;
            max = Math.max(screenWidth, screenHeight);
            min = Math.min(screenWidth, screenHeight);
            if (size.width > size.height) {
                screenWidth = max;
                screenHeight = min;
            } else {
                screenWidth = min;
                screenHeight = max;
            }
        }

        //分析

        //视频大小
        Camera.Size videoSize = getVideoSize(screenWidth, screenHeight, videoSizeList);
        //预览大小
        Camera.Size previewSize = getVideoSize(videoSize.width, videoSize.height, preSizeList);


        return new Camera.Size[]{videoSize, previewSize};
    }

    public static Camera.Size getVideoSize(
            int screenWidth, int screenHeight,
            List<Camera.Size> sizeList) {

        //获取相近大小 返回有四个  等宽高  大于 等于 小于
        //1.等宽高
        //2.等于
        //3.小于
        //4.大于

        Object[] objects = getEqualSize(screenWidth, screenHeight, sizeList);

        Camera.Size cameraSize = null;
        Object object = objects[0];
        if (object == null) {
            for (int i = 1; i < objects.length; i++) {
                if (objects[i] != null) {
                    //2.等于
                    //3.小于
                    //4.大于
                    List<Camera.Size> sizes = (List<Camera.Size>) objects[i];
                    cameraSize = getRadioNear(screenWidth, screenHeight, sizes);
                    break;
                }
            }
        } else {
            //等宽高
            cameraSize = (Camera.Size) object;
        }
        return cameraSize;
    }


    private static Camera.Size getRadioNear(int screenWidth, int screenHeight, List<Camera.Size> sizes) {
        final float radio = screenWidth / (float) screenHeight;
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                //顺序
                float temp1 = o1.width / (float) o1.height;
                float temp2 = o2.width / (float) o2.height;

                float abs1 = Math.abs(temp1 - radio);
                float abs2 = Math.abs(temp2 - radio);
                if (abs1 > abs2) return 1;
                else if (abs1 < abs1) return -1;
                else return 0;
            }
        });

        return sizes.get(0);
    }

    private static void sort(List<Camera.Size> sizeList) {
        Collections.sort(sizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                //倒序
                if (o1.width < o2.width) return 1;
                else if (o1.width > o2.width) return -1;
                return 0;
            }
        });
    }


    /**
     * 获取合适的size
     *
     * @param supportedPreviewSizes
     */
    private static Object[] getEqualSize(int videoWidth, int videoHeight, List<Camera.Size> supportedPreviewSizes) {
        Camera.Size equalSize = null;
        List<Camera.Size> bigSizeList = null;
        List<Camera.Size> littleSizeList = null;
        List<Camera.Size> equalSizeList = null;
        if (supportedPreviewSizes != null) {
            //和宽相等的size集合
            bigSizeList = new ArrayList<>();
            littleSizeList = new ArrayList<>();
            equalSizeList = new ArrayList<>();

            for (int i = 0; i < supportedPreviewSizes.size(); i++) {
                Camera.Size size = supportedPreviewSizes.get(i);
                if (size.width == videoWidth && size.height == videoHeight) {
                    equalSize = size;
                } else if (size.width > videoWidth) {
                    bigSizeList.add(size);
                } else if (size.width < videoWidth) {
                    littleSizeList.add(size);
                } else if (size.width == videoWidth) {
                    equalSizeList.add(size);
                }
            }
        }
        return new Object[]{equalSize, equalSize, littleSizeList, bigSizeList};
    }

}
