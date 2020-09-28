package com.android.ims;

import android.content.Context;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;

public class HwCustImsCallImpl extends HwCustImsCall {
    private static final String CALL_DROP_WIFI_BACKHAUL_CONGESTION = "Call is dropped due to Wi-Fi backhaul is congested";
    private static final String TAG = "HwCustImsCallImpl";
    private static final String WIFI_LTE_ERROR_MESSAGE = "Call is dropped due to Wi-Fi signal is degraded";
    private Context mContext;

    public HwCustImsCallImpl(Context context, ImsCallProfile profile) {
        super(context, profile);
        this.mContext = context;
    }

    private boolean isValidImsReasonInfo(ImsReasonInfo reasonInfo) {
        return (reasonInfo == null || reasonInfo.getExtraMessage() == null) ? false : true;
    }

    public boolean isCustImsReasonInfo(ImsReasonInfo reasonInfo) {
        if (!isValidImsReasonInfo(reasonInfo)) {
            loge("isCustImsReasonInfo: inValid ImsReasonInfo.");
            return false;
        } else if (reasonInfo.getExtraMessage().trim().equals(WIFI_LTE_ERROR_MESSAGE) || reasonInfo.getExtraMessage().trim().equals(CALL_DROP_WIFI_BACKHAUL_CONGESTION)) {
            return true;
        } else {
            return false;
        }
    }

    private int getCodeTypeByCustReason(String custReasonInfo) {
        if (WIFI_LTE_ERROR_MESSAGE.equals(custReasonInfo)) {
            return 1100;
        }
        if (CALL_DROP_WIFI_BACKHAUL_CONGESTION.equals(custReasonInfo)) {
            return 3001;
        }
        return 0;
    }

    public ImsReasonInfo getImsReasonInfoByCustReason(ImsReasonInfo reasonInfo) {
        String custReasonInfo = "";
        if (isValidImsReasonInfo(reasonInfo)) {
            custReasonInfo = reasonInfo.getExtraMessage().trim();
        }
        return new ImsReasonInfo(getCodeTypeByCustReason(custReasonInfo), reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
    }

    private void logd(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
