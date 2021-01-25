package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.internal.telephony.RILConstantsEx;
import com.huawei.systemmanager.power.HwHistoryItem;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public abstract class AbstractExpandedDecoder {
    private final GeneralAppIdDecoder generalDecoder;
    private final BitArray information;

    public abstract String parseInformation() throws NotFoundException, FormatException;

    AbstractExpandedDecoder(BitArray information2) {
        this.information = information2;
        this.generalDecoder = new GeneralAppIdDecoder(information2);
    }

    /* access modifiers changed from: protected */
    public final BitArray getInformation() {
        return this.information;
    }

    /* access modifiers changed from: protected */
    public final GeneralAppIdDecoder getGeneralDecoder() {
        return this.generalDecoder;
    }

    public static AbstractExpandedDecoder createDecoder(BitArray information2) {
        if (information2.get(1)) {
            return new AI01AndOtherAIs(information2);
        }
        if (!information2.get(2)) {
            return new AnyAIDecoder(information2);
        }
        int fourBitEncodationMethod = GeneralAppIdDecoder.extractNumericValueFromBitArray(information2, 1, 4);
        if (fourBitEncodationMethod == 4) {
            return new AI013103decoder(information2);
        }
        if (fourBitEncodationMethod == 5) {
            return new AI01320xDecoder(information2);
        }
        int fiveBitEncodationMethod = GeneralAppIdDecoder.extractNumericValueFromBitArray(information2, 1, 5);
        if (fiveBitEncodationMethod == 12) {
            return new AI01392xDecoder(information2);
        }
        if (fiveBitEncodationMethod == 13) {
            return new AI01393xDecoder(information2);
        }
        switch (GeneralAppIdDecoder.extractNumericValueFromBitArray(information2, 1, 7)) {
            case HwHistoryItem.STATE_PHONE_SIGNAL_STRENGTH_MASK /* 56 */:
                return new AI013x0x1xDecoder(information2, "310", "11");
            case 57:
                return new AI013x0x1xDecoder(information2, "320", "11");
            case 58:
                return new AI013x0x1xDecoder(information2, "310", "13");
            case 59:
                return new AI013x0x1xDecoder(information2, "320", "13");
            case 60:
                return new AI013x0x1xDecoder(information2, "310", "15");
            case RILConstantsEx.NETWORK_MODE_CDMA_HDR_GSM_WCDMA_LTEFDD /* 61 */:
                return new AI013x0x1xDecoder(information2, "320", "15");
            case 62:
                return new AI013x0x1xDecoder(information2, "310", "17");
            case RILConstantsEx.NETWORK_MODE_CDMA_LTE_WCDMA_GSM /* 63 */:
                return new AI013x0x1xDecoder(information2, "320", "17");
            default:
                throw new IllegalStateException("unknown decoder: " + information2);
        }
    }
}
