package com.android.internal.telephony.gsm;

import android.text.TextUtils;
import com.huawei.android.telephony.RlogEx;
import java.util.ArrayList;

public final class HwOplRecords {
    private static final boolean DBG = true;
    static final String TAG = "HwOplRecords";
    static final int WILD_CARD_DIGIT = 13;
    private ArrayList<OplRecord> mRecords = new ArrayList<>();

    HwOplRecords(ArrayList<byte[]> records) {
        if (records != null) {
            int listSize = records.size();
            for (int i = 0; i < listSize; i++) {
                this.mRecords.add(new OplRecord(records.get(i)));
                StringBuilder sb = new StringBuilder();
                sb.append("Record ");
                sb.append(this.mRecords.size());
                sb.append(": ");
                ArrayList<OplRecord> arrayList = this.mRecords;
                sb.append(arrayList.get(arrayList.size() - 1));
                log(sb.toString());
            }
        }
    }

    private void log(String s) {
        RlogEx.i(TAG, "[HwOplRecords EONS] " + s);
    }

    private void loge(String s) {
        RlogEx.e(TAG, "[HwOplRecords EONS] " + s);
    }

    public int size() {
        ArrayList<OplRecord> arrayList = this.mRecords;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    public int getMatchingPnnRecord(String operator, int lac, boolean useLac) {
        int[] bcchPlmn = {0, 0, 0, 0, 0, 0};
        if (TextUtils.isEmpty(operator)) {
            loge("No registered operator.");
            return 0;
        } else if (!useLac || lac != -1) {
            int length = operator.length();
            if (length == 5 || length == 6) {
                for (int i = 0; i < length; i++) {
                    bcchPlmn[i] = operator.charAt(i) - '0';
                }
                ArrayList<OplRecord> arrayList = this.mRecords;
                if (arrayList == null) {
                    return 0;
                }
                int listSize = arrayList.size();
                for (int i2 = 0; i2 < listSize; i2++) {
                    OplRecord record = this.mRecords.get(i2);
                    if (matchPlmn(record.mPlmn, bcchPlmn) && (!useLac || (record.mLac1 <= lac && lac <= record.mLac2))) {
                        return record.getPnnRecordNumber();
                    }
                }
                loge("No matching OPL record found.");
                return 0;
            }
            loge("Invalid registered operator length " + length);
            return 0;
        } else {
            loge("Invalid LAC");
            return 0;
        }
    }

    private boolean matchPlmn(int[] simPlmn, int[] bcchPlmn) {
        boolean match = true;
        for (int i = 0; i < bcchPlmn.length; i++) {
            match &= bcchPlmn[i] == simPlmn[i] || simPlmn[i] == WILD_CARD_DIGIT;
        }
        return match;
    }

    public static class OplRecord {
        private int mLac1;
        private int mLac2;
        private int[] mPlmn = {0, 0, 0, 0, 0, 0};
        private int mPnnRecordNumber;

        OplRecord(byte[] record) {
            getPlmn(record);
            getLac(record);
            this.mPnnRecordNumber = record[7] & 255;
        }

        private void getPlmn(byte[] record) {
            int[] iArr = this.mPlmn;
            iArr[0] = record[0] & 15;
            iArr[1] = (record[0] >> 4) & 15;
            iArr[2] = record[1] & 15;
            iArr[3] = record[2] & 15;
            iArr[4] = (record[2] >> 4) & 15;
            iArr[5] = (record[1] >> 4) & 15;
            if (iArr[5] == 15) {
                iArr[5] = 0;
            }
        }

        private void getLac(byte[] record) {
            this.mLac1 = ((record[3] & 255) << 8) | (record[4] & 255);
            this.mLac2 = ((record[5] & 255) << 8) | (record[6] & 255);
        }

        public int getPnnRecordNumber() {
            return this.mPnnRecordNumber;
        }

        public String toString() {
            return "PLMN=" + Integer.toHexString(this.mPlmn[0]) + Integer.toHexString(this.mPlmn[1]) + Integer.toHexString(this.mPlmn[2]) + Integer.toHexString(this.mPlmn[3]) + Integer.toHexString(this.mPlmn[4]) + Integer.toHexString(this.mPlmn[5]) + ", LAC1=" + this.mLac1 + ", LAC2=" + this.mLac2 + ", PNN Record=" + this.mPnnRecordNumber;
        }
    }
}
