package com.huawei.internal.telephony.gsm;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import java.util.ArrayList;
import java.util.Arrays;

public class HwCustGsmCellBroadcastHandler {
    private static final String CBS_DISCARD_STRANGCHAR_INT = "cbs_discard_strangechar_int";
    private static final int ETWS_ARRAY_MAX = 50;
    private static final String FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG = SystemProperties.get("ro.config.hw_cbs_del_msg", "2B");
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_CBSPDU_HANDLER_NULL_MSG = SystemProperties.getBoolean("ro.config.cbs_del_2B", false);
    private static final boolean IS_ETWS_DROP_LATE_PN = SystemProperties.getBoolean("ro.config.drop_latePN", false);
    private static String TAG = "CBS_Handler";
    private ArrayList<String> mRcvEtwsSn = null;
    private int mRcvNumSn = 0;

    public HwCustGsmCellBroadcastHandler() {
        initSNArrayList();
    }

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

    /* JADX WARNING: Missing block: B:4:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkETWSBeLatePN(boolean isEmergency, boolean isEtws, boolean isPrimary, int serialnum, int msgidentify) {
        Rlog.w(TAG, "EmergencyMsg:" + isEmergency + ", EtwsMsg:" + isEtws + ", isPn:" + isPrimary);
        if (!IS_ETWS_DROP_LATE_PN || this.mRcvEtwsSn == null || !isEmergency || (isEtws ^ 1) != 0) {
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

    private void initSNArrayList() {
        if (IS_ETWS_DROP_LATE_PN) {
            this.mRcvEtwsSn = new ArrayList();
            for (int index = 0; index < 50; index++) {
                this.mRcvEtwsSn.add("");
            }
        }
    }

    public boolean isDiscardStrangeChar(Phone phone) {
        int allowDiscardInt = -1;
        Context mContext = phone != null ? phone.getContext() : null;
        CarrierConfigManager configLoader = mContext != null ? (CarrierConfigManager) mContext.getSystemService("carrier_config") : null;
        PersistableBundle b = configLoader != null ? configLoader.getConfigForSubId(phone.getSubId()) : null;
        if (b != null) {
            allowDiscardInt = b.getInt(CBS_DISCARD_STRANGCHAR_INT, -1);
        }
        Rlog.d(TAG, "allowDiscardIntStrangeChar: allowDiscardInt=" + allowDiscardInt);
        if (1 == allowDiscardInt) {
            return true;
        }
        if (allowDiscardInt == 0) {
            return false;
        }
        return IS_CBSPDU_HANDLER_NULL_MSG;
    }
}
