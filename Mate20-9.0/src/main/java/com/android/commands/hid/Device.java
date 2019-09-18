package com.android.commands.hid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.os.SomeArgs;

public class Device {
    private static final int MSG_CLOSE_DEVICE = 3;
    private static final int MSG_OPEN_DEVICE = 1;
    private static final int MSG_SEND_REPORT = 2;
    private static final String TAG = "HidDevice";
    /* access modifiers changed from: private */
    public final Object mCond = new Object();
    /* access modifiers changed from: private */
    public final DeviceHandler mHandler;
    private final int mId;
    private final HandlerThread mThread;
    private long mTimeToSend;

    private class DeviceCallback {
        private DeviceCallback() {
        }

        public void onDeviceOpen() {
            Device.this.mHandler.resumeEvents();
        }

        public void onDeviceError() {
            Log.e(Device.TAG, "Device error occurred, closing /dev/uhid");
            Message msg = Device.this.mHandler.obtainMessage(Device.MSG_CLOSE_DEVICE);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }

    private class DeviceHandler extends Handler {
        private int mBarrierToken;
        private long mPtr;

        public DeviceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Device.MSG_OPEN_DEVICE /*1*/:
                    SomeArgs args = (SomeArgs) msg.obj;
                    this.mPtr = Device.nativeOpenDevice((String) args.arg1, args.argi1, args.argi2, args.argi3, (byte[]) args.arg2, new DeviceCallback());
                    pauseEvents();
                    return;
                case Device.MSG_SEND_REPORT /*2*/:
                    if (this.mPtr != 0) {
                        Device.nativeSendReport(this.mPtr, (byte[]) msg.obj);
                        return;
                    } else {
                        Log.e(Device.TAG, "Tried to send report to closed device.");
                        return;
                    }
                case Device.MSG_CLOSE_DEVICE /*3*/:
                    if (this.mPtr != 0) {
                        Device.nativeCloseDevice(this.mPtr);
                        getLooper().quitSafely();
                        this.mPtr = 0;
                    } else {
                        Log.e(Device.TAG, "Tried to close already closed device.");
                    }
                    synchronized (Device.this.mCond) {
                        Device.this.mCond.notify();
                    }
                    return;
                default:
                    throw new IllegalArgumentException("Unknown device message");
            }
        }

        public void pauseEvents() {
            getLooper();
            this.mBarrierToken = Looper.myQueue().postSyncBarrier();
        }

        public void resumeEvents() {
            getLooper();
            Looper.myQueue().removeSyncBarrier(this.mBarrierToken);
            this.mBarrierToken = 0;
        }
    }

    /* access modifiers changed from: private */
    public static native void nativeCloseDevice(long j);

    /* access modifiers changed from: private */
    public static native long nativeOpenDevice(String str, int i, int i2, int i3, byte[] bArr, DeviceCallback deviceCallback);

    /* access modifiers changed from: private */
    public static native void nativeSendReport(long j, byte[] bArr);

    static {
        System.loadLibrary("hidcommand_jni");
    }

    public Device(int id, String name, int vid, int pid, byte[] descriptor, byte[] report) {
        this.mId = id;
        this.mThread = new HandlerThread("HidDeviceHandler");
        this.mThread.start();
        this.mHandler = new DeviceHandler(this.mThread.getLooper());
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = id;
        args.argi2 = vid;
        args.argi3 = pid;
        if (name != null) {
            args.arg1 = name;
        } else {
            args.arg1 = id + ":" + vid + ":" + pid;
        }
        args.arg2 = descriptor;
        args.arg3 = report;
        this.mHandler.obtainMessage(MSG_OPEN_DEVICE, args).sendToTarget();
        this.mTimeToSend = SystemClock.uptimeMillis();
    }

    public void sendReport(byte[] report) {
        this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(MSG_SEND_REPORT, report), this.mTimeToSend);
    }

    public void addDelay(int delay) {
        this.mTimeToSend = Math.max(SystemClock.uptimeMillis(), this.mTimeToSend) + ((long) delay);
    }

    public void close() {
        this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(MSG_CLOSE_DEVICE), Math.max(SystemClock.uptimeMillis(), this.mTimeToSend) + 1);
        try {
            synchronized (this.mCond) {
                this.mCond.wait();
            }
        } catch (InterruptedException e) {
        }
    }
}
