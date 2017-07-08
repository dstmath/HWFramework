package com.android.server.security.trustcircle.jni;

import android.content.Context;
import com.android.server.security.trustcircle.utils.LogHelper;

public class TcisJNI {
    public static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.jni.TcisJNI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.jni.TcisJNI.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.jni.TcisJNI.<clinit>():void");
    }

    public static final native byte[] nativeProcessCmd(Context context, byte[] bArr);

    public static final native int nativeStart();

    public static final native void nativeStop();

    public static final byte[] processCmd(Context context, byte[] param) {
        byte[] nativeProcessCmd;
        synchronized (TcisJNI.class) {
            nativeProcessCmd = nativeProcessCmd(context, param);
        }
        return nativeProcessCmd;
    }

    public static final int start() {
        int nativeStart;
        synchronized (TcisJNI.class) {
            try {
                nativeStart = nativeStart();
            } catch (Exception e) {
                LogHelper.e(TAG, "error, nativeStart failed, " + e.getMessage());
                return -1;
            }
        }
        return nativeStart;
    }

    public static final void stop() {
        synchronized (TcisJNI.class) {
            try {
                nativeStop();
            } catch (Exception e) {
                LogHelper.e(TAG, "error, nativeStop failed, " + e.getMessage());
            }
        }
    }
}
