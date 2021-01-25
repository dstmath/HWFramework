package com.android.internal.telephony.cat;

import com.android.internal.telephony.HwTelephonyFactory;
import java.util.Iterator;
import java.util.List;

class BerTlv {
    public static final int BER_EVENT_DOWNLOAD_TAG = 214;
    public static final int BER_MENU_SELECTION_TAG = 211;
    public static final int BER_PROACTIVE_COMMAND_TAG = 208;
    public static final int BER_UNKNOWN_TAG = 0;
    private List<ComprehensionTlv> mCompTlvs = null;
    private boolean mLengthValid = true;
    private int mTag = 0;

    private BerTlv(int tag, List<ComprehensionTlv> ctlvs, boolean lengthValid) {
        this.mTag = tag;
        this.mCompTlvs = ctlvs;
        this.mLengthValid = lengthValid;
    }

    public List<ComprehensionTlv> getComprehensionTlvs() {
        return this.mCompTlvs;
    }

    public int getTag() {
        return this.mTag;
    }

    public boolean isLengthValid() {
        return this.mLengthValid;
    }

    public static BerTlv decode(byte[] data) throws ResultException {
        int curIndex;
        ResultException e;
        int tag;
        int endIndex = data.length;
        int length = 0;
        boolean isLengthValid = true;
        int temp = 0 + 1;
        try {
            tag = data[0] & 255;
            if (tag == 208) {
                curIndex = temp + 1;
                try {
                    int temp2 = data[temp] & 255;
                    if (temp2 < 128) {
                        length = temp2;
                        temp = curIndex;
                    } else if (temp2 == 129) {
                        int curIndex2 = curIndex + 1;
                        try {
                            int temp3 = data[curIndex] & 255;
                            if (temp3 >= 128) {
                                length = temp3;
                                temp = curIndex2;
                            } else {
                                ResultCode resultCode = ResultCode.CMD_DATA_NOT_UNDERSTOOD;
                                throw new ResultException(resultCode, "length < 0x80 length=" + Integer.toHexString(0) + " curIndex=" + curIndex2 + " endIndex=" + endIndex);
                            }
                        } catch (IndexOutOfBoundsException e2) {
                            curIndex = curIndex2;
                            ResultCode resultCode2 = ResultCode.REQUIRED_VALUES_MISSING;
                            throw new ResultException(resultCode2, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
                        } catch (ResultException e3) {
                            e = e3;
                            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
                        }
                    } else {
                        ResultCode resultCode3 = ResultCode.CMD_DATA_NOT_UNDERSTOOD;
                        throw new ResultException(resultCode3, "Expected first byte to be length or a length tag and < 0x81 byte= " + Integer.toHexString(temp2) + " curIndex=" + curIndex + " endIndex=" + endIndex);
                    }
                } catch (IndexOutOfBoundsException e4) {
                    ResultCode resultCode22 = ResultCode.REQUIRED_VALUES_MISSING;
                    throw new ResultException(resultCode22, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
                } catch (ResultException e5) {
                    e = e5;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
                }
            } else if (ComprehensionTlvTag.COMMAND_DETAILS.value() == (tag & -129)) {
                tag = 0;
                temp = 0;
            }
        } catch (IndexOutOfBoundsException e6) {
            curIndex = temp;
            ResultCode resultCode222 = ResultCode.REQUIRED_VALUES_MISSING;
            throw new ResultException(resultCode222, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
        } catch (ResultException e7) {
            e = e7;
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
        }
        if (endIndex - temp >= length) {
            List<ComprehensionTlv> ctlvs = ComprehensionTlv.decodeMany(data, temp);
            if (tag == 208) {
                int totalLength = 0;
                Iterator<ComprehensionTlv> it = ctlvs.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    int itemLength = it.next().getLength();
                    if (itemLength >= 128 && itemLength <= 255) {
                        totalLength += itemLength + 3;
                    } else if (itemLength < 0 || itemLength >= 128) {
                        break;
                    } else {
                        totalLength += itemLength + 2;
                    }
                }
                isLengthValid = false;
                if (length != totalLength) {
                    if (HwTelephonyFactory.getHwUiccManager().isContainZeros(data, length, totalLength, temp)) {
                        isLengthValid = true;
                    } else {
                        isLengthValid = false;
                    }
                }
            }
            return new BerTlv(tag, ctlvs, isLengthValid);
        }
        ResultCode resultCode4 = ResultCode.CMD_DATA_NOT_UNDERSTOOD;
        throw new ResultException(resultCode4, "Command had extra data endIndex=" + endIndex + " curIndex=" + temp + " length=" + length);
    }
}
