package com.android.internal.telephony.uicc;

import android.content.Context;

public class HwCustHwSIMRecords {
    Context mContext;
    SIMRecords mSIMRecords;

    public HwCustHwSIMRecords(SIMRecords obj, Context mConText) {
        this.mSIMRecords = obj;
        this.mContext = mConText;
    }

    public void setVmPriorityModeInClaro(VoiceMailConstants mVmConfig) {
    }

    public void addHwVirtualNetSpecialFiles(String filePath, String fileId, byte[] bytes, int slotId) {
    }

    public void refreshDataRoamingSettings() {
    }

    public void refreshMobileDataAlwaysOnSettings() {
    }

    public void refreshCardType() {
    }
}
