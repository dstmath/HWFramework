package com.android.internal.telephony;

import android.util.Log;

public class HwAddonTelephonyFactory {
    private static final String TAG = "HwAddonTelephonyFactory";
    private static final Object mLock = null;
    private static volatile HwAddonTelephonyInterface obj;

    public interface HwAddonTelephonyInterface {
        int getDefault4GSlotId();
    }

    private static class HwAddonTelephonyDefaultImpl implements HwAddonTelephonyInterface {
        private HwAddonTelephonyDefaultImpl() {
        }

        public int getDefault4GSlotId() {
            return 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwAddonTelephonyFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwAddonTelephonyFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwAddonTelephonyFactory.<clinit>():void");
    }

    private static HwAddonTelephonyInterface getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (HwAddonTelephonyInterface) Class.forName("com.huawei.android.telephony.HwAddonTelephonyImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return new HwAddonTelephonyDefaultImpl();
    }

    public static HwAddonTelephonyInterface getTelephony() {
        return getImplObject();
    }
}
