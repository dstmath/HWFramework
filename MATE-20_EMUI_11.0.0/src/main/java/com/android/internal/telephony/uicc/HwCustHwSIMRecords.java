package com.android.internal.telephony.uicc;

import android.content.Context;

public class HwCustHwSIMRecords {
    Context mContext;
    IIccRecordsInner mIccRecordsInner;
    IHwIccRecordsEx mSIMRecords;

    public HwCustHwSIMRecords(IIccRecordsInner iccRecordsInner, IHwIccRecordsEx obj, Context mConText) {
        this.mIccRecordsInner = iccRecordsInner;
        this.mSIMRecords = obj;
        this.mContext = mConText;
    }

    public void setVmPriorityModeInClaro(IVoiceMailConstantsInner mVmConfig) {
    }

    public void addHwVirtualNetSpecialFiles(String filePath, String fileId, byte[] bytes, int slotId) {
    }

    public void refreshDataRoamingSettings() {
    }

    public void refreshMobileDataAlwaysOnSettings() {
    }

    public void refreshCardType() {
    }

    public boolean isHwCustDataRoamingOpenArea() {
        return false;
    }
}
