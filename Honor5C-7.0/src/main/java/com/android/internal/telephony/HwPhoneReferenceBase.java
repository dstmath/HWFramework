package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;

public abstract class HwPhoneReferenceBase {
    private static String LOG_TAG;
    private GsmCdmaPhone mGsmCdmaPhone;
    private String subTag;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwPhoneReferenceBase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwPhoneReferenceBase.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwPhoneReferenceBase.<clinit>():void");
    }

    public HwPhoneReferenceBase(GsmCdmaPhone phone) {
        this.mGsmCdmaPhone = phone;
        this.subTag = LOG_TAG + "[" + this.mGsmCdmaPhone.getPhoneId() + "]";
    }

    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        boolean msgHandled = true;
        switch (msg.what) {
            case 104:
                AsyncResult ar = msg.obj;
                setEccNumbers((String) ar.result);
                logd("Handle EVENT_ECC_NUM:" + ((String) ar.result));
                break;
            default:
                msgHandled = false;
                if (msg.what >= 100) {
                    msgHandled = true;
                }
                if (!msgHandled) {
                    logd("unhandle event");
                    break;
                }
                break;
        }
        return msgHandled;
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    private void setEccNumbers(String value) {
        try {
            if (!needSetEccNumbers()) {
                value = "";
            }
            if (this.mGsmCdmaPhone.getSubId() <= 0) {
                SystemProperties.set("ril.ecclist", value);
            } else {
                SystemProperties.set("ril.ecclist1", value);
            }
        } catch (RuntimeException e) {
            loge("setEccNumbers RuntimeException: " + e);
        } catch (Exception e2) {
            loge("setEccNumbers Exception: " + e2);
        }
    }

    private boolean needSetEccNumbers() {
        boolean z = false;
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || !SystemProperties.getBoolean("ro.config.hw_ecc_with_sim_card", false)) {
            return true;
        }
        boolean hasPresentCard = false;
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            if (TelephonyManager.getDefault().getSimState(i) != 1) {
                hasPresentCard = true;
                break;
            }
        }
        int slotId = SubscriptionController.getInstance().getSlotId(this.mGsmCdmaPhone.getSubId());
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        if (!(hasPresentCard && TelephonyManager.getDefault().getSimState(slotId) == 1)) {
            z = true;
        }
        return z;
    }
}
