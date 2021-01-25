package com.android.commands.hid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;

public class Device {
    private static final int MSG_CLOSE_DEVICE = 4;
    private static final int MSG_OPEN_DEVICE = 1;
    private static final int MSG_SEND_GET_FEATURE_REPORT_REPLY = 3;
    private static final int MSG_SEND_REPORT = 2;
    private static final String TAG = "HidDevice";
    private final Object mCond = new Object();
    private final SparseArray<byte[]> mFeatureReports;
    private final DeviceHandler mHandler;
    private final int mId;
    private final HandlerThread mThread;
    private long mTimeToSend;

    /* access modifiers changed from: private */
    public static native void nativeCloseDevice(long j);

    /* access modifiers changed from: private */
    public static native long nativeOpenDevice(String str, int i, int i2, int i3, byte[] bArr, DeviceCallback deviceCallback);

    /* access modifiers changed from: private */
    public static native void nativeSendGetFeatureReportReply(long j, int i, byte[] bArr);

    /* access modifiers changed from: private */
    public static native void nativeSendReport(long j, byte[] bArr);

    static {
        System.loadLibrary("hidcommand_jni");
    }

    public Device(int id, String name, int vid, int pid, byte[] descriptor, byte[] report, SparseArray<byte[]> featureReports) {
        this.mId = id;
        this.mThread = new HandlerThread("HidDeviceHandler");
        this.mThread.start();
        this.mHandler = new DeviceHandler(this.mThread.getLooper());
        this.mFeatureReports = featureReports;
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

    /* access modifiers changed from: private */
    public class DeviceHandler extends Handler {
        private int mBarrierToken;
        private long mPtr;

        public DeviceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == Device.MSG_OPEN_DEVICE) {
                SomeArgs args = (SomeArgs) msg.obj;
                this.mPtr = Device.nativeOpenDevice((String) args.arg1, args.argi1, args.argi2, args.argi3, (byte[]) args.arg2, new DeviceCallback());
                pauseEvents();
            } else if (i == Device.MSG_SEND_REPORT) {
                long j = this.mPtr;
                if (j != 0) {
                    Device.nativeSendReport(j, (byte[]) msg.obj);
                } else {
                    Log.e(Device.TAG, "Tried to send report to closed device.");
                }
            } else if (i == Device.MSG_SEND_GET_FEATURE_REPORT_REPLY) {
                long j2 = this.mPtr;
                if (j2 != 0) {
                    Device.nativeSendGetFeatureReportReply(j2, msg.arg1, (byte[]) msg.obj);
                } else {
                    Log.e(Device.TAG, "Tried to send feature report reply to closed device.");
                }
            } else if (i == Device.MSG_CLOSE_DEVICE) {
                long j3 = this.mPtr;
                if (j3 != 0) {
                    Device.nativeCloseDevice(j3);
                    getLooper().quitSafely();
                    this.mPtr = 0;
                } else {
                    Log.e(Device.TAG, "Tried to close already closed device.");
                }
                synchronized (Device.this.mCond) {
                    Device.this.mCond.notify();
                }
            } else {
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
    public class DeviceCallback {
        private DeviceCallback() {
        }

        public void onDeviceOpen() {
            Device.this.mHandler.resumeEvents();
        }

        public void onDeviceGetReport(int requestId, int reportId) {
            byte[] report = (byte[]) Device.this.mFeatureReports.get(reportId);
            if (report == null) {
                Log.e(Device.TAG, "Requested feature report " + reportId + " is not specified");
            }
            Message msg = Device.this.mHandler.obtainMessage(Device.MSG_SEND_GET_FEATURE_REPORT_REPLY, requestId, 0, report);
            msg.setAsynchronous(true);
            Device.this.mHandler.sendMessageAtTime(msg, Device.this.mTimeToSend);
        }

        public void onDeviceError() {
            Log.e(Device.TAG, "Device error occurred, closing /dev/uhid");
            Message msg = Device.this.mHandler.obtainMessage(Device.MSG_CLOSE_DEVICE);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }
}
