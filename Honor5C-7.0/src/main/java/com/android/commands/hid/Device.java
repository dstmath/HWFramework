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
    private final Object mCond;
    private long mEventTime;
    private final DeviceHandler mHandler;
    private final int mId;
    private final HandlerThread mThread;

    private class DeviceCallback {
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
                    this.mPtr = Device.nativeOpenDevice(str, i, i2, i3, bArr, Looper.myQueue(), new DeviceCallback(null));
                    Device.nativeSendReport(this.mPtr, (byte[]) args.arg3);
                    pauseEvents();
                case Device.MSG_SEND_REPORT /*2*/:
                    if (this.mPtr != 0) {
                        Device.nativeSendReport(this.mPtr, (byte[]) msg.obj);
                    } else {
                        Log.e(Device.TAG, "Tried to send report to closed device.");
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
                        break;
                    }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.hid.Device.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.hid.Device.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.hid.Device.<clinit>():void");
    }

    private static native void nativeCloseDevice(long j);

    private static native long nativeOpenDevice(String str, int i, int i2, int i3, byte[] bArr, MessageQueue messageQueue, DeviceCallback deviceCallback);

    private static native void nativeSendReport(long j, byte[] bArr);

    public Device(int id, String name, int vid, int pid, byte[] descriptor, byte[] report) {
        this.mCond = new Object();
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
