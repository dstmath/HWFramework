package com.android.internal.telephony.gsm;

import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;

public final class HwPnnRecords {
    private static final boolean DBG = true;
    static final String TAG = "HwPnnRecords";
    private String mCurrentEons = null;
    private ArrayList<PnnRecord> mRecords = new ArrayList();

    public static class PnnRecord {
        static final int TAG_ADDL_INFO = 128;
        static final int TAG_FULL_NAME_IEI = 67;
        static final int TAG_SHORT_NAME_IEI = 69;
        private String mAddlInfo = null;
        private String mFullName = null;
        private String mShortName = null;

        PnnRecord(byte[] record) {
            SimTlv tlv = new SimTlv(record, 0, record.length);
            if (tlv.isValidObject() && tlv.getTag() == 67) {
                this.mFullName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
            } else {
                HwPnnRecords.log("Invalid tlv Object for Full Name, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
            }
            tlv.nextObject();
            if (tlv.isValidObject() && tlv.getTag() == 69) {
                this.mShortName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
            } else {
                HwPnnRecords.log("Invalid tlv Object for Short Name, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
            }
            tlv.nextObject();
            if (tlv.isValidObject() && tlv.getTag() == TAG_ADDL_INFO) {
                this.mAddlInfo = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
            } else {
                HwPnnRecords.log("Invalid tlv Object for Addl Info, tag= " + tlv.getTag() + ", valid=" + tlv.isValidObject());
            }
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

    HwPnnRecords(ArrayList<byte[]> records) {
        if (records != null) {
            int list_size = records.size();
            for (int i = 0; i < list_size; i++) {
                this.mRecords.add(new PnnRecord((byte[]) records.get(i)));
                log("Record " + this.mRecords.size() + ": " + this.mRecords.get(this.mRecords.size() - 1));
            }
        }
    }

    public static void log(String s) {
        Rlog.d(TAG, "[HwPnnRecords EONS] " + s);
    }

    public static void loge(String s) {
        Rlog.e(TAG, "[HwPnnRecords EONS] " + s);
    }

    public int size() {
        return this.mRecords != null ? this.mRecords.size() : 0;
    }

    public String getCurrentEons() {
        return this.mCurrentEons;
    }

    public String getNameFromPnnRecord(int recordNumber, boolean update) {
        String fullName = null;
        String ShortName = null;
        if (recordNumber < 1 || recordNumber > this.mRecords.size()) {
            loge("Invalid PNN record number " + recordNumber);
        } else {
            fullName = ((PnnRecord) this.mRecords.get(recordNumber - 1)).getFullName();
            ShortName = ((PnnRecord) this.mRecords.get(recordNumber - 1)).getShortName();
            log("getNameFromPnnRecord and  fullName is" + fullName + "and ShortName is" + ShortName);
        }
        if (update) {
            if (fullName != null && (fullName.equals("") ^ 1) != 0) {
                this.mCurrentEons = fullName;
            } else if (fullName == null || !fullName.equals("") || ShortName == null || (ShortName.equals("") ^ 1) == 0) {
                this.mCurrentEons = fullName;
            } else {
                this.mCurrentEons = ShortName;
            }
        }
        return fullName;
    }
}
