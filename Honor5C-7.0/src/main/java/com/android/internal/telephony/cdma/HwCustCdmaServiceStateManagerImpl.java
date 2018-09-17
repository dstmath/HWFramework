package com.android.internal.telephony.cdma;

import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.GsmCdmaPhone;

public class HwCustCdmaServiceStateManagerImpl extends HwCustCdmaServiceStateManager {
    private static final int HOME_ERI_INDEX = 1;
    private static final boolean SET_PLMN_TO_ERITEXT = false;
    private static final String TAG = "HwCustCdmaServiceStateManagerImpl";
    private static final String USE_WHEN_ERI_TEXT_EMPTY = "Roaming Indicator On";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwCustCdmaServiceStateManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwCustCdmaServiceStateManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwCustCdmaServiceStateManagerImpl.<clinit>():void");
    }

    public String setEriBasedPlmn(GsmCdmaPhone phone, String actualPlmnValue) {
        if (SET_PLMN_TO_ERITEXT && getCombinedRegState(phone) == 0) {
            int iconIndex = phone.getCdmaEriIconIndex();
            if (iconIndex != HOME_ERI_INDEX) {
                actualPlmnValue = phone.getCdmaEriText();
                if (TextUtils.isEmpty(actualPlmnValue)) {
                    actualPlmnValue = USE_WHEN_ERI_TEXT_EMPTY;
                }
            }
            Log.d(TAG, "setEriBasedPlmn -> eriIndex :" + iconIndex + " ,modifiedPlmnValue :" + actualPlmnValue);
        }
        return actualPlmnValue;
    }

    private int getCombinedRegState(GsmCdmaPhone phone) {
        ServiceState sS = phone.getServiceState();
        int regState = sS.getVoiceRegState();
        int dataRegState = sS.getDataRegState();
        if (regState == HOME_ERI_INDEX && dataRegState == 0) {
            return dataRegState;
        }
        return regState;
    }
}
