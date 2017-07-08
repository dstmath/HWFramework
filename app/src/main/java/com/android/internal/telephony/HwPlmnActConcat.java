package com.android.internal.telephony;

import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwPlmnActConcat {
    private static final String TAG = "HwPlmnActConcat";
    private static boolean mNeedConcat;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwPlmnActConcat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwPlmnActConcat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwPlmnActConcat.<clinit>():void");
    }

    public static boolean needPlmnActConcat() {
        return mNeedConcat;
    }

    public static String getPlmnActConcat(String plmnValue, ServiceState ss) {
        if (plmnValue == null) {
            return null;
        }
        int voiceRegState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        int voiceNetworkType = ss.getVoiceNetworkType();
        int dataNetworkType = ss.getDataNetworkType();
        String plmnActValue = plmnValue;
        int networkType = 0;
        if (dataRegState == 0) {
            networkType = dataNetworkType;
        } else if (voiceRegState == 0) {
            networkType = voiceNetworkType;
        }
        Rlog.d(TAG, "plmnValue:" + plmnValue + ",voiceNetworkType:" + voiceNetworkType + ",dataNetworkType:" + dataNetworkType + ",NetworkType:" + networkType);
        String act = null;
        switch (TelephonyManager.getNetworkClass(networkType)) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                act = "";
                break;
            case HwVSimUtilsInner.STATE_EB /*2*/:
                act = "3G";
                break;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                act = "4G";
                break;
            default:
                Rlog.d(TAG, "network class unknow");
                break;
        }
        if (!(act == null || plmnValue.endsWith(act))) {
            plmnActValue = plmnValue + act;
        }
        Rlog.d(TAG, "plmnActValue:" + plmnActValue);
        return plmnActValue;
    }
}
