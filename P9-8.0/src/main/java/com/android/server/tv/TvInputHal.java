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
    private static final String TAG = TvInputHal.class.getSimpleName();
    private final Callback mCallback;
    private final Handler mHandler;
    private final Object mLock = new Object();
    private final Queue<Message> mPendingMessageQueue = new LinkedList();
    private long mPtr = 0;
    private final SparseIntArray mStreamConfigGenerations = new SparseIntArray();
    private final SparseArray<TvStreamConfig[]> mStreamConfigs = new SparseArray();

    public interface Callback {
        void onDeviceAvailable(TvInputHardwareInfo tvInputHardwareInfo, TvStreamConfig[] tvStreamConfigArr);

        void onDeviceUnavailable(int i);

        void onFirstFrameCaptured(int i, int i2);

        void onStreamConfigurationChanged(int i, TvStreamConfig[] tvStreamConfigArr);
    }

    private static native int nativeAddOrUpdateStream(long j, int i, int i2, Surface surface);

    private static native void nativeClose(long j);

    private static native TvStreamConfig[] nativeGetStreamConfigs(long j, int i, int i2);

    private native long nativeOpen(MessageQueue messageQueue);

    private static native int nativeRemoveStream(long j, int i, int i2);

    public TvInputHal(Callback callback) {
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
                return -1;
            } else if (this.mStreamConfigGenerations.get(deviceId, 0) != streamConfig.getGeneration()) {
                return -2;
            } else if (nativeAddOrUpdateStream(this.mPtr, deviceId, streamConfig.getStreamId(), surface) == 0) {
                return 0;
            } else {
                return -3;
            }
        }
    }

    public int removeStream(int deviceId, TvStreamConfig streamConfig) {
        synchronized (this.mLock) {
            if (this.mPtr == 0) {
                return -1;
            } else if (this.mStreamConfigGenerations.get(deviceId, 0) != streamConfig.getGeneration()) {
                return -2;
            } else if (nativeRemoveStream(this.mPtr, deviceId, streamConfig.getStreamId()) == 0) {
                return 0;
            } else {
                return -3;
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
        int generation = this.mStreamConfigGenerations.get(deviceId, 0) + 1;
        this.mStreamConfigs.put(deviceId, nativeGetStreamConfigs(this.mPtr, deviceId, generation));
        this.mStreamConfigGenerations.put(deviceId, generation);
    }

    private void deviceAvailableFromNative(TvInputHardwareInfo info) {
        this.mHandler.obtainMessage(1, info).sendToTarget();
    }

    private void deviceUnavailableFromNative(int deviceId) {
        this.mHandler.obtainMessage(2, deviceId, 0).sendToTarget();
    }

    private void streamConfigsChangedFromNative(int deviceId) {
        this.mHandler.obtainMessage(3, deviceId, 0).sendToTarget();
    }

    private void firstFrameCapturedFromNative(int deviceId, int streamId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, deviceId, streamId));
    }

    public boolean handleMessage(Message msg) {
        TvStreamConfig[] configs;
        switch (msg.what) {
            case 1:
                TvInputHardwareInfo info = msg.obj;
                synchronized (this.mLock) {
                    retrieveStreamConfigsLocked(info.getDeviceId());
                    configs = (TvStreamConfig[]) this.mStreamConfigs.get(info.getDeviceId());
                }
                this.mCallback.onDeviceAvailable(info, configs);
                break;
            case 2:
                this.mCallback.onDeviceUnavailable(msg.arg1);
                break;
            case 3:
                int deviceId = msg.arg1;
                synchronized (this.mLock) {
                    retrieveStreamConfigsLocked(deviceId);
                    configs = (TvStreamConfig[]) this.mStreamConfigs.get(deviceId);
                }
                this.mCallback.onStreamConfigurationChanged(deviceId, configs);
                break;
            case 4:
                this.mCallback.onFirstFrameCaptured(msg.arg1, msg.arg2);
                break;
            default:
                Slog.e(TAG, "Unknown event: " + msg);
                return false;
        }
        return true;
    }
}
