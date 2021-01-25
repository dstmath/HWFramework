package com.huawei.internal.telephony.gsm;

import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import huawei.cust.HwCfgFilePolicy;
import java.util.Arrays;

public class HwCustGsmCellBroadcastHandlerImpl extends HwCustGsmCellBroadcastHandler {
    private static final String FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG = SystemProperties.get("ro.config.hw_cbs_del_msg", "2B");
    private static final boolean IS_CBSPDU_HANDLER_NULL_MSG = SystemProperties.getBoolean("ro.config.cbs_del_2B", false);
    private static String TAG = "CBS_Handler";
    protected Phone mPhone;
    private int mRcvNumSn = 0;

    public HwCustGsmCellBroadcastHandlerImpl(Phone phone) {
        super(phone);
        this.mPhone = phone;
    }

    private boolean shouldDeleteCBSNullMsg() {
        Phone phone = this.mPhone;
        if (phone == null) {
            return false;
        }
        Boolean deleteCbs = (Boolean) HwCfgFilePolicy.getValue("cbs_del_2b_switch", phone.getPhoneId(), Boolean.class);
        if (deleteCbs != null) {
            return deleteCbs.booleanValue();
        }
        return IS_CBSPDU_HANDLER_NULL_MSG;
    }

    public byte[] cbsPduAfterDiscardNullBlock(byte[] receivedPdu) {
        if (shouldDeleteCBSNullMsg()) {
            log("cbsPduAfterDiscardNullBlock:Delete CBS 2B info when Null msg.");
            int cbsPduLength = receivedPdu.length;
            if (cbsPduLength > 0) {
                StringBuilder sb = new StringBuilder();
                for (int j = cbsPduLength - 1; j > 0; j--) {
                    int b = receivedPdu[j] & 255;
                    if (b < 16) {
                        sb.append('0');
                    }
                    sb.append(Integer.toHexString(b));
                    if (!FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG.equalsIgnoreCase(sb.toString())) {
                        break;
                    }
                    cbsPduLength--;
                    sb.delete(0, sb.length());
                }
                return Arrays.copyOf(receivedPdu, cbsPduLength);
            }
        }
        return receivedPdu;
    }

    public boolean checkETWSBeLatePN(boolean isEmergency, boolean isEtws, boolean isPrimary, int serialnum, int msgidentify) {
        Rlog.w(TAG, "EmergencyMsg:" + isEmergency + ", EtwsMsg:" + isEtws + ", isPn:" + isPrimary);
        if (!IS_ETWS_DROP_LATE_PN || this.mRcvEtwsSn == null || !isEmergency || !isEtws) {
            return false;
        }
        String keyitem = "<" + Integer.toString(serialnum) + "|" + Integer.toString(msgidentify) + ">";
        boolean snexisted = this.mRcvEtwsSn.contains(keyitem);
        Rlog.w(TAG, "snexisted = " + snexisted);
        if (!isPrimary) {
            this.mRcvEtwsSn.set(this.mRcvNumSn % 50, keyitem);
            this.mRcvNumSn++;
            return false;
        } else if (snexisted) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        String tag = TAG;
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.d(tag, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String tag = TAG;
        if (this.mPhone != null) {
            tag = tag + "[SUB" + this.mPhone.getPhoneId() + "]";
        }
        Rlog.e(tag, s);
    }
}
