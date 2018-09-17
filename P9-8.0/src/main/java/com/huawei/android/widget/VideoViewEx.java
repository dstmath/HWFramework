package com.huawei.android.widget;

import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.widget.VideoView;

public class VideoViewEx {
    public static int getVideoWidth(VideoView vv) {
        return vv.getVideoWidth();
    }

    public static int getVideoHeight(VideoView vv) {
        return vv.getVideoHeight();
    }

    public static void setVideoScale(VideoView vv, int width, int height) {
        vv.setVideoScale(width, height);
    }

    public static void setOnBufferingUpdateListener(VideoView obj, OnBufferingUpdateListener l) {
        obj.setOnBufferingUpdateListener(l);
    }
}
