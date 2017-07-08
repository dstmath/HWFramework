package android.media;

import android.app.ActivityThread;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.security.keymaster.KeymasterDefs;
import android.util.AndroidRuntimeException;
import android.util.Log;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SoundPool {
    private static final boolean DEBUG = false;
    private static final int SAMPLE_LOADED = 1;
    private static final String TAG = "SoundPool";
    private static IAudioService sService;
    private final IAppOpsService mAppOps;
    private final IAppOpsCallback mAppOpsCallback;
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
        private int mMaxStreams;

        public Builder() {
            this.mMaxStreams = SoundPool.SAMPLE_LOADED;
        }

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
                this.mAudioAttributes = new android.media.AudioAttributes.Builder().setUsage(SoundPool.SAMPLE_LOADED).build();
            }
            return new SoundPool(this.mAudioAttributes, null);
        }
    }

    private final class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SoundPool.SAMPLE_LOADED /*1*/:
                    if (SoundPool.DEBUG) {
                        Log.d(SoundPool.TAG, "Sample " + msg.arg1 + " loaded");
                    }
                    synchronized (SoundPool.this.mLock) {
                        if (SoundPool.this.mOnLoadCompleteListener != null) {
                            SoundPool.this.mOnLoadCompleteListener.onLoadComplete(SoundPool.this, msg.arg1, msg.arg2);
                        }
                        break;
                    }
                default:
                    Log.e(SoundPool.TAG, "Unknown message type " + msg.what);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.SoundPool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.SoundPool.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.SoundPool.<clinit>():void");
    }

    private final native int _load(FileDescriptor fileDescriptor, long j, long j2, int i);

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

    public SoundPool(int maxStreams, int streamType, int srcQuality) {
        this(maxStreams, new android.media.AudioAttributes.Builder().setInternalLegacyStreamType(streamType).build());
    }

    private SoundPool(int maxStreams, AudioAttributes attributes) {
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(AudioAttributes.toLegacyStreamType(attributes)), TAG);
        if (native_setup(new WeakReference(this), maxStreams, attributes) != 0) {
            throw new RuntimeException("Native setup failed");
        }
        this.mLock = new Object();
        this.mAttributes = attributes;
        this.mAppOps = Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        updateAppOpsPlayAudio();
        this.mAppOpsCallback = new IAppOpsCallback.Stub() {
            public void opChanged(int op, int uid, String packageName) {
                synchronized (SoundPool.this.mLock) {
                    if (op == 28) {
                        SoundPool.this.updateAppOpsPlayAudio();
                    }
                }
            }
        };
        try {
            this.mAppOps.startWatchingMode(28, ActivityThread.currentPackageName(), this.mAppOpsCallback);
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = DEBUG;
        }
    }

    public final void release() {
        try {
            this.mAppOps.stopWatchingMode(this.mAppOpsCallback);
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        native_release();
    }

    protected void finalize() {
        release();
    }

    public int load(String path, int priority) {
        try {
            File f = new File(path);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(f, KeymasterDefs.KM_ENUM);
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
        if (len >= 0) {
            return _load(afd.getFileDescriptor(), afd.getStartOffset(), len, priority);
        }
        throw new AndroidRuntimeException("no length for fd");
    }

    public int load(FileDescriptor fd, long offset, long length, int priority) {
        return _load(fd, offset, length, priority);
    }

    public final int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
        if (isRestricted()) {
            rightVolume = 0.0f;
            leftVolume = 0.0f;
        }
        return _play(soundID, leftVolume, rightVolume, priority, loop, rate);
    }

    public final void setVolume(int streamID, float leftVolume, float rightVolume) {
        if (!isRestricted()) {
            _setVolume(streamID, leftVolume, rightVolume);
        }
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

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
        return sService;
    }

    private boolean isRestricted() {
        if (this.mHasAppOpsPlayAudio || (this.mAttributes.getAllFlags() & 64) != 0) {
            return DEBUG;
        }
        if ((this.mAttributes.getAllFlags() & SAMPLE_LOADED) != 0) {
            boolean cameraSoundForced = DEBUG;
            try {
                cameraSoundForced = getService().isCameraSoundForced();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot access AudioService in isRestricted()");
            } catch (NullPointerException e2) {
                Log.e(TAG, "Null AudioService in isRestricted()");
            }
            if (cameraSoundForced) {
                return DEBUG;
            }
        }
        return true;
    }

    private void updateAppOpsPlayAudio() {
        try {
            this.mHasAppOpsPlayAudio = this.mAppOps.checkAudioOperation(28, this.mAttributes.getUsage(), Process.myUid(), ActivityThread.currentPackageName()) == 0 ? true : DEBUG;
        } catch (RemoteException e) {
            this.mHasAppOpsPlayAudio = DEBUG;
        }
    }

    private static void postEventFromNative(Object ref, int msg, int arg1, int arg2, Object obj) {
        SoundPool soundPool = (SoundPool) ((WeakReference) ref).get();
        if (!(soundPool == null || soundPool.mEventHandler == null)) {
            soundPool.mEventHandler.sendMessage(soundPool.mEventHandler.obtainMessage(msg, arg1, arg2, obj));
        }
    }
}
