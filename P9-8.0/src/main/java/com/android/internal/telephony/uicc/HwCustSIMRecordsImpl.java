package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class HwCustSIMRecordsImpl extends HwCustSIMRecords {
    private static final String CUST_ROAMING_VOICEMAIL = SystemProperties.get("ro.config.cust_roamingvoicemail", "");
    private static final String LOG_TAG = "HwCustSIMRecordsImpl";
    private static final int MCCMNC = 0;
    private static final int ROAMING_NUMBER = 2;
    private static final int UNROAMING_NUMBER = 1;
    private static final int VOICEMAIL_CUST_LENGTH = 3;
    private Context mContext;
    private BroadcastReceiver mNetworkInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                TelephonyManager tm = (TelephonyManager) HwCustSIMRecordsImpl.this.mContext.getSystemService("phone");
                if (tm != null) {
                    String rplmn = tm.getNetworkOperator();
                    if (TextUtils.isEmpty(rplmn)) {
                        Rlog.i("LOG_TAG", "getRoamingVoicemail:null or empty");
                    } else if (!TextUtils.isEmpty(HwCustSIMRecordsImpl.CUST_ROAMING_VOICEMAIL)) {
                        String[] items = HwCustSIMRecordsImpl.CUST_ROAMING_VOICEMAIL.split(",");
                        Rlog.i("LOG_TAG", "getRoamingVoicemail" + rplmn);
                        if (items.length == HwCustSIMRecordsImpl.VOICEMAIL_CUST_LENGTH && rplmn.startsWith(items[0])) {
                            HwCustSIMRecordsImpl.this.voiceMailNumforSBM = items[1];
                        } else {
                            HwCustSIMRecordsImpl.this.voiceMailNumforSBM = items[2];
                        }
                        HwCustSIMRecordsImpl.this.mContext.unregisterReceiver(this);
                    }
                }
            }
        }
    };
    private String voiceMailNumforSBM;

    public HwCustSIMRecordsImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isOpenRoamingVoiceMail() {
        if (TextUtils.isEmpty(CUST_ROAMING_VOICEMAIL)) {
            return false;
        }
        return true;
    }

    public String getRoamingVoicemail() {
        return this.voiceMailNumforSBM;
    }

    public void registerRoamingState(String sVoiceMailNum) {
        this.voiceMailNumforSBM = sVoiceMailNum;
        this.mContext.registerReceiver(this.mNetworkInfoReceiver, new IntentFilter("android.intent.action.SERVICE_STATE"));
    }
}
