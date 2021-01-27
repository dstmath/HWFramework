package com.huawei.internal.telephony.cdma;

import com.android.internal.telephony.cdma.CdmaInformationRecords;

public class CdmaLineControlInfoRecEx {
    public static final int INVALID = -1;
    private CdmaInformationRecords.CdmaLineControlInfoRec mCdmaLineControlInfoRec;

    public static CdmaLineControlInfoRecEx from(Object cdmaLineControlInfoRec) {
        if (!(cdmaLineControlInfoRec instanceof CdmaInformationRecords.CdmaLineControlInfoRec)) {
            return null;
        }
        CdmaLineControlInfoRecEx cdmaLineControlInfoRecEx = new CdmaLineControlInfoRecEx();
        cdmaLineControlInfoRecEx.setCdmaLineControlInfoRec((CdmaInformationRecords.CdmaLineControlInfoRec) cdmaLineControlInfoRec);
        return cdmaLineControlInfoRecEx;
    }

    public CdmaInformationRecords.CdmaLineControlInfoRec getCdmaLineControlInfoRec() {
        return this.mCdmaLineControlInfoRec;
    }

    public void setCdmaLineControlInfoRec(CdmaInformationRecords.CdmaLineControlInfoRec cdmaLineControlInfoRec) {
        this.mCdmaLineControlInfoRec = cdmaLineControlInfoRec;
    }

    public int getlineCtrlPolarityIncluded() {
        CdmaInformationRecords.CdmaLineControlInfoRec cdmaLineControlInfoRec = this.mCdmaLineControlInfoRec;
        if (cdmaLineControlInfoRec != null) {
            return cdmaLineControlInfoRec.lineCtrlPolarityIncluded;
        }
        return -1;
    }
}
