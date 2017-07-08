package com.huawei.android.hardware;

import android.util.Log;

public class CameraEx {
    public static final int CAMERA_ID0 = 0;
    public static final int CAMERA_ID1 = 1;
    public static final int CAMERA_ID2 = 2;
    public static final int CAMERA_ID3 = 3;
    public static final int CAMERA_SENSOR0 = 0;
    public static final int CAMERA_SENSOR1 = 1;
    public static final int CAMERA_SENSOR2 = 2;
    public static final int CAMERA_SENSOR3 = 3;
    private static final String TAG = "CameraEx";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hardware.CameraEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hardware.CameraEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hardware.CameraEx.<clinit>():void");
    }

    private static final native int hw_native_getFocusContrast();

    private static final native int hw_native_getFocusValue();

    public int getFocusValue() {
        return getFocusValue(CAMERA_SENSOR0, CAMERA_SENSOR0);
    }

    public int getFocusValue(int id, int index) {
        Log.i(TAG, "getFocusValue()");
        return hw_native_getFocusValue();
    }

    public int getFocusContrast() {
        return getFocusContrast(CAMERA_SENSOR0, CAMERA_SENSOR0);
    }

    public int getFocusContrast(int id, int index) {
        Log.i(TAG, "getFocusContrast()");
        return hw_native_getFocusContrast();
    }
}
