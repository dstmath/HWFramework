package com.android.internal.telephony.uicc;

import android.os.Message;
import com.huawei.internal.telephony.uicc.AdnRecordExt;

public interface IHwAdnRecordCacheEx {
    public static final int INVALID_EFID = -1;

    default void requestLoadAllAdnHw(int fileId, int extensionEf, Message response) {
    }

    default void updateAdnRecordId(AdnRecordExt adn, int efid, int index) {
    }

    default int extensionEfForEfHw(IAdnRecordCacheInner adnRecordCacheInner, int efid) {
        if (adnRecordCacheInner != null) {
            return adnRecordCacheInner.extensionEfForEf(efid);
        }
        return -1;
    }

    default void updateAdnBySearchHw(int efid, AdnRecordExt oldAdn, AdnRecordExt newAdn, String pin2, Message response) {
    }

    default void updateUsimAdnByIndexHw(int efid, AdnRecordExt newAdn, int originEfid, int recordIndex, String pin2, Message response) {
    }

    default int getAdnCountHw() {
        return 0;
    }

    default void setAdnCountHw(int count) {
    }

    default int[] getRecordsSizeHw() {
        return new int[0];
    }

    default void resetHw() {
    }

    default void updateUsimPhoneBookRecordHw(AdnRecordExt adnRecordExt, int efid, int index) {
    }
}
