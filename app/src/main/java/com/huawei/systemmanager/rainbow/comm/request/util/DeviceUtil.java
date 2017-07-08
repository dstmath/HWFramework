package com.huawei.systemmanager.rainbow.comm.request.util;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class DeviceUtil {
    private static String mEMUIVersion;
    private static String mPhoneImei;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil.<clinit>():void");
    }

    public static String getTelephoneIMEIFromSys(Context context) {
        if (TextUtils.isEmpty(mPhoneImei)) {
            mPhoneImei = ((TelephonyManager) context.getSystemService("phone")).getImei();
        }
        return mPhoneImei;
    }

    public static String getTelephoneEMUIVersion() {
        if (TextUtils.isEmpty(mEMUIVersion)) {
            mEMUIVersion = SystemProperties.get("ro.build.version.emui", " ");
        }
        return mEMUIVersion;
    }
}
