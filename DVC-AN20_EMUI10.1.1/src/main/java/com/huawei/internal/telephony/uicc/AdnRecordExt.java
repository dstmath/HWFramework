package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.AdnRecord;

public class AdnRecordExt {
    private static final int INVALID = -1;
    private AdnRecord mAdnRecord;

    public static AdnRecordExt from(Object object) {
        if (!(object instanceof AdnRecord)) {
            return null;
        }
        AdnRecordExt adnRecordEx = new AdnRecordExt();
        adnRecordEx.setAdnRecord((AdnRecord) object);
        return adnRecordEx;
    }

    public void setAdnRecord(AdnRecord adnRecord) {
        this.mAdnRecord = adnRecord;
    }

    public AdnRecord getAdnRecord() {
        return this.mAdnRecord;
    }

    public int getEfid() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getEfid();
        }
        return -1;
    }

    public int getRecordNumber() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getRecId();
        }
        return -1;
    }
}
