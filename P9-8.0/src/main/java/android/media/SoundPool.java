package android.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.VolumeShaper.Configuration;
import android.media.VolumeShaper.Operation;
import android.media.VolumeShaper.State;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.AndroidRuntimeException;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SoundPool extends PlayerBase {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int SAMPLE_LOADED = 1;
    private static final String TAG = "SoundPool";
    private static IAudioService sService;
    private final AudioAttributes mAttributes;
    private EventHandler mEventHandler;
    private boolean mHasAppOpsPlayAudio;
    private final Object mLock;
    private long mNativeContext;
    private OnLoadCompleteListener mOnLoadCompleteListener;

    public interface OnLoadCompleteListener {
        void onLoadComplete(SoundPool soundPool, int i, int i2);
    }

    public static class Builder {
        private AudioAttributes mAudioAttributes;
        private int mMaxStreams = 1;

        public Builder setMaxStreams(int maxStreams) throws IllegalArgumentException {
            if (maxStreams <= 0) {
                throw new IllegalArgumentException("Strictly positive value required for the maximum number of streams");
            }
            this.mMaxStreams = maxStreams;
            return this;
        }

        public Builder setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
            if (attributes == null) {
                throw new IllegalArgumentException("Invalid null AudioAttributes");
            }
            this.mAudioAttributes = attributes;
            return this;
        }

        public SoundPool build() {
            if (this.mAudioAttributes == null) {
                this.mAudioAttributes = new android.media.AudioAttributes.Builder().setUsage(1).build();
            }
            return new SoundPool(this.mMaxStreams, this.mAudioAttributes, null);
        }
    }

    private final class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (SoundPool.DEBUG) {
                        Log.d(SoundPool.TAG, "Sample " + msg.arg1 + " loaded");
                    }
                    synchronized (SoundPool.this.mLock) {
                        if (SoundPool.this.mOnLoadCompleteListener != null) {
                            SoundPool.this.mOnLoadCompleteListener.onLoadComplete(SoundPool.this, msg.arg1, msg.arg2);
                        }
                    }
                    return;
                default:
                    Log.e(SoundPool.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    private final native int _load(FileDescriptor fileDescriptor, long j, long j2, int i);

    private final native void _mute(boolean z);

    private final native int _play(int i, float f, float f2, int i2, int i3, float f3);

    private final native void _setVolume(int i, float f, float f2);

    private final native void native_release();

    private final native int native_setup(Object obj, int i, Object obj2);

    public final native void autoPause();

    public final native void autoResume();

    public final native void pause(int i);

    public final native void resume(int i);

    public final native void setLoop(int i, int i2);

    public final native void setPriority(int i, int i2);

    public final native void setRate(int i, float f);

    public final native void stop(int i);

    public final native boolean unload(int i);

    static {
        System.loadLibrary("soundpool");
    }

    public SoundPool(int maxStreams, int streamType, int srcQuality) {
        this(maxStreams, new android.media.AudioAttributes.Builder().setInternalLegacyStreamType(streamType).build());
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "SoundPool()");
    }

    private SoundPool(int maxStreams, AudioAttributes attributes) {
        super(attributes, 3);
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(AudioAttributes.toLegacyStreamType(attributes)), TAG);
        if (native_setup(new WeakReference(this), maxStreams, attributes) != 0) {
            throw new RuntimeException("Native setup failed");
        }
        this.mLock = new Object();
        this.mAttributes = attributes;
        baseRegisterPlayer();
    }

    public final void release() {
        baseRelease();
        native_release();
    }

    protected void finalize() {
        release();
    }

    public int load(String path, int priority) {
        try {
            File f = new File(path);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(f, 268435456);
            if (fd == null) {
                return 0;
            }
            int id = _load(fd.getFileDescriptor(), 0, f.length(), priority);
            fd.close();
            return id;
        } catch (IOException e) {
            Log.e(TAG, "error loading " + path);
            return 0;
        }
    }

    public int load(Context context, int resId, int priority) {
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
        int id = 0;
        if (afd != null) {
            id = _load(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), priority);
            try {
                afd.close();
            } catch (IOException e) {
            }
        }
        return id;
    }

    public int load(AssetFileDescriptor afd, int priority) {
        if (afd == null) {
            return 0;
        }
        long len = afd.getLength();
        if (len < 0) {
            throw new AndroidRuntimeException("no length for fd");
        }
        return _load(afd.getFileDescriptor(), afd.getStartOffset(), len, priority);
    }

    public int load(FileDescriptor fd, long offset, long length, int priority) {
        return _load(fd, offset, length, priority);
    }

    public final int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
        baseStart();
        return _play(soundID, leftVolume, rightVolume, priority, loop, rate);
    }

    public final void setVolume(int streamID, float leftVolume, float rightVolume) {
        _setVolume(streamID, leftVolume, rightVolume);
    }

    int playerApplyVolumeShaper(Configuration configuration, Operation operation) {
        return -1;
    }

    State playerGetVolumeShaperState(int id) {
        return null;
    }

    void playerSetVolume(boolean muting, float leftVolume, float rightVolume) {
        _mute(muting);
    }

    int playerSetAuxEffectSendLevel(boolean muting, float level) {
        return 0;
    }

    void playerStart() {
    }

    void playerPause() {
    }

    void playerStop() {
    }

    public void setVolume(int streamID, float volume) {
        setVolume(streamID, volume, volume);
    }

    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
        synchronized (this.mLock) {
            if (listener != null) {
                Looper looper = Looper.myLooper();
                if (looper != null) {
                    this.mEventHandler = new EventHandler(looper);
                } else {
                    looper = Looper.getMainLooper();
                    if (looper != null) {
                        this.mEventHandler = new EventHandler(looper);
                    } else {
                        this.mEventHandler = null;
                    }
                }
            } else {
                this.mEventHandler = null;
            }
            this.mOnLoadCompleteListener = listener;
        }
    }

    private static void postEventFromNative(Object ref, int msg, int arg1, int arg2, Object obj) {
        SoundPool soundPool = (SoundPool) ((WeakReference) ref).get();
        if (!(soundPool == null || soundPool.mEventHandler == null)) {
            soundPool.mEventHandler.sendMessage(soundPool.mEventHandler.obtainMessage(msg, arg1, arg2, obj));
        }
    }
}
