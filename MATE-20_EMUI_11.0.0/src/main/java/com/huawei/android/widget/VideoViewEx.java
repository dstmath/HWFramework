package com.huawei.android.widget;

import android.media.MediaPlayer;
import android.widget.VideoView;

public class VideoViewEx {
    public static int getVideoWidth(VideoView videoView) {
        if (videoView == null) {
            return 0;
        }
        return videoView.getVideoWidth();
    }

    public static int getVideoHeight(VideoView videoView) {
        if (videoView == null) {
            return 0;
        }
        return videoView.getVideoHeight();
    }

    public static void setVideoScale(VideoView videoView, int width, int height) {
        if (videoView != null) {
            videoView.setVideoScale(width, height);
        }
    }

    public static void setOnBufferingUpdateListener(VideoView videoView, MediaPlayer.OnBufferingUpdateListener listener) {
        if (videoView != null) {
            videoView.setOnBufferingUpdateListener(listener);
        }
    }
}
