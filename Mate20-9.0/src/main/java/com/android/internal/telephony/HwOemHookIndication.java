package com.android.internal.telephony;

import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import vendor.huawei.hardware.radio.deprecated.V1_0.IOemHookIndication;

public class HwOemHookIndication extends IOemHookIndication.Stub {
    private RIL mRil;

    HwOemHookIndication(RIL ril) {
        this.mRil = ril;
    }

    public void oemHookRaw(int indicationType, ArrayList<Byte> data) {
        this.mRil.processIndication(indicationType);
        byte[] response = RIL.arrayListToPrimitiveArray(data);
        this.mRil.unsljLogvRet(1028, IccUtils.bytesToHexString(response));
        this.mRil.notifyUnsolOemHookResponse(response);
    }
}
