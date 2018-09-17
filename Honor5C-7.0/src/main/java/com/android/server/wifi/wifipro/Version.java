package com.android.server.wifi.wifipro;

import android.content.Context;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class Version {
    private static String BETA_CFG_FILE = null;
    private static final int BETA_VER = 1;
    private static final int NON_BETA_VER = 2;
    private static final int UNKNOWN_VER = 0;
    private static int mVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.Version.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.Version.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.Version.<clinit>():void");
    }

    public static boolean isBeta(Context context) {
        if (mVersion == 0) {
            if (HwCfgFilePolicy.getCfgFile("log_collect_service_beta.xml", 0) == null) {
                Log.d("wifipro", "no log_collect_service_beta.xml exists.");
                if (new File(BETA_CFG_FILE).exists()) {
                    Log.d("wifipro", "isBetaUser");
                    mVersion = BETA_VER;
                } else {
                    mVersion = NON_BETA_VER;
                }
            } else {
                Log.d("wifipro", "isBetaUser");
                mVersion = BETA_VER;
            }
        }
        return false;
    }
}
