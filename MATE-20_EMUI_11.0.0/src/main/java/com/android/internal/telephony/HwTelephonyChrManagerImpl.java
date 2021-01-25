package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.RILRequestEx;

public class HwTelephonyChrManagerImpl extends DefaultHwTelephonyChrManager {
    public static final String CHR_ACTION = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    public static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    public static final String CHR_DATA = "chr_data";
    public static final boolean DBG = true;
    private static final int EVENT_RIL_REQUEST_TIMEOUT_PHONE0 = 0;
    private static final int EVENT_RIL_REQUEST_TIMEOUT_PHONE1 = 1;
    private static final int EVENT_RIL_REQUEST_TIMEOUT_PHONE2 = 2;
    public static final String FAULT_ID = "fault_id";
    public static final String LOG_TAG = "HwTelephonyChrManagerImpl";
    public static final String MODULE_ID = "module_id";
    private static final int MODULE_ID_TELEPHONY = 26000;
    private static final int PHONE_ID0 = 0;
    private static final int PHONE_ID1 = 1;
    private static final int PHONE_ID2 = 2;
    public static final String SUB_ID = "sub_id";
    private static final int TELEPHONY_FAULT_ID = 26000;
    private static final int TELEPHONY_RIL_FAULT_ID = 26001;
    private static HwTelephonyChrManager mInstance = new HwTelephonyChrManagerImpl();
    public Context mContext;
    private int[] mFirstSeril = new int[(this.mSimNum + 1)];
    private MyHandler mHandler = new MyHandler(Looper.getMainLooper());
    private final int mSimNum = TelephonyManagerEx.getDefault().getPhoneCount();

    private HwTelephonyChrManagerImpl() {
    }

    public static HwTelephonyChrManager getDefault() {
        return mInstance;
    }

    private int getRequestTimeoutEventId(int phoneId) {
        if (phoneId == 0) {
            return 0;
        }
        if (phoneId == 1) {
            return 1;
        }
        if (phoneId == 2) {
            return 2;
        }
        RlogEx.e(LOG_TAG, "getRequestTimeoutEventId, Error, phoneId: " + phoneId);
        return 0;
    }

    public void checkFirstRequest(int requestListSize, RILRequestEx rilRequest, int phoneId) {
        if (!isValidSubId(phoneId)) {
            RlogEx.e(LOG_TAG, "checkFirstRequest, Error, phoneId: " + phoneId);
        } else if (requestListSize <= 0) {
            stopRilRequestBlockTimer(phoneId);
        } else if (rilRequest != null) {
            int serial = rilRequest.getSerial();
            chrLog("checkFirstRequest serial: " + serial + " phoneId: " + phoneId);
            if (serial != this.mFirstSeril[phoneId]) {
                stopRilRequestBlockTimer(phoneId);
                this.mFirstSeril[phoneId] = serial;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(getRequestTimeoutEventId(phoneId), phoneId, requestListSize, rilRequest), 60000);
            }
        }
    }

    public void stopRilRequestBlockTimer(int phoneId) {
        if (!isValidSubId(phoneId)) {
            RlogEx.e(LOG_TAG, "stopRilRequestBlockTimer, Error, phoneId: " + phoneId);
            return;
        }
        int eventId = getRequestTimeoutEventId(phoneId);
        if (this.mHandler.hasMessages(eventId)) {
            this.mHandler.removeMessages(eventId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportCommandBlock(RILRequestEx rilRequest, int phoneId, int requestSize) {
        synchronized (rilRequest) {
            chrLog("reportCommandBlock rilRequest: " + rilRequest.getSerial() + " phoneId: " + phoneId + ", size = " + requestSize);
            Bundle bundle = new Bundle();
            bundle.putInt("RilBlockCommand", rilRequest.getRequest());
            bundle.putInt("RilBlockListSize", requestSize);
            sendTelephonyRilChrBroadcast(bundle, phoneId);
        }
    }

    private void sendTelephonyRilChrBroadcast(Bundle data, int subId) {
        chrLog("sendTelephonyChrBroadcast subId " + subId);
        if (data == null || !isValidSubId(subId)) {
            chrLog("data is null or invalid subid");
            return;
        }
        Intent intent = new Intent(CHR_ACTION);
        intent.putExtra(MODULE_ID, 26000);
        intent.putExtra(FAULT_ID, TELEPHONY_RIL_FAULT_ID);
        intent.putExtra(SUB_ID, subId);
        intent.putExtra(CHR_DATA, data);
        intent.setPackage("com.huawei.android.chr");
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
        }
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
        Intent intent = new Intent(CHR_ACTION);
        intent.putExtra(MODULE_ID, 26000);
        intent.putExtra(FAULT_ID, 26000);
        intent.putExtra(SUB_ID, subId);
        intent.putExtra(CHR_DATA, data);
        intent.setPackage("com.huawei.android.chr");
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
        }
    }

    public void sendTelephonyChrBroadcast(Bundle data) {
        sendTelephonyChrBroadcast(data, SubscriptionManager.getDefaultDataSubscriptionId());
    }

    private boolean isValidSubId(int subId) {
        if (subId < 0 || subId > this.mSimNum) {
            return false;
        }
        return true;
    }

    private void chrLog(String s) {
        RlogEx.d(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0 || i == 1 || i == 2) {
                HwTelephonyChrManagerImpl.this.reportCommandBlock((RILRequestEx) msg.obj, msg.arg1, msg.arg2);
            }
        }
    }
}
