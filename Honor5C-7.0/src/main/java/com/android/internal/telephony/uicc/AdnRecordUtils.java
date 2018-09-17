package com.android.internal.telephony.uicc;

public class AdnRecordUtils {
    public static int getEfid(AdnRecord adnRecord) {
        if (adnRecord != null) {
            return adnRecord.mEfid;
        }
        return -1;
    }

    public static int getRecordNumber(AdnRecord adnRecord) {
        if (adnRecord != null) {
            return adnRecord.mRecordNumber;
        }
        return -1;
    }
}
