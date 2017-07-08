package huawei.android.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.SubtitleController.Anchor;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.VideoView.STCallback;
import com.huawei.motiondetection.MotionTypeApps;

public class VideoView extends android.widget.VideoView implements MediaPlayerControl, Anchor {
    private static final boolean SUPPROT_SUBTITLE_CONFIG = false;
    private boolean mIsBuffering;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private STCallback mSTCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.VideoView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.VideoView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.VideoView.<clinit>():void");
    }

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
