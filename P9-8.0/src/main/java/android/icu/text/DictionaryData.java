package android.icu.text;

import android.icu.impl.Assert;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.nio.ByteBuffer;

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

    public static DictionaryMatcher loadDictionaryFor(String dictType) throws IOException {
        ByteBuffer bytes = ICUBinary.getRequiredData("brkitr/" + ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME)).getStringWithFallback("dictionaries/" + dictType));
        ICUBinary.readHeader(bytes, DATA_FORMAT_ID, null);
        int[] indexes = new int[8];
        for (int i = 0; i < 8; i++) {
            indexes[i] = bytes.getInt();
        }
        int offset = indexes[0];
        Assert.assrt(offset >= 32);
        if (offset > 32) {
            ICUBinary.skipBytes(bytes, offset - 32);
        }
        int trieType = indexes[4] & 7;
        int totalSize = indexes[3] - offset;
        if (trieType == 0) {
            int transform = indexes[5];
            byte[] data = new byte[totalSize];
            bytes.get(data);
            return new BytesDictionaryMatcher(data, transform);
        } else if (trieType != 1) {
            return null;
        } else {
            Assert.assrt(totalSize % 2 == 0);
            return new CharsDictionaryMatcher(ICUBinary.getString(bytes, totalSize / 2, totalSize & 1));
        }
    }
}
