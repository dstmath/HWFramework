package ohos.global.icu.text;

import java.io.IOException;
import java.nio.ByteBuffer;
import ohos.global.icu.impl.Assert;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.util.UResourceBundle;

final class DictionaryData {
    private static final int DATA_FORMAT_ID = 1147757428;
    public static final int IX_COUNT = 8;
    public static final int IX_RESERVED1_OFFSET = 1;
    public static final int IX_RESERVED2_OFFSET = 2;
    public static final int IX_RESERVED6 = 6;
    public static final int IX_RESERVED7 = 7;
    public static final int IX_STRING_TRIE_OFFSET = 0;
    public static final int IX_TOTAL_SIZE = 3;
    public static final int IX_TRANSFORM = 5;
    public static final int IX_TRIE_TYPE = 4;
    public static final int TRANSFORM_NONE = 0;
    public static final int TRANSFORM_OFFSET_MASK = 2097151;
    public static final int TRANSFORM_TYPE_MASK = 2130706432;
    public static final int TRANSFORM_TYPE_OFFSET = 16777216;
    public static final int TRIE_HAS_VALUES = 8;
    public static final int TRIE_TYPE_BYTES = 0;
    public static final int TRIE_TYPE_MASK = 7;
    public static final int TRIE_TYPE_UCHARS = 1;

    private DictionaryData() {
    }

    public static DictionaryMatcher loadDictionaryFor(String str) throws IOException {
        ByteBuffer requiredData = ICUBinary.getRequiredData("brkitr/" + UResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME).getStringWithFallback("dictionaries/" + str));
        ICUBinary.readHeader(requiredData, DATA_FORMAT_ID, null);
        int[] iArr = new int[8];
        boolean z = false;
        for (int i = 0; i < 8; i++) {
            iArr[i] = requiredData.getInt();
        }
        int i2 = iArr[0];
        Assert.assrt(i2 >= 32);
        if (i2 > 32) {
            ICUBinary.skipBytes(requiredData, i2 - 32);
        }
        int i3 = iArr[4] & 7;
        int i4 = iArr[3] - i2;
        if (i3 == 0) {
            int i5 = iArr[5];
            byte[] bArr = new byte[i4];
            requiredData.get(bArr);
            return new BytesDictionaryMatcher(bArr, i5);
        } else if (i3 != 1) {
            return null;
        } else {
            if (i4 % 2 == 0) {
                z = true;
            }
            Assert.assrt(z);
            return new CharsDictionaryMatcher(ICUBinary.getString(requiredData, i4 / 2, i4 & 1));
        }
    }
}
