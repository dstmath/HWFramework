package com.huawei.zxing.oned;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.common.BitArray;

final class UPCEANExtensionSupport {
    private static final int[] EXTENSION_START_PATTERN = new int[]{1, 1, 2};
    private final UPCEANExtension5Support fiveSupport = new UPCEANExtension5Support();
    private final UPCEANExtension2Support twoSupport = new UPCEANExtension2Support();

    UPCEANExtensionSupport() {
    }

    Result decodeRow(int rowNumber, BitArray row, int rowOffset) throws NotFoundException {
        int[] extensionStartRange = UPCEANReader.findGuardPattern(row, rowOffset, false, EXTENSION_START_PATTERN);
        try {
            return this.fiveSupport.decodeRow(rowNumber, row, extensionStartRange);
        } catch (ReaderException e) {
            return this.twoSupport.decodeRow(rowNumber, row, extensionStartRange);
        }
    }
}
