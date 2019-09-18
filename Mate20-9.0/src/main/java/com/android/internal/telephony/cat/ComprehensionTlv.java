package com.android.internal.telephony.cat;

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
        ArrayList<ComprehensionTlv> items = new ArrayList<>();
        int endIndex = data.length;
        while (true) {
            if (startIndex >= endIndex) {
                break;
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        r11 = r4;
        r10 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        r4 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003c, code lost:
        r0 = r12[r2] & 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        if (r0 >= 128) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0042, code lost:
        r7 = r0;
        r3 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0049, code lost:
        if (r0 != 129) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        r5 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r3 = 255 & r12[r4];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0050, code lost:
        if (r3 < 128) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0052, code lost:
        r7 = r3;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008a, code lost:
        throw new com.android.internal.telephony.cat.ResultException(com.android.internal.telephony.cat.ResultCode.CMD_DATA_NOT_UNDERSTOOD, "length < 0x80 length=" + java.lang.Integer.toHexString(r3) + " startIndex=" + r13 + " curIndex=" + r5 + " endIndex=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008c, code lost:
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0091, code lost:
        if (r0 != 130) goto L_0x00db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009c, code lost:
        r2 = ((r12[r4] & 255) << 8) | (255 & r12[r4 + 1]);
        r3 = r4 + 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a2, code lost:
        if (r2 < 256) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a4, code lost:
        r7 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00da, code lost:
        throw new com.android.internal.telephony.cat.ResultException(com.android.internal.telephony.cat.ResultCode.CMD_DATA_NOT_UNDERSTOOD, "two byte length < 0x100 length=" + java.lang.Integer.toHexString(r2) + " startIndex=" + r13 + " curIndex=" + r3 + " endIndex=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00dd, code lost:
        if (r0 != 131) goto L_0x013d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00f0, code lost:
        r2 = (((r12[r4] & 255) << 16) | ((r12[r4 + 1] & 255) << 8)) | (255 & r12[r4 + 2]);
        r3 = r4 + 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f6, code lost:
        if (r2 < 65536) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        r4 = new com.android.internal.telephony.cat.ComprehensionTlv(r10, r11, r7, r12, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0104, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x013c, code lost:
        throw new com.android.internal.telephony.cat.ResultException(com.android.internal.telephony.cat.ResultCode.CMD_DATA_NOT_UNDERSTOOD, "three byte length < 0x10000 length=0x" + java.lang.Integer.toHexString(r2) + " startIndex=" + r13 + " curIndex=" + r3 + " endIndex=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x016d, code lost:
        throw new com.android.internal.telephony.cat.ResultException(com.android.internal.telephony.cat.ResultCode.CMD_DATA_NOT_UNDERSTOOD, "Bad length modifer=" + r0 + " startIndex=" + r13 + " curIndex=" + r4 + " endIndex=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x016f, code lost:
        r3 = r4;
     */
    public static ComprehensionTlv decode(byte[] data, int startIndex) throws ResultException {
        int i;
        int curIndex = startIndex;
        int endIndex = data.length;
        int curIndex2 = curIndex + 1;
        try {
            int temp = data[curIndex] & 255;
            if (!(temp == 0 || temp == 255)) {
                boolean cr = false;
                switch (temp) {
                    case 127:
                        int tag = ((data[curIndex2] & 255) << 8) | (data[curIndex2 + 1] & 255);
                        if ((32768 & tag) != 0) {
                            cr = true;
                        }
                        i = -32769 & tag;
                        curIndex2 += 2;
                        break;
                    case 128:
                        break;
                    default:
                        int tag2 = temp;
                        if ((tag2 & 128) != 0) {
                            cr = true;
                        }
                        i = tag2 & -129;
                        break;
                }
            }
            Rlog.d("CAT     ", "decode: unexpected first tag byte=" + Integer.toHexString(temp) + ", startIndex=" + startIndex + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
            return null;
        } catch (IndexOutOfBoundsException e) {
            int curIndex3 = curIndex2;
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, "IndexOutOfBoundsException startIndex=" + startIndex + " curIndex=" + curIndex3 + " endIndex=" + endIndex);
        }
    }
}
