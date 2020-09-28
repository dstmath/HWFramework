package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.List;

public class ComprehensionTlv {
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

    @UnsupportedAppUsage
    public int getTag() {
        return this.mTag;
    }

    public boolean isComprehensionRequired() {
        return this.mCr;
    }

    @UnsupportedAppUsage
    public int getLength() {
        return this.mLength;
    }

    @UnsupportedAppUsage
    public int getValueIndex() {
        return this.mValueIndex;
    }

    @UnsupportedAppUsage
    public byte[] getRawValue() {
        return this.mRawValue;
    }

    public static List<ComprehensionTlv> decodeMany(byte[] data, int startIndex) throws ResultException {
        ArrayList<ComprehensionTlv> items = new ArrayList<>();
        int endIndex = data.length;
        while (true) {
            if (startIndex < endIndex) {
                ComprehensionTlv ctlv = decode(data, startIndex);
                if (ctlv == null) {
                    CatLog.d(LOG_TAG, "decodeMany: ctlv is null, stop decoding");
                    break;
                }
                items.add(ctlv);
                startIndex = ctlv.mValueIndex + ctlv.mLength;
            } else {
                break;
            }
        }
        return items;
    }

    /* JADX INFO: Multiple debug info for r3v4 int: [D('curIndex' int), D('tag' int)] */
    public static ComprehensionTlv decode(byte[] data, int startIndex) throws ResultException {
        boolean cr;
        int tag;
        int curIndex;
        int length;
        int endIndex = data.length;
        int curIndex2 = startIndex + 1;
        try {
            int temp = data[startIndex] & 255;
            if (!(temp == 0 || temp == 255)) {
                boolean cr2 = false;
                if (temp == 127) {
                    int tag2 = ((data[curIndex2] & 255) << 8) | (data[curIndex2 + 1] & 255);
                    if ((32768 & tag2) != 0) {
                        cr2 = true;
                    }
                    curIndex2 += 2;
                    tag = tag2 & -32769;
                    cr = cr2;
                } else if (temp != 128) {
                    if ((temp & 128) != 0) {
                        cr2 = true;
                    }
                    tag = temp & -129;
                    cr = cr2;
                }
                int tag3 = curIndex2 + 1;
                try {
                    int temp2 = data[curIndex2] & 255;
                    if (temp2 < 128) {
                        length = temp2;
                        curIndex = tag3;
                    } else if (temp2 == 129) {
                        int curIndex3 = tag3 + 1;
                        int length2 = 255 & data[tag3];
                        if (length2 >= 128) {
                            curIndex = curIndex3;
                            length = length2;
                        } else {
                            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "length < 0x80 length=" + Integer.toHexString(length2) + " startIndex=" + startIndex + " curIndex=" + curIndex3 + " endIndex=" + endIndex);
                        }
                    } else if (temp2 == 130) {
                        int length3 = ((data[tag3] & 255) << 8) | (255 & data[tag3 + 1]);
                        int curIndex4 = tag3 + 2;
                        if (length3 >= 256) {
                            length = length3;
                            curIndex = curIndex4;
                        } else {
                            try {
                                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "two byte length < 0x100 length=" + Integer.toHexString(length3) + " startIndex=" + startIndex + " curIndex=" + curIndex4 + " endIndex=" + endIndex);
                            } catch (IndexOutOfBoundsException e) {
                                curIndex2 = curIndex4;
                                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
                            }
                        }
                    } else if (temp2 == 131) {
                        int length4 = ((data[tag3] & 255) << 16) | ((data[tag3 + 1] & 255) << 8) | (255 & data[tag3 + 2]);
                        int curIndex5 = tag3 + 3;
                        if (length4 >= 65536) {
                            length = length4;
                            curIndex = curIndex5;
                        } else {
                            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "three byte length < 0x10000 length=0x" + Integer.toHexString(length4) + " startIndex=" + startIndex + " curIndex=" + curIndex5 + " endIndex=" + endIndex);
                        }
                    } else {
                        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "Bad length modifer=" + temp2 + " startIndex=" + startIndex + " curIndex=" + tag3 + " endIndex=" + endIndex);
                    }
                } catch (IndexOutOfBoundsException e2) {
                    curIndex2 = tag3;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
                }
                try {
                    return new ComprehensionTlv(tag, cr, length, data, curIndex);
                } catch (IndexOutOfBoundsException e3) {
                    curIndex2 = curIndex;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
                }
            }
            Rlog.d("CAT     ", "decode: unexpected first tag byte=" + Integer.toHexString(temp) + ", startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
            return null;
        } catch (IndexOutOfBoundsException e4) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
        }
    }
}
