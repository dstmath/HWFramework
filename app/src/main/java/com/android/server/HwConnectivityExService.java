package com.android.server;

import android.content.Context;
import android.util.Slog;
import huawei.android.net.IConnectivityExManager.Stub;

public class HwConnectivityExService extends Stub {
    private static final String TAG = "HwConnectivityExService";
    static String mSmartKeyguardLevel;
    static boolean useCtrlSocket;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwConnectivityExService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwConnectivityExService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwConnectivityExService.<clinit>():void");
    }

    public HwConnectivityExService(Context context) {
        this.mContext = context;
    }

    public void setSmartKeyguardLevel(String level) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setStaticSmartKeyguardLevel(level);
    }

    private static void setStaticSmartKeyguardLevel(String level) {
        mSmartKeyguardLevel = level;
        Slog.d(TAG, "set mSmartKeyguardLevel = " + mSmartKeyguardLevel);
    }

    private static void setUseCtrlSocketStatic(boolean flag) {
        useCtrlSocket = flag;
    }

    public void setUseCtrlSocket(boolean flag) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setUseCtrlSocketStatic(flag);
        Slog.d(TAG, "set useCtrlSocket = " + useCtrlSocket);
    }
}
