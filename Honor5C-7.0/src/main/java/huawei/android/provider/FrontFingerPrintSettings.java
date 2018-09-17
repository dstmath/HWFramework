package huawei.android.provider;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings.System;
import huawei.android.os.HwGeneralManager;

public class FrontFingerPrintSettings {
    public static final boolean FRONT_FINGERPRINT_NAVIGATION = false;
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.FrontFingerPrintSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.FrontFingerPrintSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.FrontFingerPrintSettings.<clinit>():void");
    }

    public static synchronized boolean isNaviBarEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FrontFingerPrintSettings.class) {
            int NAVI_BAR_DEFAULT_STATUS = 1;
            if (FRONT_FINGERPRINT_NAVIGATION) {
                if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                    if (isChinaArea()) {
                        NAVI_BAR_DEFAULT_STATUS = 0;
                    } else {
                        NAVI_BAR_DEFAULT_STATUS = 1;
                    }
                } else if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                    return false;
                }
                if (System.getIntForUser(resolver, HwSettings.System.NAVIGATION_BAR_ENABLE, NAVI_BAR_DEFAULT_STATUS, ActivityManager.getCurrentUser()) != 1) {
                    z = false;
                }
                return z;
            }
            return true;
        }
    }

    public static int getDefaultNaviMode() {
        if (!isSupportTrikey()) {
            return -1;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            return -1;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1) {
            return -1;
        }
        if (isChinaArea()) {
            return -1;
        }
        return 0;
    }

    public static boolean isSupportTrikey() {
        return HwGeneralManager.getInstance().isSupportTrikey();
    }

    public static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF).equals("156");
    }
}
