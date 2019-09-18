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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v5, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v12, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static BerTlv decode(byte[] data) throws ResultException {
        int curIndex;
        int totalLength;
        int endIndex = data.length;
        int length = 0;
        boolean isLengthValid = true;
        int temp = 0 + 1;
        try {
            int tag = data[0] & 255;
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
                        } catch (IndexOutOfBoundsException e) {
                            curIndex = curIndex2;
                            ResultCode resultCode2 = ResultCode.REQUIRED_VALUES_MISSING;
                            throw new ResultException(resultCode2, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
                        } catch (ResultException e2) {
                            e = e2;
                            int i = curIndex2;
                            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
                        }
                    } else {
                        ResultCode resultCode3 = ResultCode.CMD_DATA_NOT_UNDERSTOOD;
                        throw new ResultException(resultCode3, "Expected first byte to be length or a length tag and < 0x81 byte= " + Integer.toHexString(temp2) + " curIndex=" + curIndex + " endIndex=" + endIndex);
                    }
                } catch (IndexOutOfBoundsException e3) {
                    ResultCode resultCode22 = ResultCode.REQUIRED_VALUES_MISSING;
                    throw new ResultException(resultCode22, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
                } catch (ResultException e4) {
                    e = e4;
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
                }
            } else if (ComprehensionTlvTag.COMMAND_DETAILS.value() == (tag & -129)) {
                tag = 0;
                temp = 0;
            }
            if (endIndex - temp >= length) {
                List<ComprehensionTlv> ctlvs = ComprehensionTlv.decodeMany(data, temp);
                if (tag == 208) {
                    int totalLength2 = 0;
                    Iterator<ComprehensionTlv> it = ctlvs.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        int itemLength = it.next().getLength();
                        if (itemLength >= 128 && itemLength <= 255) {
                            totalLength = totalLength2 + itemLength + 3;
                        } else if (itemLength < 0 || itemLength >= 128) {
                            isLengthValid = false;
                        } else {
                            totalLength = totalLength2 + itemLength + 2;
                        }
                        totalLength2 = totalLength;
                    }
                    isLengthValid = false;
                    if (length != totalLength2) {
                        if (HwTelephonyFactory.getHwUiccManager().isContainZeros(data, length, totalLength2, temp)) {
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
        } catch (IndexOutOfBoundsException e5) {
            curIndex = temp;
            ResultCode resultCode222 = ResultCode.REQUIRED_VALUES_MISSING;
            throw new ResultException(resultCode222, "IndexOutOfBoundsException  curIndex=" + curIndex + " endIndex=" + endIndex);
        } catch (ResultException e6) {
            e = e6;
            int i2 = temp;
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD, e.explanation());
        }
    }
}
