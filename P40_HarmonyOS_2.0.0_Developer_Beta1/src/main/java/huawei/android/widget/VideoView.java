package huawei.android.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SubtitleController;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoView extends android.widget.VideoView implements MediaController.MediaPlayerControl, SubtitleController.Anchor {
    private static final boolean IS_SUPPROT_SUBTITLE_CONFIG = SystemProperties.getBoolean("ro.config.hw_subtitle_support", false);
    private boolean mIsBuffering;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private VideoView.STCallback mSTCallback;

    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSTCallback(VideoView.STCallback call) {
        this.mSTCallback = call;
    }

    /* access modifiers changed from: protected */
    public void onSTCallbackSetPlayer(MediaPlayer mediaPlayer) {
        VideoView.STCallback sTCallback;
        if (IS_SUPPROT_SUBTITLE_CONFIG && (sTCallback = this.mSTCallback) != null) {
            sTCallback.setPlayer(getMediaPlayer());
        }
    }

    /* access modifiers changed from: protected */
    public void adjustIsBuffering(int arg1) {
        if (arg1 == 701) {
            this.mIsBuffering = true;
        } else if (arg1 == 702) {
            this.mIsBuffering = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onBufferingUpdateOuter(MediaPlayer player, int percent) {
        MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = this.mOnBufferingUpdateListener;
        if (onBufferingUpdateListener != null) {
            onBufferingUpdateListener.onBufferingUpdate(player, percent);
        }
    }

    @Override // android.widget.VideoView, android.widget.MediaController.MediaPlayerControl
    public void start() {
        VideoView.STCallback sTCallback;
        if (isInPlaybackStateOuter() && IS_SUPPROT_SUBTITLE_CONFIG && (sTCallback = this.mSTCallback) != null) {
            sTCallback.start();
        }
        super.start();
    }

    @Override // android.widget.VideoView, android.widget.MediaController.MediaPlayerControl
    public void pause() {
        VideoView.STCallback sTCallback;
        if (isPlaying() && IS_SUPPROT_SUBTITLE_CONFIG && (sTCallback = this.mSTCallback) != null) {
            sTCallback.pause();
        }
        super.pause();
    }

    @Override // android.widget.VideoView, android.widget.MediaController.MediaPlayerControl
    public void seekTo(int msec) {
        VideoView.STCallback sTCallback;
        if (isInPlaybackStateOuter() && IS_SUPPROT_SUBTITLE_CONFIG && (sTCallback = this.mSTCallback) != null) {
            sTCallback.seekTo(msec);
        }
        super.seekTo(msec);
    }

    public boolean getCacheState() {
        return this.mIsBuffering;
    }

    public void setVideoScale(int width, int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }
}
