package com.huawei.telephony;

import android.content.Context;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.VirtualNet;
import com.huawei.android.util.NoExtAPIException;

public class HuaweiTelephonyManager {
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_UG_CARD = 50;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int UNKNOWN_CARD = -1;
    private static HuaweiTelephonyManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.telephony.HuaweiTelephonyManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.telephony.HuaweiTelephonyManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.telephony.HuaweiTelephonyManager.<clinit>():void");
    }

    public static HuaweiTelephonyManager getDefault() {
        return mInstance;
    }

    public boolean isCTCdmaCardInGsmMode() {
        return false;
    }

    public boolean isCardPresent(int slotId) {
        return true;
    }

    public int getSlotIdFromSubId(int subId) {
        return subId;
    }

    public int getCardType(int slotId) {
        return UNKNOWN_CARD;
    }

    public boolean isCTNationRoamingEnable() {
        return false;
    }

    public boolean isSubActive(int subId) {
        return true;
    }

    public int getDualCardMode() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean getDSDARadioState(int subId) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getSubidFromSlotId(int slotId) {
        return slotId;
    }

    public boolean setDualCardMode(int nMode) {
        return true;
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public String getIccATR() {
        return HwTelephonyManagerInner.getDefault().getIccATR();
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        HwTelephonyManagerInner.getDefault().waitingSetDefault4GSlotDone(waiting);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        return HwTelephonyManagerInner.getDefault().isSetDefault4GSlotIdEnabled();
    }

    public void setDefault4GSlotId(int slotId, Message msg) {
        HwTelephonyManagerInner.getDefault().setDefault4GSlotId(slotId, msg);
    }

    public String getOperatorKey(Context context) {
        return VirtualNet.getOperatorKey(context);
    }

    public String getOperatorKey(Context context, int slotId) {
        return VirtualNet.getOperatorKey(context, slotId);
    }
}
