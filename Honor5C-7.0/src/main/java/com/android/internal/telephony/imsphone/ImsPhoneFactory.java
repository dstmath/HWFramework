package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;

public class ImsPhoneFactory {
    private static final String IMS_SERVICE_CLASS_NAME = null;
    private static final String IMS_SERVICE_PKG_NAME = null;
    private static final boolean isImsAsNormal = false;
    private static final boolean volte = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.imsphone.ImsPhoneFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.imsphone.ImsPhoneFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneFactory.<clinit>():void");
    }

    public static ImsPhone makePhone(Context context, PhoneNotifier phoneNotifier, Phone defaultPhone) {
        try {
            return new ImsPhone(context, phoneNotifier, defaultPhone);
        } catch (Exception e) {
            Rlog.e("VoltePhoneFactory", "makePhone", e);
            return null;
        }
    }

    public static void startImsService(Context context) {
        if (volte) {
            Rlog.d("ImsPhoneFactory", "startImsService");
            try {
                Intent intent = new Intent();
                intent.setClassName(IMS_SERVICE_PKG_NAME, IMS_SERVICE_CLASS_NAME);
                context.startService(intent);
            } catch (SecurityException ex) {
                Rlog.w("ImsPhoneFactory", "startImsService: exception = " + ex);
            }
        }
    }

    public static boolean isimsAsNormalCon() {
        return isImsAsNormal;
    }
}
