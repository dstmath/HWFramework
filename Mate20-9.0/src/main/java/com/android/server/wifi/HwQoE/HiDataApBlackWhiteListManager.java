package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.HwQoE.HiDataTracfficInfo;

public class HiDataApBlackWhiteListManager {
    public static final String TAG = "HiDATA_WhiteListManager";
    private static HiDataApBlackWhiteListManager mHiDataApBlackWhiteListManager;
    private Context mContext;
    private String mCurrentDefaultDataImsi;
    private HwQoEQualityManager mHwQoEQualityManager = HwQoEQualityManager.getInstance(this.mContext);
    private TelephonyManager mTelephonyManager = ((TelephonyManager) this.mContext.getSystemService("phone"));

    public static HiDataApBlackWhiteListManager createInstance(Context context) {
        if (mHiDataApBlackWhiteListManager == null) {
            mHiDataApBlackWhiteListManager = new HiDataApBlackWhiteListManager(context);
        }
        return mHiDataApBlackWhiteListManager;
    }

    private HiDataApBlackWhiteListManager(Context context) {
        this.mContext = context;
    }

    public synchronized void addHandoverWhiteList(String ssid, int apAuthType, int appType) {
        if (!TextUtils.isEmpty(ssid)) {
            logD("addHandoverWhiteList,ssid: " + ssid);
            HiDataTracfficInfo.HiDataApInfo hiDataApInfo = new HiDataTracfficInfo.HiDataApInfo(ssid, apAuthType, 1, appType, 0);
            this.mHwQoEQualityManager.addOrUpdateAPRcd(hiDataApInfo);
        }
    }

    public synchronized void addHandoverBlackList(String ssid, int apAuthType, int appType) {
        if (!TextUtils.isEmpty(ssid)) {
            HiDataTracfficInfo.HiDataApInfo hiDataApInfo = this.mHwQoEQualityManager.queryAPUseType(ssid, apAuthType, appType);
            if (2 <= hiDataApInfo.mBlackCount) {
                logD("addHandoverBlackList,max blackCount ,ignore  " + ssid);
                return;
            }
            HiDataTracfficInfo.HiDataApInfo hiDataApInfo2 = new HiDataTracfficInfo.HiDataApInfo(ssid, apAuthType, 2, appType, hiDataApInfo.mBlackCount + 1);
            HiDataTracfficInfo.HiDataApInfo hiDataApInfo3 = hiDataApInfo2;
            logD("addHandoverBlackList,ssid: " + ssid + " , count : " + hiDataApInfo3.mBlackCount);
            this.mHwQoEQualityManager.addOrUpdateAPRcd(hiDataApInfo3);
        }
    }

    private int getBlackListCounter(String ssid, int apAuthType, int appType) {
        if (TextUtils.isEmpty(ssid)) {
            return 0;
        }
        int counter = 0;
        HiDataTracfficInfo.HiDataApInfo hiDataApInfo = this.mHwQoEQualityManager.queryAPUseType(ssid, apAuthType, appType);
        if (2 == hiDataApInfo.mApType) {
            counter = hiDataApInfo.mBlackCount;
        }
        logD("getBlackListCounter,ssid: " + ssid + ", counter :" + counter);
        return counter;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003b, code lost:
        return false;
     */
    public synchronized boolean isInTheBlackList(String ssid, int apAuthType, int appType, int usertype) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        int counter = getBlackListCounter(ssid, apAuthType, appType);
        logD("isInTheBlackList,usertype: " + usertype + ", black counter: " + counter);
        if (counter <= 0) {
            return false;
        }
        if (1 == usertype) {
            return true;
        }
        if (usertype == 0 && counter >= 2) {
            return true;
        }
    }

    public synchronized boolean isInTheWhiteList(String ssid, int apAuthType, int appType) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        if (1 != this.mHwQoEQualityManager.queryAPUseType(ssid, apAuthType, appType).mApType) {
            return false;
        }
        logD(ssid + " in the WhiteList ");
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0071, code lost:
        return 0;
     */
    public synchronized int getApBlackType(String ssid, int apAuthType, int appType, int usertype) {
        logD("getApBlackType,ssid: " + ssid + ", apAuthType: " + apAuthType + ", appType: " + appType + " ,usertype:" + usertype);
        if (TextUtils.isEmpty(ssid)) {
            return 0;
        }
        HiDataTracfficInfo.HiDataApInfo hiDataApInfo = this.mHwQoEQualityManager.queryAPUseType(ssid, apAuthType, appType);
        logD("hiDataApInfo:" + hiDataApInfo.toString());
        if (1 == hiDataApInfo.mApType) {
            return 1;
        }
        if (2 == hiDataApInfo.mApType) {
            if (1 == usertype && hiDataApInfo.mBlackCount >= 1) {
                return 2;
            }
            if (usertype == 0) {
                if (hiDataApInfo.mBlackCount >= 2) {
                    return 2;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        return;
     */
    public synchronized void updateHoldWiFiCounter(String ssid, int apAuthType, int appType) {
        if (!TextUtils.isEmpty(ssid)) {
            logD("updateHoldWiFiCounter,ssid: " + ssid);
            if (!TextUtils.isEmpty(getCurrentDefaultDataImsi())) {
                if (!isInTheWhiteList(ssid, apAuthType, appType)) {
                    if (getBlackListCounter(ssid, apAuthType, appType) > 0) {
                        minusHandoverBlackCounter(ssid, apAuthType, appType);
                    }
                }
            }
        }
    }

    private void minusHandoverBlackCounter(String ssid, int apAuthType, int appType) {
        if (!TextUtils.isEmpty(ssid)) {
            HiDataTracfficInfo.HiDataApInfo hiDataApInfo = this.mHwQoEQualityManager.queryAPUseType(ssid, apAuthType, appType);
            if (2 == hiDataApInfo.mApType && hiDataApInfo.mBlackCount > 0) {
                hiDataApInfo.mBlackCount--;
                logD("minusHandoverBlackCounter,ssid: " + ssid + ", count: " + hiDataApInfo.mBlackCount);
                this.mHwQoEQualityManager.addOrUpdateAPRcd(hiDataApInfo);
            }
        }
    }

    private String getCurrentDefaultDataImsi() {
        this.mCurrentDefaultDataImsi = this.mTelephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        return this.mCurrentDefaultDataImsi;
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }
}
