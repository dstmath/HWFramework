package com.android.internal.telephony.cat;

import android.telephony.Rlog;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.WapPushManagerParams;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import java.util.ArrayList;
import java.util.List;

class ComprehensionTlv {
    private static final String LOG_TAG = "ComprehensionTlv";
    private boolean mCr;
    private int mLength;
    private byte[] mRawValue;
    private int mTag;
    private int mValueIndex;

    protected ComprehensionTlv(int tag, boolean cr, int length, byte[] data, int valueIndex) {
        this.mTag = tag;
        this.mCr = cr;
        this.mLength = length;
        this.mValueIndex = valueIndex;
        this.mRawValue = data;
    }

    public int getTag() {
        return this.mTag;
    }

    public boolean isComprehensionRequired() {
        return this.mCr;
    }

    public int getLength() {
        return this.mLength;
    }

    public int getValueIndex() {
        return this.mValueIndex;
    }

    public byte[] getRawValue() {
        return this.mRawValue;
    }

    public static List<ComprehensionTlv> decodeMany(byte[] data, int startIndex) throws ResultException {
        ArrayList<ComprehensionTlv> items = new ArrayList();
        int endIndex = data.length;
        while (startIndex < endIndex) {
            ComprehensionTlv ctlv = decode(data, startIndex);
            if (ctlv == null) {
                CatLog.d(LOG_TAG, "decodeMany: ctlv is null, stop decoding");
                break;
            }
            items.add(ctlv);
            startIndex = ctlv.mValueIndex + ctlv.mLength;
        }
        return items;
    }

    public static ComprehensionTlv decode(byte[] data, int startIndex) throws ResultException {
        int curIndex = startIndex;
        int endIndex = data.length;
        curIndex = startIndex + 1;
        try {
            int tag;
            boolean cr;
            int curIndex2;
            int length;
            int temp = data[startIndex] & PduHeaders.STORE_STATUS_ERROR_END;
            switch (temp) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                case PduPart.P_Q /*128*/:
                case PduHeaders.STORE_STATUS_ERROR_END /*255*/:
                    Rlog.d("CAT     ", "decode: unexpected first tag byte=" + Integer.toHexString(temp) + ", startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
                    return null;
                case CallFailCause.INTERWORKING_UNSPECIFIED /*127*/:
                    tag = ((data[curIndex] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[curIndex + 1] & PduHeaders.STORE_STATUS_ERROR_END);
                    cr = (WapPushManagerParams.FURTHER_PROCESSING & tag) != 0;
                    tag &= -32769;
                    curIndex2 = curIndex + 2;
                    break;
                default:
                    tag = temp;
                    cr = (temp & PduPart.P_Q) != 0;
                    tag = temp & -129;
                    curIndex2 = curIndex;
                    break;
            }
            curIndex = curIndex2 + 1;
            temp = data[curIndex2] & PduHeaders.STORE_STATUS_ERROR_END;
            if (temp < PduPart.P_Q) {
                length = temp;
            } else if (temp == PduPart.P_DISPOSITION_ATTACHMENT) {
                curIndex2 = curIndex + 1;
                try {
                    length = data[curIndex] & PduHeaders.STORE_STATUS_ERROR_END;
                    if (length < PduPart.P_Q) {
                        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "length < 0x80 length=" + Integer.toHexString(length) + " startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
                    }
                    curIndex = curIndex2;
                } catch (IndexOutOfBoundsException e) {
                    curIndex = curIndex2;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
                }
            } else if (temp == PduPart.P_LEVEL) {
                length = ((data[curIndex] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[curIndex + 1] & PduHeaders.STORE_STATUS_ERROR_END);
                curIndex += 2;
                if (length < 256) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "two byte length < 0x100 length=" + Integer.toHexString(length) + " startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
                }
            } else if (temp == PduPart.P_TYPE) {
                length = (((data[curIndex] & PduHeaders.STORE_STATUS_ERROR_END) << 16) | ((data[curIndex + 1] & PduHeaders.STORE_STATUS_ERROR_END) << 8)) | (data[curIndex + 2] & PduHeaders.STORE_STATUS_ERROR_END);
                curIndex += 3;
                if (length < 65536) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "three byte length < 0x10000 length=0x" + Integer.toHexString(length) + " startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
                }
            } else {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "Bad length modifer=" + temp + " startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
            }
            return new ComprehensionTlv(tag, cr, length, data, curIndex);
        } catch (IndexOutOfBoundsException e2) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex + " endIndex=" + endIndex);
        }
    }
}
