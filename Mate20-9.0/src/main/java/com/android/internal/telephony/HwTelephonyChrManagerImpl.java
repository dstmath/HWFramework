package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class HwTelephonyChrManagerImpl implements HwTelephonyChrManager {
    public static final String CHR_ACTION = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    public static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    public static final String CHR_DATA = "chr_data";
    public static final boolean DBG = true;
    public static final String FAULT_ID = "fault_id";
    public static final String LOG_TAG = "HwTelephonyChrManagerImpl";
    public static final String MODULE_ID = "module_id";
    private static final int MODULE_ID_TELEPHONY = 26000;
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final String SUB_ID = "sub_id";
    private static final int TELEPHONY_FAULT_ID = 26000;
    private static HwTelephonyChrManager mInstance = new HwTelephonyChrManagerImpl();
    public Context mContext;

    public static HwTelephonyChrManager getDefault() {
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public void sendTelephonyChrBroadcast(Bundle data, int subId) {
        chrLog("sendTelephonyChrBroadcast subId " + subId);
        if (data == null || !isValidSubId(subId)) {
            chrLog("data is null or invalid subid");
            return;
        }
        Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
        intent.putExtra("module_id", 26000);
        intent.putExtra("fault_id", 26000);
        intent.putExtra(SUB_ID, subId);
        intent.putExtra("chr_data", data);
        intent.setPackage("com.huawei.android.chr");
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    public void sendTelephonyChrBroadcast(Bundle data) {
        sendTelephonyChrBroadcast(data, SubscriptionManager.getDefaultDataSubscriptionId());
    }

    private boolean isValidSubId(int subId) {
        if (subId < 0 || subId > SIM_NUM) {
            return false;
        }
        return true;
    }

    private void chrLog(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
