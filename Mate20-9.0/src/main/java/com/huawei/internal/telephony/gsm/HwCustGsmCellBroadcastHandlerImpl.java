package com.huawei.internal.telephony.gsm;

import android.os.SystemProperties;
import android.telephony.Rlog;
import java.util.Arrays;

public class HwCustGsmCellBroadcastHandlerImpl extends HwCustGsmCellBroadcastHandler {
    private static final String FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG = SystemProperties.get("ro.config.hw_cbs_del_msg", "2B");
    private static String TAG = "CBS_Handler";
    private int mRcvNumSn = 0;

    public byte[] cbsPduAfterDiscardNullBlock(byte[] receivedPdu) {
        int cbsPduLength = receivedPdu.length;
        if (cbsPduLength <= 0) {
            return receivedPdu;
        }
        StringBuilder sb = new StringBuilder();
        for (int j = cbsPduLength - 1; j > 0; j--) {
            int b = receivedPdu[j] & 255;
            if (b < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (!sb.toString().equalsIgnoreCase(FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG)) {
                break;
            }
            cbsPduLength--;
            sb.delete(0, sb.length());
        }
        return Arrays.copyOf(receivedPdu, cbsPduLength);
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
}
