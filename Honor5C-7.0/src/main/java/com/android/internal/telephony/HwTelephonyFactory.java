package com.android.internal.telephony;

import android.content.Context;
import android.util.Log;

public class HwTelephonyFactory {
    private static final String TAG = "HwTelephonyFactory";
    private static final Object mLock = null;
    private static volatile HwTelephonyFactoryInterface obj;

    public interface HwTelephonyFactoryInterface {
        HwChrServiceManager getHwChrServiceManager();

        HwDataConnectionManager getHwDataConnectionManager();

        HwDataServiceChrManager getHwDataServiceChrManager();

        HwInnerSmsManager getHwInnerSmsManager();

        HwInnerVSimManager getHwInnerVSimManager();

        HwNetworkManager getHwNetworkManager();

        HwPhoneManager getHwPhoneManager();

        PhoneSubInfoController getHwSubInfoController(Context context, Phone[] phoneArr);

        HwTelephonyBaseManager getHwTelephonyBaseManager();

        HwUiccManager getHwUiccManager();

        HwVolteChrManager getHwVolteChrManager();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwTelephonyFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwTelephonyFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwTelephonyFactory.<clinit>():void");
    }

    private static HwTelephonyFactoryInterface getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (HwTelephonyFactoryInterface) Class.forName("com.android.internal.telephony.HwTelephonyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }

    public static PhoneSubInfoController getHwSubInfoController(Context cxt, Phone[] phone) {
        HwTelephonyFactoryInterface obj = getImplObject();
        if (obj != null) {
            return obj.getHwSubInfoController(cxt, phone);
        }
        return new PhoneSubInfoController(cxt, phone);
    }

    public static HwUiccManager getHwUiccManager() {
        return getImplObject().getHwUiccManager();
    }

    public static HwNetworkManager getHwNetworkManager() {
        return getImplObject().getHwNetworkManager();
    }

    public static HwDataServiceChrManager getHwDataServiceChrManager() {
        return getImplObject().getHwDataServiceChrManager();
    }

    public static HwPhoneManager getHwPhoneManager() {
        return getImplObject().getHwPhoneManager();
    }

    public static HwDataConnectionManager getHwDataConnectionManager() {
        return getImplObject().getHwDataConnectionManager();
    }

    public static HwInnerSmsManager getHwInnerSmsManager() {
        return getImplObject().getHwInnerSmsManager();
    }

    public static HwTelephonyBaseManager getHwTelephonyBaseManager() {
        return getImplObject().getHwTelephonyBaseManager();
    }

    public static HwVolteChrManager getHwVolteChrManager() {
        return getImplObject().getHwVolteChrManager();
    }

    public static HwInnerVSimManager getHwInnerVSimManager() {
        return getImplObject().getHwInnerVSimManager();
    }

    public static HwChrServiceManager getHwChrServiceManager() {
        return getImplObject().getHwChrServiceManager();
    }
}
