package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.uicc.SIMRecords;

public class HwCustPhoneManager {
    public boolean isStringHuaweiIgnoreCode(GsmCdmaPhone phone, String dialString) {
        return false;
    }

    public void initParamByPlmn(SIMRecords mSIMRecords, Context mContext) {
    }

    public String getVirtualNetEccWihCard(int slotId) {
        return null;
    }

    public boolean changeMMItoUSSD(GsmCdmaPhone phone, String poundString) {
        return false;
    }

    public String getVirtualNetEccNoCard(int slotId) {
        return null;
    }

    public boolean isSupportEccFormVirtualNet() {
        return false;
    }

    public boolean isSupportOrangeApn(Phone phone) {
        return false;
    }

    public void addSpecialAPN(Phone phone) {
    }
}
