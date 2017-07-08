package com.android.internal.telephony.gsm;

import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.HwSubscriptionManager;
import java.util.ArrayList;

public final class HwOplRecords {
    private static final boolean DBG = true;
    static final String TAG = "HwOplRecords";
    static final int wildCardDigit = 13;
    private ArrayList<OplRecord> mRecords;

    public static class OplRecord {
        private int mLac1;
        private int mLac2;
        private int[] mPlmn;
        private int mPnnRecordNumber;

        OplRecord(byte[] record) {
            this.mPlmn = new int[]{0, 0, 0, 0, 0, 0};
            getPlmn(record);
            getLac(record);
            this.mPnnRecordNumber = record[7] & HwSubscriptionManager.SUB_INIT_STATE;
        }

        private void getPlmn(byte[] record) {
            this.mPlmn[0] = record[0] & 15;
            this.mPlmn[1] = (record[0] >> 4) & 15;
            this.mPlmn[2] = record[1] & 15;
            this.mPlmn[3] = record[2] & 15;
            this.mPlmn[4] = (record[2] >> 4) & 15;
            this.mPlmn[5] = (record[1] >> 4) & 15;
            if (this.mPlmn[5] == 15) {
                this.mPlmn[5] = 0;
            }
        }

        private void getLac(byte[] record) {
            this.mLac1 = ((record[3] & HwSubscriptionManager.SUB_INIT_STATE) << 8) | (record[4] & HwSubscriptionManager.SUB_INIT_STATE);
            this.mLac2 = ((record[5] & HwSubscriptionManager.SUB_INIT_STATE) << 8) | (record[6] & HwSubscriptionManager.SUB_INIT_STATE);
        }

        public int getPnnRecordNumber() {
            return this.mPnnRecordNumber;
        }

        public String toString() {
            return "PLMN=" + Integer.toHexString(this.mPlmn[0]) + Integer.toHexString(this.mPlmn[1]) + Integer.toHexString(this.mPlmn[2]) + Integer.toHexString(this.mPlmn[3]) + Integer.toHexString(this.mPlmn[4]) + Integer.toHexString(this.mPlmn[5]) + ", LAC1=" + this.mLac1 + ", LAC2=" + this.mLac2 + ", PNN Record=" + this.mPnnRecordNumber;
        }
    }

    HwOplRecords(ArrayList<byte[]> records) {
        this.mRecords = new ArrayList();
        for (byte[] record : records) {
            this.mRecords.add(new OplRecord(record));
            log("Record " + this.mRecords.size() + ": " + this.mRecords.get(this.mRecords.size() - 1));
        }
    }

    private void log(String s) {
        Rlog.d(TAG, "[HwOplRecords EONS] " + s);
    }

    private void loge(String s) {
        Rlog.e(TAG, "[HwOplRecords EONS] " + s);
    }

    public int size() {
        return this.mRecords != null ? this.mRecords.size() : 0;
    }

    public int getMatchingPnnRecord(String operator, int lac, boolean useLac) {
        int[] bcchPlmn = new int[]{0, 0, 0, 0, 0, 0};
        if (TextUtils.isEmpty(operator)) {
            loge("No registered operator.");
            return 0;
        } else if (useLac && lac == -1) {
            loge("Invalid LAC");
            return 0;
        } else {
            int length = operator.length();
            if (length == 5 || length == 6) {
                for (int i = 0; i < length; i++) {
                    bcchPlmn[i] = operator.charAt(i) - 48;
                }
                for (OplRecord record : this.mRecords) {
                    if (matchPlmn(record.mPlmn, bcchPlmn) && (!useLac || (record.mLac1 <= lac && lac <= record.mLac2))) {
                        return record.getPnnRecordNumber();
                    }
                }
                loge("No matching OPL record found.");
                return 0;
            }
            loge("Invalid registered operator length " + length);
            return 0;
        }
    }

    private boolean matchPlmn(int[] simPlmn, int[] bcchPlmn) {
        boolean match = DBG;
        for (int i = 0; i < bcchPlmn.length; i++) {
            int i2 = bcchPlmn[i] != simPlmn[i] ? simPlmn[i] == wildCardDigit ? 1 : 0 : 1;
            match &= i2;
        }
        return match;
    }
}
