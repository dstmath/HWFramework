package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.android.os.UserHandleEx;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

/* access modifiers changed from: package-private */
public final class AI013x0x1xDecoder extends AI01weightDecoder {
    private static final int DATE_SIZE = 16;
    private static final int HEADER_SIZE = 8;
    private static final int WEIGHT_SIZE = 20;
    private final String dateCode;
    private final String firstAIdigits;

    AI013x0x1xDecoder(BitArray information, String firstAIdigits2, String dateCode2) {
        super(information);
        this.dateCode = dateCode2;
        this.firstAIdigits = firstAIdigits2;
    }

    @Override // com.huawei.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder
    public String parseInformation() throws NotFoundException {
        if (getInformation().getSize() == 84) {
            StringBuilder buf = new StringBuilder();
            encodeCompressedGtin(buf, 8);
            encodeCompressedWeight(buf, 48, 20);
            encodeCompressedDate(buf, 68);
            return buf.toString();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void encodeCompressedDate(StringBuilder buf, int currentPos) {
        int numericDate = getGeneralDecoder().extractNumericValueFromBitArray(currentPos, 16);
        if (numericDate != 38400) {
            buf.append('(');
            buf.append(this.dateCode);
            buf.append(')');
            int day = numericDate % 32;
            int numericDate2 = numericDate / 32;
            int month = (numericDate2 % 12) + 1;
            int numericDate3 = numericDate2 / 12;
            if (numericDate3 / 10 == 0) {
                buf.append('0');
            }
            buf.append(numericDate3);
            if (month / 10 == 0) {
                buf.append('0');
            }
            buf.append(month);
            if (day / 10 == 0) {
                buf.append('0');
            }
            buf.append(day);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.rss.expanded.decoders.AI01weightDecoder
    public void addWeightCode(StringBuilder buf, int weight) {
        int lastAI = weight / UserHandleEx.PER_USER_RANGE;
        buf.append('(');
        buf.append(this.firstAIdigits);
        buf.append(lastAI);
        buf.append(')');
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.rss.expanded.decoders.AI01weightDecoder
    public int checkWeight(int weight) {
        return weight % UserHandleEx.PER_USER_RANGE;
    }
}
