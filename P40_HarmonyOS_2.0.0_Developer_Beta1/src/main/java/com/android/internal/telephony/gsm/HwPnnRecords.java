package com.android.internal.telephony.gsm;

import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.gsm.SimTlvEx;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import java.util.ArrayList;

public final class HwPnnRecords {
    private static final boolean DBG = true;
    static final String TAG = "HwPnnRecords";
    private String mCurrentEons = null;
    private ArrayList<PnnRecord> mRecords = new ArrayList<>();

    HwPnnRecords(ArrayList<byte[]> records) {
        if (records != null) {
            int listSize = records.size();
            for (int i = 0; i < listSize; i++) {
                this.mRecords.add(new PnnRecord(records.get(i)));
                StringBuilder sb = new StringBuilder();
                sb.append("Record ");
                sb.append(this.mRecords.size());
                sb.append(": ");
                ArrayList<PnnRecord> arrayList = this.mRecords;
                sb.append(arrayList.get(arrayList.size() - 1));
                log(sb.toString());
            }
        }
    }

    public static void log(String s) {
        RlogEx.i(TAG, "[HwPnnRecords EONS] " + s);
    }

    public static void loge(String s) {
        RlogEx.e(TAG, "[HwPnnRecords EONS] " + s);
    }

    public int size() {
        ArrayList<PnnRecord> arrayList = this.mRecords;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    public String getCurrentEons() {
        return this.mCurrentEons;
    }

    public String getNameFromPnnRecord(int recordNumber, boolean update) {
        String fullName = null;
        String shortName = null;
        if (recordNumber < 1 || recordNumber > this.mRecords.size()) {
            loge("Invalid PNN record number " + recordNumber);
        } else {
            fullName = this.mRecords.get(recordNumber - 1).getFullName();
            shortName = this.mRecords.get(recordNumber - 1).getShortName();
            log("getNameFromPnnRecord and  fullName is" + fullName + "and ShortName is" + shortName);
        }
        if (update) {
            if (fullName != null && !fullName.equals(BuildConfig.FLAVOR)) {
                this.mCurrentEons = fullName;
            } else if (fullName == null || !fullName.equals(BuildConfig.FLAVOR) || shortName == null || shortName.equals(BuildConfig.FLAVOR)) {
                this.mCurrentEons = fullName;
            } else {
                this.mCurrentEons = shortName;
            }
        }
        return fullName;
    }

    public static class PnnRecord {
        static final int TAG_ADDL_INFO = 128;
        static final int TAG_FULL_NAME_IEI = 67;
        static final int TAG_SHORT_NAME_IEI = 69;
        private String mAddlInfo = null;
        private String mFullName = null;
        private String mShortName = null;

        PnnRecord(byte[] record) {
            SimTlvEx tlv = new SimTlvEx(record, 0, record.length);
            if (!tlv.isValidObject() || tlv.getTag() != 67) {
                HwPnnRecords.log("Invalid tlv Object for Full Name, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
            } else {
                this.mFullName = IccUtilsEx.networkNameToString(tlv.getData(), 0, tlv.getData().length);
            }
            tlv.nextObject();
            if (!tlv.isValidObject() || tlv.getTag() != TAG_SHORT_NAME_IEI) {
                HwPnnRecords.log("Invalid tlv Object for Short Name, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
            } else {
                this.mShortName = IccUtilsEx.networkNameToString(tlv.getData(), 0, tlv.getData().length);
            }
            tlv.nextObject();
            if (!tlv.isValidObject() || tlv.getTag() != TAG_ADDL_INFO) {
                HwPnnRecords.log("Invalid tlv Object for Addl Info, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
                return;
            }
            this.mAddlInfo = IccUtilsEx.networkNameToString(tlv.getData(), 0, tlv.getData().length);
        }

        public String getFullName() {
            return this.mFullName;
        }

        public String getShortName() {
            return this.mShortName;
        }

        public String getAddlInfo() {
            return this.mAddlInfo;
        }

        public String toString() {
            return "Full Name=" + this.mFullName + ", Short Name=" + this.mShortName + ", Additional Info=" + this.mAddlInfo;
        }
    }
}
