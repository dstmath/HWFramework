package com.android.internal.telephony.gsm;

import android.telephony.Rlog;

public class SimTlv {
    int mCurDataLength;
    int mCurDataOffset;
    int mCurOffset;
    boolean mHasValidTlvObject = parseCurrentTlvObject();
    byte[] mRecord;
    int mTlvLength;
    int mTlvOffset;

    public SimTlv(byte[] record, int offset, int length) {
        this.mRecord = record;
        this.mTlvOffset = offset;
        this.mTlvLength = length;
        this.mCurOffset = offset;
    }

    public boolean nextObject() {
        if (!this.mHasValidTlvObject) {
            return false;
        }
        this.mCurOffset = this.mCurDataOffset + this.mCurDataLength;
        this.mHasValidTlvObject = parseCurrentTlvObject();
        return this.mHasValidTlvObject;
    }

    public boolean isValidObject() {
        return this.mHasValidTlvObject;
    }

    public int getTag() {
        if (this.mHasValidTlvObject) {
            return this.mRecord[this.mCurOffset] & 255;
        }
        return 0;
    }

    public byte[] getData() {
        if (!this.mHasValidTlvObject) {
            return null;
        }
        byte[] ret = new byte[this.mCurDataLength];
        System.arraycopy(this.mRecord, this.mCurDataOffset, ret, 0, this.mCurDataLength);
        return ret;
    }

    private boolean parseCurrentTlvObject() {
        try {
            if (this.mRecord[this.mCurOffset] == (byte) 0 || (this.mRecord[this.mCurOffset] & 255) == 255) {
                return false;
            }
            if ((this.mRecord[this.mCurOffset + 1] & 255) < 128) {
                this.mCurDataLength = this.mRecord[this.mCurOffset + 1] & 255;
                this.mCurDataOffset = this.mCurOffset + 2;
            } else if ((this.mRecord[this.mCurOffset + 1] & 255) != 129) {
                return false;
            } else {
                this.mCurDataLength = this.mRecord[this.mCurOffset + 2] & 255;
                this.mCurDataOffset = this.mCurOffset + 3;
            }
            if (this.mCurDataLength + this.mCurDataOffset > this.mTlvOffset + this.mTlvLength) {
                Rlog.d("SimTlv.java", "error ! current data dont match TLV");
                this.mCurDataLength = this.mTlvLength - this.mCurDataOffset;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
