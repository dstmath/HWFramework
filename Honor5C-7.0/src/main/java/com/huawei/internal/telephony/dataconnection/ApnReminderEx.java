package com.huawei.internal.telephony.dataconnection;

import android.content.Context;
import com.android.internal.telephony.dataconnection.ApnReminder;

public class ApnReminderEx {
    private static final ApnReminderEx sInstance = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.dataconnection.ApnReminderEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.dataconnection.ApnReminderEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.dataconnection.ApnReminderEx.<clinit>():void");
    }

    public static synchronized ApnReminderEx getInstance() {
        ApnReminderEx apnReminderEx;
        synchronized (ApnReminderEx.class) {
            apnReminderEx = sInstance;
        }
        return apnReminderEx;
    }

    public void restoreApn(Context context, String plmn, String imsi) {
        ApnReminder.getInstance(context).restoreApn(plmn, imsi);
    }

    public void restoreApn(Context context, String plmn, String imsi, int subId) {
        ApnReminder.getInstance(context, subId).restoreApn(plmn, imsi);
    }
}
