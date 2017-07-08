package com.android.server.accessibility;

import android.content.Context;
import android.util.Log;

public class HwMagnificationFactory {
    private static final String TAG = "HwMagnificationFactory";
    private static final Object mLock = null;
    private static volatile Factory obj;

    public interface Factory {
        IMagnificationGestureHandler getHwMagnificationGestureHandler();
    }

    public interface IMagnificationGestureHandler {
        MagnificationGestureHandler getInstance(Context context, AccessibilityManagerService accessibilityManagerService, boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.HwMagnificationFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accessibility.HwMagnificationFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.HwMagnificationFactory.<clinit>():void");
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.accessibility.HwMagnificationFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
            Log.v(TAG, "get allimpl object = " + obj);
        }
        return obj;
    }

    public static IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMagnificationGestureHandler();
        }
        return null;
    }
}
