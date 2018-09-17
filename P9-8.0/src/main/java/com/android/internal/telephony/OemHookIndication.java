package com.android.internal.telephony;

import android.hardware.radio.deprecated.V1_0.IOemHookIndication.Stub;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;

public class OemHookIndication extends Stub {
    RIL mRil;

    public OemHookIndication(RIL ril) {
        this.mRil = ril;
    }

    public void oemHookRaw(int indicationType, ArrayList<Byte> data) {
        this.mRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        this.mRil.unsljLogvRet(1028, IccUtils.bytesToHexString(response));
        this.mRil.notifyUnsolOemHookResponse(response);
    }
}
