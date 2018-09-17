package com.android.server.tv;

import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvStreamConfig;
import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import java.util.LinkedList;
import java.util.Queue;

final class TvInputHal implements android.os.Handler.Callback {
    private static final boolean DEBUG = false;
    public static final int ERROR_NO_INIT = -1;
    public static final int ERROR_STALE_CONFIG = -2;
    public static final int ERROR_UNKNOWN = -3;
    public static final int EVENT_DEVICE_AVAILABLE = 1;
    public static final int EVENT_DEVICE_UNAVAILABLE = 2;
    public static final int EVENT_FIRST_FRAME_CAPTURED = 4;
    public static final int EVENT_STREAM_CONFIGURATION_CHANGED = 3;
    public static final int SUCCESS = 0;
    private static final String TAG = null;
    private final Callback mCallback;
    private final Handler mHandler;
    private final Object mLock;
    private final Queue<Message> mPendingMessageQueue;
    private long mPtr;
    private final SparseIntArray mStreamConfigGenerations;
    private final SparseArray<TvStreamConfig[]> mStreamConfigs;

    public interface Callback {
        void onDeviceAvailable(TvInputHardwareInfo tvInputHardwareInfo, TvStreamConfig[] tvStreamConfigArr);

        void onDeviceUnavailable(int i);

        void onFirstFrameCaptured(int i, int i2);

        void onStreamConfigurationChanged(int i, TvStreamConfig[] tvStreamConfigArr);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.tv.TvInputHal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.tv.TvInputHal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.tv.TvInputHal.<clinit>():void");
    }

    private static native int nativeAddOrUpdateStream(long j, int i, int i2, Surface surface);

    private static native void nativeClose(long j);

    private static native TvStreamConfig[] nativeGetStreamConfigs(long j, int i, int i2);

    private native long nativeOpen(MessageQueue messageQueue);

    private static native int nativeRemoveStream(long j, int i, int i2);

    public TvInputHal(Callback callback) {
        this.mLock = new Object();
        this.mPtr = 0;
        this.mStreamConfigGenerations = new SparseIntArray();
        this.mStreamConfigs = new SparseArray();
        this.mPendingMessageQueue = new LinkedList();
        this.mCallback = callback;
        this.mHandler = new Handler(this);
    }

    public void init() {
        synchronized (this.mLock) {
            this.mPtr = nativeOpen(this.mHandler.getLooper().getQueue());
        }
    }

    public int addOrUpdateStream(int deviceId, Surface surface, TvStreamConfig streamConfig) {
        synchronized (this.mLock) {
            if (this.mPtr == 0) {
                return ERROR_NO_INIT;
            } else if (this.mStreamConfigGenerations.get(deviceId, 0) != streamConfig.getGeneration()) {
                return ERROR_STALE_CONFIG;
            } else if (nativeAddOrUpdateStream(this.mPtr, deviceId, streamConfig.getStreamId(), surface) == 0) {
                return 0;
            } else {
                return ERROR_UNKNOWN;
            }
        }
    }

    public int removeStream(int deviceId, TvStreamConfig streamConfig) {
        synchronized (this.mLock) {
            if (this.mPtr == 0) {
                return ERROR_NO_INIT;
            } else if (this.mStreamConfigGenerations.get(deviceId, 0) != streamConfig.getGeneration()) {
                return ERROR_STALE_CONFIG;
            } else if (nativeRemoveStream(this.mPtr, deviceId, streamConfig.getStreamId()) == 0) {
                return 0;
            } else {
                return ERROR_UNKNOWN;
            }
        }
    }

    public void close() {
        synchronized (this.mLock) {
            if (this.mPtr != 0) {
                nativeClose(this.mPtr);
            }
        }
    }

    private void retrieveStreamConfigsLocked(int deviceId) {
        int generation = this.mStreamConfigGenerations.get(deviceId, 0) + EVENT_DEVICE_AVAILABLE;
        this.mStreamConfigs.put(deviceId, nativeGetStreamConfigs(this.mPtr, deviceId, generation));
        this.mStreamConfigGenerations.put(deviceId, generation);
    }

    private void deviceAvailableFromNative(TvInputHardwareInfo info) {
        this.mHandler.obtainMessage(EVENT_DEVICE_AVAILABLE, info).sendToTarget();
    }

    private void deviceUnavailableFromNative(int deviceId) {
        this.mHandler.obtainMessage(EVENT_DEVICE_UNAVAILABLE, deviceId, 0).sendToTarget();
    }

    private void streamConfigsChangedFromNative(int deviceId) {
        this.mHandler.obtainMessage(EVENT_STREAM_CONFIGURATION_CHANGED, deviceId, 0).sendToTarget();
    }

    private void firstFrameCapturedFromNative(int deviceId, int streamId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_STREAM_CONFIGURATION_CHANGED, deviceId, streamId));
    }

    public boolean handleMessage(Message msg) {
        TvStreamConfig[] configs;
        switch (msg.what) {
            case EVENT_DEVICE_AVAILABLE /*1*/:
                TvInputHardwareInfo info = msg.obj;
                synchronized (this.mLock) {
                    retrieveStreamConfigsLocked(info.getDeviceId());
                    configs = (TvStreamConfig[]) this.mStreamConfigs.get(info.getDeviceId());
                    break;
                }
                this.mCallback.onDeviceAvailable(info, configs);
                break;
            case EVENT_DEVICE_UNAVAILABLE /*2*/:
                this.mCallback.onDeviceUnavailable(msg.arg1);
                break;
            case EVENT_STREAM_CONFIGURATION_CHANGED /*3*/:
                int deviceId = msg.arg1;
                synchronized (this.mLock) {
                    retrieveStreamConfigsLocked(deviceId);
                    configs = (TvStreamConfig[]) this.mStreamConfigs.get(deviceId);
                    break;
                }
                this.mCallback.onStreamConfigurationChanged(deviceId, configs);
                break;
            case EVENT_FIRST_FRAME_CAPTURED /*4*/:
                this.mCallback.onFirstFrameCaptured(msg.arg1, msg.arg2);
                break;
            default:
                Slog.e(TAG, "Unknown event: " + msg);
                return DEBUG;
        }
        return true;
    }
}
