package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.util.Log;

public class FpsRequest implements IFpsRequest {
    private static final String TAG = "Hwaps";
    private long mNativeObject;

    public enum SceneTypeE {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.FpsRequest.SceneTypeE.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.FpsRequest.SceneTypeE.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.FpsRequest.SceneTypeE.<clinit>():void");
        }
    }

    public FpsRequest() {
        this(SceneTypeE.DEFAULT);
    }

    public FpsRequest(SceneTypeE type) {
        Log.d(TAG, "Fpsrequest create,type:" + type);
        this.mNativeObject = HwApsInterface.nativeInitFpsRequest((long) type.ordinal());
    }

    public void start(int fps) {
        HwApsInterface.nativeStart(this.mNativeObject, fps);
    }

    public void startFeedback(int fps_increment) {
        HwApsInterface.nativeStartFeedback(this.mNativeObject, fps_increment);
    }

    public void stop() {
        HwApsInterface.nativeStop(this.mNativeObject);
    }

    public int getCurFPS() {
        return HwApsInterface.nativeGetCurFPS(this.mNativeObject);
    }

    public int getTargetFPS() {
        return HwApsInterface.nativeGetTargetFPS(this.mNativeObject);
    }

    protected void finalize() throws Throwable {
        try {
            HwApsInterface.nativeFpsRequestRelease(this.mNativeObject);
        } finally {
            super.finalize();
        }
    }
}
