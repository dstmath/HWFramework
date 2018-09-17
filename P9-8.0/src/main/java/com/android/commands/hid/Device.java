package com.android.commands.hid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.os.SomeArgs;

public class Device {
    private static final int MIN_WAIT_FOR_FIRST_EVENT = 150;
    private static final int MSG_CLOSE_DEVICE = 3;
    private static final int MSG_OPEN_DEVICE = 1;
    private static final int MSG_SEND_REPORT = 2;
    private static final String TAG = "HidDevice";
    private final Object mCond = new Object();
    private long mEventTime;
    private final DeviceHandler mHandler;
    private final int mId;
    private final HandlerThread mThread;

    private class DeviceCallback {
        /* synthetic */ DeviceCallback(Device this$0, DeviceCallback -this1) {
            this();
        }

        private DeviceCallback() {
        }

        public void onDeviceOpen() {
            Device.this.mHandler.resumeEvents();
        }

        public void onDeviceError() {
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
                    SomeArgs args = msg.obj;
                    String str = (String) args.arg1;
                    int i = args.argi1;
                    int i2 = args.argi2;
                    int i3 = args.argi3;
                    byte[] bArr = (byte[]) args.arg2;
                    getLooper();
                    this.mPtr = Device.nativeOpenDevice(str, i, i2, i3, bArr, Looper.myQueue(), new DeviceCallback(Device.this, null));
                    Device.nativeSendReport(this.mPtr, (byte[]) args.arg3);
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

    private static native void nativeCloseDevice(long j);

    private static native long nativeOpenDevice(String str, int i, int i2, int i3, byte[] bArr, MessageQueue messageQueue, DeviceCallback deviceCallback);

    private static native void nativeSendReport(long j, byte[] bArr);

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
        this.mEventTime = SystemClock.uptimeMillis() + 150;
    }

    public void sendReport(byte[] report) {
        this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(MSG_SEND_REPORT, report), this.mEventTime);
    }

    public void addDelay(int delay) {
        this.mEventTime += (long) delay;
    }

    public void close() {
        Message msg = this.mHandler.obtainMessage(MSG_CLOSE_DEVICE);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, this.mEventTime + 1);
        try {
            synchronized (this.mCond) {
                this.mCond.wait();
            }
        } catch (InterruptedException e) {
        }
    }
}
