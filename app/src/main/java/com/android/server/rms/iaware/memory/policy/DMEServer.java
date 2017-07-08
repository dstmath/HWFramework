package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import java.util.concurrent.atomic.AtomicBoolean;

public class DMEServer {
    private static final String TAG = "AwareMem_DMEServer";
    private static final Object mLock = null;
    private static DMEServer sDMEServer;
    private final AtomicBoolean mRunning;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.policy.DMEServer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.policy.DMEServer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.policy.DMEServer.<clinit>():void");
    }

    public DMEServer() {
        this.mRunning = new AtomicBoolean(false);
    }

    public static DMEServer getInstance() {
        DMEServer dMEServer;
        synchronized (mLock) {
            if (sDMEServer == null) {
                sDMEServer = new DMEServer();
            }
            dMEServer = sDMEServer;
        }
        return dMEServer;
    }

    public void setHandler(HandlerThread handlerThread) {
        AwareLog.d(TAG, "setHandler: object=" + handlerThread);
        if (handlerThread != null) {
            MemoryExecutorServer.getInstance().setMemHandlerThread(handlerThread);
        } else {
            AwareLog.e(TAG, "setHandler: why handlerThread is null!!");
        }
    }

    public void enable() {
        if (!this.mRunning.get()) {
            this.mRunning.set(true);
            MemoryExecutorServer.getInstance().enable();
            AwareLog.i(TAG, "start");
        }
    }

    public void disable() {
        if (this.mRunning.get()) {
            this.mRunning.set(false);
            MemoryExecutorServer.getInstance().disable();
            AwareLog.i(TAG, "stop");
        }
    }

    public void stopExecute(long timestamp, int event) {
        if (this.mRunning.get()) {
            MemoryExecutorServer.getInstance().stopMemoryRecover();
            AwareLog.d(TAG, "stopExecuteMemoryRecover event=" + event);
            EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_STOP, event, timestamp, null);
            return;
        }
        AwareLog.i(TAG, "stopMemoryRecover iaware not running");
    }

    public void execute(String scene, Bundle extras, int event, long timeStamp) {
        if (this.mRunning.get()) {
            MemoryExecutorServer.getInstance().executeMemoryRecover(scene, extras, event, timeStamp);
        } else {
            AwareLog.i(TAG, "executeMemoryRecover iaware not running");
        }
    }
}
