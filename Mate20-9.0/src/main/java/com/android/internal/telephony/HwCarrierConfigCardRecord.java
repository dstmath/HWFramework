package com.android.internal.telephony;

import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.text.TextUtils;
import huawei.cust.aidl.SpecialFile;
import java.util.ArrayList;
import java.util.List;

public class HwCarrierConfigCardRecord {
    private static final String GCF_CARD_CDMA_PLMNS = SystemProperties.get("ro.config.no_gcf_card_c_plmns", "");
    private static final String LOG_TAG = "HwCarrierConfigCardRecord";
    public static final int RULE_GID1 = 16;
    public static final int RULE_GID2 = 32;
    public static final int RULE_ICCID = 2;
    public static final int RULE_IMSI = 4;
    public static final int RULE_MCCMNC = 1;
    public static final int RULE_NONE = 0;
    public static final int RULE_SPECIAL = 64;
    public static final int RULE_SPN = 8;
    private static final String gcf_sim_mccmnc = "00101";
    private String mCImsi;
    private String mCMccmnc;
    private String mGImsi;
    private String mGMccmnc;
    private String mGid1;
    private String mGid2;
    private boolean mHasCDMAMode = false;
    private boolean mHasGsmMode = false;
    private String mIccid;
    private boolean mIsCDMAValid = false;
    private boolean mIsSingleModeCdmaCard = false;
    private int mRecordFlag = 0;
    private int mSlotId;
    private List<SpecialFile> mSpecialFiles = new ArrayList();
    private String mSpn;

    public HwCarrierConfigCardRecord(int slotId) {
        resetRecords();
        this.mSlotId = slotId;
    }

    public void setGImsi(String imsi) {
        this.mGImsi = imsi;
    }

    public void setCImsi(String imsi) {
        this.mCImsi = imsi;
    }

    public String getImsi() {
        if (isCDMAValid()) {
            return this.mCImsi;
        }
        return this.mGImsi;
    }

    public void setGMccmnc(String mccmnc) {
        this.mGMccmnc = mccmnc;
    }

    public void setCMccmnc(String mccmnc) {
        this.mCMccmnc = mccmnc;
    }

    public String getMccmnc() {
        if (isCDMAValid()) {
            return this.mCMccmnc;
        }
        return this.mGMccmnc;
    }

    public void setIccid(String iccid) {
        this.mIccid = iccid;
        this.mRecordFlag |= 2;
    }

    public String getIccid() {
        return this.mIccid;
    }

    public void setSpn(String spn) {
        this.mSpn = spn;
        this.mRecordFlag |= 8;
    }

    public String getSpn() {
        return this.mSpn;
    }

    public void setGid1(String gid1) {
        this.mGid1 = gid1;
        this.mRecordFlag |= 16;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public void setGid2(String gid2) {
        this.mGid2 = gid2;
        this.mRecordFlag |= 32;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public void setSpecialFiles(List<SpecialFile> specialFiles) {
        this.mSpecialFiles = specialFiles;
    }

    public void setSpecialFileValue(String filePath, String fileId, String value) {
        for (SpecialFile sp : this.mSpecialFiles) {
            if (sp.getFileId() != null && sp.getFileId().equals(fileId)) {
                sp.setValue(value);
            }
        }
    }

    public void setCDMAValid(boolean value) {
        this.mIsCDMAValid = value;
    }

    public void setGsmCardMode(boolean value) {
        this.mHasGsmMode = value;
    }

    public void seCDMACardMode(boolean value) {
        this.mHasCDMAMode = value;
    }

    public boolean isCDMAValid() {
        if (this.mIsSingleModeCdmaCard) {
            logd("SingleModeCdmaCard mccmnc = " + this.mCMccmnc);
            return true;
        } else if (!this.mHasGsmMode || !this.mHasCDMAMode) {
            if (!this.mIsCDMAValid && !HwTelephonyManagerInner.getDefault().isCDMASimCard(this.mSlotId) && this.mCImsi == null) {
                return false;
            }
            return true;
        } else if (this.mGMccmnc == null) {
            return false;
        } else {
            if (!gcf_sim_mccmnc.equals(this.mGMccmnc)) {
                return true;
            }
            if (TextUtils.isEmpty(GCF_CARD_CDMA_PLMNS)) {
                return false;
            }
            if (this.mCMccmnc == null || isCdmaPlmnHighPri()) {
                return true;
            }
            return false;
        }
    }

    private boolean isCdmaPlmnHighPri() {
        logd("GCF_CARD_CDMA_PLMNS = " + GCF_CARD_CDMA_PLMNS);
        if (TextUtils.isEmpty(GCF_CARD_CDMA_PLMNS)) {
            return false;
        }
        String[] plmns = GCF_CARD_CDMA_PLMNS.split(",");
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String plmn = plmns[i];
            if (TextUtils.isEmpty(plmn) || !plmn.equals(this.mCMccmnc)) {
                i++;
            } else {
                logd("isCdmaMccMncInProp: gcf card mccmnc = " + this.mCMccmnc + " need cdma PLMN match card.");
                return true;
            }
        }
        return false;
    }

    public List<SpecialFile> getSpecialFiles() {
        return this.mSpecialFiles;
    }

    public void addSpecialFlag() {
        this.mRecordFlag |= 64;
    }

    public int getRecordFlag() {
        int recordFlag = this.mRecordFlag;
        if (isCDMAValid()) {
            if (this.mCImsi != null) {
                recordFlag |= 4;
            }
            if (this.mCMccmnc != null) {
                return recordFlag | 1;
            }
            return recordFlag;
        }
        if (this.mGImsi != null) {
            recordFlag |= 4;
        }
        if (this.mGMccmnc != null) {
            return recordFlag | 1;
        }
        return recordFlag;
    }

    private final void resetRecords() {
        this.mIsCDMAValid = false;
        this.mIsSingleModeCdmaCard = false;
        this.mGImsi = null;
        this.mGMccmnc = null;
        this.mCImsi = null;
        this.mCMccmnc = null;
        this.mIccid = null;
        this.mSpn = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mRecordFlag = 0;
        this.mSpecialFiles.clear();
        this.mHasGsmMode = false;
        this.mHasCDMAMode = false;
    }

    public void dispose() {
        resetRecords();
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void setSingleModeCdmaCard(boolean value) {
        if (this.mGMccmnc == null) {
            this.mIsSingleModeCdmaCard = value;
        }
    }
}
