package huawei.android.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.SubtitleController.Anchor;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.VideoView.STCallback;
import com.huawei.motiondetection.MotionTypeApps;

public class VideoView extends android.widget.VideoView implements MediaPlayerControl, Anchor {
    private static final boolean SUPPROT_SUBTITLE_CONFIG = SystemProperties.getBoolean("ro.config.hw_subtitle_support", false);
    private boolean mIsBuffering;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private STCallback mSTCallback;

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

    public void setSTCallback(STCallback call) {
        this.mSTCallback = call;
    }

    protected void onSTCallbackSetPlayer(MediaPlayer mediaPlayer) {
        if (SUPPROT_SUBTITLE_CONFIG && this.mSTCallback != null) {
            this.mSTCallback.setPlayer(getMediaPlayer());
        }
    }

    protected void adjustIsBuffering(int arg1) {
        if (arg1 == MotionTypeApps.TYPE_ROTATION_SCREEN) {
            this.mIsBuffering = true;
        } else if (arg1 == 702) {
            this.mIsBuffering = false;
        }
    }

    protected void onBufferingUpdateOuter(MediaPlayer mp, int percent) {
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
        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        this.mOnBufferingUpdateListener = l;
    }
}
