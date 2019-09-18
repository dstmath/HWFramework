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
    private static final boolean SUPPROT_SUBTITLE_CONFIG = SystemProperties.getBoolean("ro.config.hw_subtitle_support", false);
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
        if (SUPPROT_SUBTITLE_CONFIG && this.mSTCallback != null) {
            this.mSTCallback.setPlayer(getMediaPlayer());
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
    public void onBufferingUpdateOuter(MediaPlayer mp, int percent) {
        if (this.mOnBufferingUpdateListener != null) {
            this.mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    }

    public void start() {
        if (isInPlaybackStateOuter() && SUPPROT_SUBTITLE_CONFIG && this.mSTCallback != null) {
            this.mSTCallback.start();
        }
        super.start();
    }

    public void pause() {
        if (isPlaying() && SUPPROT_SUBTITLE_CONFIG && this.mSTCallback != null) {
            this.mSTCallback.pause();
        }
        super.pause();
    }

    public void seekTo(int msec) {
        if (isInPlaybackStateOuter() && SUPPROT_SUBTITLE_CONFIG && this.mSTCallback != null) {
            this.mSTCallback.seekTo(msec);
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

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener l) {
        this.mOnBufferingUpdateListener = l;
    }
}
