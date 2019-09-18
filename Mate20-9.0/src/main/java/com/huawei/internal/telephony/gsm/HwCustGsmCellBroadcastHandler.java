package com.huawei.internal.telephony.gsm;

import android.os.SystemProperties;
import java.util.ArrayList;

public class HwCustGsmCellBroadcastHandler {
    protected static final int ETWS_ARRAY_MAX = 50;
    protected static final boolean IS_ETWS_DROP_LATE_PN = SystemProperties.getBoolean("ro.config.drop_latePN", false);
    protected ArrayList<String> mRcvEtwsSn = null;

    public HwCustGsmCellBroadcastHandler() {
        initSNArrayList();
    }

    public byte[] cbsPduAfterDiscardNullBlock(byte[] receivedPdu) {
        return receivedPdu;
    }

    public boolean checkETWSBeLatePN(boolean isEmergency, boolean isEtws, boolean isPrimary, int serialnum, int msgidentify) {
        return false;
    }

    private void initSNArrayList() {
        if (IS_ETWS_DROP_LATE_PN) {
            this.mRcvEtwsSn = new ArrayList<>();
            for (int index = 0; index < 50; index++) {
                this.mRcvEtwsSn.add("");
            }
        }
    }
}
