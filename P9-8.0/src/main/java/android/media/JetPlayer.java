package android.media;

import android.content.res.AssetFileDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AndroidRuntimeException;
import android.util.Log;
import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

public class JetPlayer {
    private static final int JET_EVENT = 1;
    private static final int JET_EVENT_CHAN_MASK = 245760;
    private static final int JET_EVENT_CHAN_SHIFT = 14;
    private static final int JET_EVENT_CTRL_MASK = 16256;
    private static final int JET_EVENT_CTRL_SHIFT = 7;
    private static final int JET_EVENT_SEG_MASK = -16777216;
    private static final int JET_EVENT_SEG_SHIFT = 24;
    private static final int JET_EVENT_TRACK_MASK = 16515072;
    private static final int JET_EVENT_TRACK_SHIFT = 18;
    private static final int JET_EVENT_VAL_MASK = 127;
    private static final int JET_NUMQUEUEDSEGMENT_UPDATE = 3;
    private static final int JET_OUTPUT_CHANNEL_CONFIG = 12;
    private static final int JET_OUTPUT_RATE = 22050;
    private static final int JET_PAUSE_UPDATE = 4;
    private static final int JET_USERID_UPDATE = 2;
    private static int MAXTRACKS = 32;
    private static final String TAG = "JetPlayer-J";
    private static JetPlayer singletonRef;
    private NativeEventHandler mEventHandler = null;
    private final Object mEventListenerLock = new Object();
    private Looper mInitializationLooper = null;
    private OnJetEventListener mJetEventListener = null;
    private long mNativePlayerInJavaObj;

    private class NativeEventHandler extends Handler {
        private JetPlayer mJet;

        public NativeEventHandler(JetPlayer jet, Looper looper) {
            super(looper);
            this.mJet = jet;
        }

        public void handleMessage(Message msg) {
            OnJetEventListener listener;
            synchronized (JetPlayer.this.mEventListenerLock) {
                listener = this.mJet.mJetEventListener;
            }
            switch (msg.what) {
                case 1:
                    if (listener != null) {
                        JetPlayer.this.mJetEventListener.onJetEvent(this.mJet, (short) ((msg.arg1 & -16777216) >> 24), (byte) ((msg.arg1 & JetPlayer.JET_EVENT_TRACK_MASK) >> 18), (byte) (((msg.arg1 & JetPlayer.JET_EVENT_CHAN_MASK) >> 14) + 1), (byte) ((msg.arg1 & JetPlayer.JET_EVENT_CTRL_MASK) >> 7), (byte) (msg.arg1 & 127));
                    }
                    return;
                case 2:
                    if (listener != null) {
                        listener.onJetUserIdUpdate(this.mJet, msg.arg1, msg.arg2);
                    }
                    return;
                case 3:
                    if (listener != null) {
                        listener.onJetNumQueuedSegmentUpdate(this.mJet, msg.arg1);
                    }
                    return;
                case 4:
                    if (listener != null) {
                        listener.onJetPauseUpdate(this.mJet, msg.arg1);
                    }
                    return;
                default:
                    JetPlayer.loge("Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public interface OnJetEventListener {
        void onJetEvent(JetPlayer jetPlayer, short s, byte b, byte b2, byte b3, byte b4);

        void onJetNumQueuedSegmentUpdate(JetPlayer jetPlayer, int i);

        void onJetPauseUpdate(JetPlayer jetPlayer, int i);

        void onJetUserIdUpdate(JetPlayer jetPlayer, int i, int i2);
    }

    private final native boolean native_clearQueue();

    private final native boolean native_closeJetFile();

    private final native void native_finalize();

    private final native boolean native_loadJetFromFile(String str);

    private final native boolean native_loadJetFromFileD(FileDescriptor fileDescriptor, long j, long j2);

    private final native boolean native_pauseJet();

    private final native boolean native_playJet();

    private final native boolean native_queueJetSegment(int i, int i2, int i3, int i4, int i5, byte b);

    private final native boolean native_queueJetSegmentMuteArray(int i, int i2, int i3, int i4, boolean[] zArr, byte b);

    private final native void native_release();

    private final native boolean native_setMuteArray(boolean[] zArr, boolean z);

    private final native boolean native_setMuteFlag(int i, boolean z, boolean z2);

    private final native boolean native_setMuteFlags(int i, boolean z);

    private final native boolean native_setup(Object obj, int i, int i2);

    private final native boolean native_triggerClip(int i);

    public static JetPlayer getJetPlayer() {
        if (singletonRef == null) {
            singletonRef = new JetPlayer();
        }
        return singletonRef;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private JetPlayer() {
        Looper myLooper = Looper.myLooper();
        this.mInitializationLooper = myLooper;
        if (myLooper == null) {
            this.mInitializationLooper = Looper.getMainLooper();
        }
        int buffSizeInBytes = AudioTrack.getMinBufferSize(JET_OUTPUT_RATE, 12, 2);
        if (buffSizeInBytes != -1 && buffSizeInBytes != -2) {
            native_setup(new WeakReference(this), getMaxTracks(), Math.max(1200, buffSizeInBytes / (AudioFormat.getBytesPerSample(2) * 2)));
        }
    }

    protected void finalize() {
        native_finalize();
    }

    public void release() {
        native_release();
        singletonRef = null;
    }

    public static int getMaxTracks() {
        return MAXTRACKS;
    }

    public boolean loadJetFile(String path) {
        return native_loadJetFromFile(path);
    }

    public boolean loadJetFile(AssetFileDescriptor afd) {
        long len = afd.getLength();
        if (len < 0) {
            throw new AndroidRuntimeException("no length for fd");
        }
        return native_loadJetFromFileD(afd.getFileDescriptor(), afd.getStartOffset(), len);
    }

    public boolean closeJetFile() {
        return native_closeJetFile();
    }

    public boolean play() {
        return native_playJet();
    }

    public boolean pause() {
        return native_pauseJet();
    }

    public boolean queueJetSegment(int segmentNum, int libNum, int repeatCount, int transpose, int muteFlags, byte userID) {
        return native_queueJetSegment(segmentNum, libNum, repeatCount, transpose, muteFlags, userID);
    }

    public boolean queueJetSegmentMuteArray(int segmentNum, int libNum, int repeatCount, int transpose, boolean[] muteArray, byte userID) {
        if (muteArray.length != getMaxTracks()) {
            return false;
        }
        return native_queueJetSegmentMuteArray(segmentNum, libNum, repeatCount, transpose, muteArray, userID);
    }

    public boolean setMuteFlags(int muteFlags, boolean sync) {
        return native_setMuteFlags(muteFlags, sync);
    }

    public boolean setMuteArray(boolean[] muteArray, boolean sync) {
        if (muteArray.length != getMaxTracks()) {
            return false;
        }
        return native_setMuteArray(muteArray, sync);
    }

    public boolean setMuteFlag(int trackId, boolean muteFlag, boolean sync) {
        return native_setMuteFlag(trackId, muteFlag, sync);
    }

    public boolean triggerClip(int clipId) {
        return native_triggerClip(clipId);
    }

    public boolean clearQueue() {
        return native_clearQueue();
    }

    public void setEventListener(OnJetEventListener listener) {
        setEventListener(listener, null);
    }

    public void setEventListener(OnJetEventListener listener, Handler handler) {
        synchronized (this.mEventListenerLock) {
            this.mJetEventListener = listener;
            if (listener == null) {
                this.mEventHandler = null;
            } else if (handler != null) {
                this.mEventHandler = new NativeEventHandler(this, handler.getLooper());
            } else {
                this.mEventHandler = new NativeEventHandler(this, this.mInitializationLooper);
            }
        }
    }

    private static void postEventFromNative(Object jetplayer_ref, int what, int arg1, int arg2) {
        JetPlayer jet = (JetPlayer) ((WeakReference) jetplayer_ref).get();
        if (jet != null && jet.mEventHandler != null) {
            jet.mEventHandler.sendMessage(jet.mEventHandler.obtainMessage(what, arg1, arg2, null));
        }
    }

    private static void logd(String msg) {
        Log.d(TAG, "[ android.media.JetPlayer ] " + msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, "[ android.media.JetPlayer ] " + msg);
    }
}
