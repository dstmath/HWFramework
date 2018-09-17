package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;

public abstract class HwPhoneReferenceBase {
    private static final String EXTRA_LAA_STATE = "laa_state";
    private static final String LAA_STATE_CHANGE_ACTION = "com.huawei.laa.action.STATE_CHANGE_ACTION";
    private static String LOG_TAG = "HwPhoneReferenceBase";
    private GsmCdmaPhone mGsmCdmaPhone;
    int mPhoneId = this.mGsmCdmaPhone.getPhoneId();
    private String subTag = (LOG_TAG + "[" + this.mGsmCdmaPhone.getPhoneId() + "]");

    public HwPhoneReferenceBase(GsmCdmaPhone phone) {
        this.mGsmCdmaPhone = phone;
    }

    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        boolean msgHandled = true;
        switch (msg.what) {
            case 104:
                AsyncResult ar = msg.obj;
                setEccNumbers((String) ar.result);
                logd("Handle EVENT_ECC_NUM:" + ((String) ar.result));
                break;
            case 112:
                logd("EVENT_HW_LAA_STATE_CHANGED");
                onLaaStageChanged(msg);
                break;
            case 113:
                logd("EVENT_UNSOL_HW_CALL_ALT_SRV_DONE");
                handleUnsolCallAltSrv(msg);
                break;
            default:
                msgHandled = false;
                if (msg.what >= 100) {
                    msgHandled = true;
                }
                if (!msgHandled) {
                    logd("unhandle event");
                    break;
                }
                break;
        }
        return msgHandled;
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    private void setEccNumbers(String value) {
        try {
            if (!needSetEccNumbers()) {
                value = "";
            }
            if (this.mGsmCdmaPhone.getSubId() <= 0) {
                SystemProperties.set("ril.ecclist", value);
            } else {
                SystemProperties.set("ril.ecclist1", value);
            }
        } catch (RuntimeException e) {
            loge("setEccNumbers RuntimeException: " + e);
        } catch (Exception e2) {
            loge("setEccNumbers Exception: " + e2);
        }
    }

    private boolean needSetEccNumbers() {
        int i = 1;
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || (SystemProperties.getBoolean("ro.config.hw_ecc_with_sim_card", false) ^ 1) != 0) {
            return true;
        }
        boolean hasPresentCard = false;
        int simCount = TelephonyManager.getDefault().getSimCount();
        for (int i2 = 0; i2 < simCount; i2++) {
            if (TelephonyManager.getDefault().getSimState(i2) != 1) {
                hasPresentCard = true;
                break;
            }
        }
        int slotId = SubscriptionController.getInstance().getSlotIndex(this.mGsmCdmaPhone.getSubId());
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        if (!(hasPresentCard && TelephonyManager.getDefault().getSimState(slotId) == 1)) {
            i = 0;
        }
        return i ^ 1;
    }

    protected void onLaaStageChanged(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar == null || ar.exception != null) {
            logd("onLaaStageChanged:don't sendBroadcast LAA_STATE_CHANGE_ACTION");
            return;
        }
        int[] result = ar.result;
        Intent intent = new Intent(LAA_STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_LAA_STATE, result[0]);
        logd("sendBroadcast com.huawei.laa.action.STATE_CHANGE_ACTION Laa_state=" + result[0]);
        Context context = this.mGsmCdmaPhone.getContext();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void handleUnsolCallAltSrv(Message msg) {
        AsyncResult ar = msg.obj;
        logd("handleUnsolCallAltSrv");
        if (ar == null || ar.exception != null) {
            logd("handleUnsolCallAltSrv: ar or ar.exception is null");
            return;
        }
        IPhoneCallback callback = ar.userObj;
        if (callback != null) {
            try {
                callback.onCallback1(this.mPhoneId);
                logd("handleUnsolCallAltSrv,onCallback1 for subId=" + this.mPhoneId);
                return;
            } catch (RemoteException ex) {
                logd("handleUnsolCallAltSrv:onCallback1 RemoteException:" + ex);
                return;
            }
        }
        logd("handleUnsolCallAltSrv: callback is null");
    }
}
