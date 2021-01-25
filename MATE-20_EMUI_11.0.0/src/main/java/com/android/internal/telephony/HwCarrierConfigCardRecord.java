package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import huawei.cust.aidl.SpecialFile;
import java.util.ArrayList;
import java.util.List;

public class HwCarrierConfigCardRecord {
    private static final String GCF_CARD_CDMA_PLMNS = SystemPropertiesEx.get("ro.config.no_gcf_card_c_plmns", BuildConfig.FLAVOR);
    private static final boolean HW_DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
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

    private static void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
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

    public String getIccid() {
        return this.mIccid;
    }

    public void setIccid(String iccid) {
        this.mIccid = iccid;
        this.mRecordFlag |= 2;
    }

    public String getSpn() {
        return this.mSpn;
    }

    public void setSpn(String spn) {
        this.mSpn = spn;
        this.mRecordFlag |= 8;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public void setGid1(String gid1) {
        this.mGid1 = gid1;
        this.mRecordFlag |= 16;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public void setGid2(String gid2) {
        this.mGid2 = gid2;
        this.mRecordFlag |= 32;
    }

    public void setSpecialFileValue(String filePath, String fileId, String value) {
        for (SpecialFile sp : this.mSpecialFiles) {
            if (!(value == null || sp.getFileId() == null || !sp.getFileId().equals(fileId))) {
                sp.setValue(value);
            }
        }
    }

    public void setGsmCardMode(boolean value) {
        this.mHasGsmMode = value;
    }

    public void seCDMACardMode(boolean value) {
        this.mHasCDMAMode = value;
    }

    public boolean isCDMAValid() {
        if (this.mIsSingleModeCdmaCard) {
            StringBuilder sb = new StringBuilder();
            sb.append("SingleModeCdmaCard mccmnc = ");
            sb.append(HW_DBG ? this.mCMccmnc : "***");
            logd(sb.toString());
            return true;
        } else if (this.mHasGsmMode && this.mHasCDMAMode) {
            String str = this.mGMccmnc;
            if (str == null) {
                return false;
            }
            if (!gcf_sim_mccmnc.equals(str)) {
                return true;
            }
            if (TextUtils.isEmpty(GCF_CARD_CDMA_PLMNS)) {
                return false;
            }
            if (this.mCMccmnc == null || isCdmaPlmnHighPri()) {
                return true;
            }
            return false;
        } else if (!this.mIsCDMAValid && !HwTelephonyManagerInner.getDefault().isCDMASimCard(this.mSlotId) && this.mCImsi == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setCDMAValid(boolean value) {
        this.mIsCDMAValid = value;
    }

    private boolean isCdmaPlmnHighPri() {
        StringBuilder sb = new StringBuilder();
        sb.append("GCF_CARD_CDMA_PLMNS = ");
        String str = "***";
        sb.append(HW_DBG ? GCF_CARD_CDMA_PLMNS : str);
        logd(sb.toString());
        if (TextUtils.isEmpty(GCF_CARD_CDMA_PLMNS)) {
            return false;
        }
        String[] plmns = GCF_CARD_CDMA_PLMNS.split(",");
        for (String plmn : plmns) {
            if (!TextUtils.isEmpty(plmn) && plmn.equals(this.mCMccmnc)) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("isCdmaMccMncInProp: gcf card mccmnc = ");
                if (HW_DBG) {
                    str = this.mCMccmnc;
                }
                sb2.append(str);
                sb2.append(" need cdma PLMN match card.");
                logd(sb2.toString());
                return true;
            }
        }
        return false;
    }

    public List<SpecialFile> getSpecialFiles() {
        return this.mSpecialFiles;
    }

    public void setSpecialFiles(List<SpecialFile> specialFiles) {
        this.mSpecialFiles = specialFiles;
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

    public void setSingleModeCdmaCard(boolean value) {
        if (this.mGMccmnc == null) {
            this.mIsSingleModeCdmaCard = value;
        }
    }
}
