package ohos.global.icu.impl;

import java.util.Arrays;

public class TrieBuilder {
    protected static final int BMP_INDEX_LENGTH_ = 2048;
    public static final int DATA_BLOCK_LENGTH = 32;
    protected static final int DATA_GRANULARITY_ = 4;
    protected static final int INDEX_SHIFT_ = 2;
    protected static final int MASK_ = 31;
    private static final int MAX_BUILD_TIME_DATA_LENGTH_ = 1115168;
    protected static final int MAX_DATA_LENGTH_ = 262144;
    protected static final int MAX_INDEX_LENGTH_ = 34816;
    protected static final int OPTIONS_DATA_IS_32_BIT_ = 256;
    protected static final int OPTIONS_INDEX_SHIFT_ = 4;
    protected static final int OPTIONS_LATIN1_IS_LINEAR_ = 512;
    protected static final int SHIFT_ = 5;
    protected static final int SURROGATE_BLOCK_COUNT_ = 32;
    protected int m_dataCapacity_;
    protected int m_dataLength_;
    protected int m_indexLength_;
    protected int[] m_index_;
    protected boolean m_isCompacted_;
    protected boolean m_isLatin1Linear_;
    protected int[] m_map_;

    public interface DataManipulate {
        int getFoldedValue(int i, int i2);
    }

    public boolean isInZeroBlock(int i) {
        if (this.m_isCompacted_ || i > 1114111 || i < 0 || this.m_index_[i >> 5] == 0) {
            return true;
        }
        return false;
    }

    protected TrieBuilder() {
        this.m_index_ = new int[MAX_INDEX_LENGTH_];
        this.m_map_ = new int[34849];
        this.m_isLatin1Linear_ = false;
        this.m_isCompacted_ = false;
        this.m_indexLength_ = MAX_INDEX_LENGTH_;
    }

    protected TrieBuilder(TrieBuilder trieBuilder) {
        this.m_index_ = new int[MAX_INDEX_LENGTH_];
        this.m_indexLength_ = trieBuilder.m_indexLength_;
        System.arraycopy(trieBuilder.m_index_, 0, this.m_index_, 0, this.m_indexLength_);
        this.m_dataCapacity_ = trieBuilder.m_dataCapacity_;
        this.m_dataLength_ = trieBuilder.m_dataLength_;
        this.m_map_ = new int[trieBuilder.m_map_.length];
        int[] iArr = trieBuilder.m_map_;
        int[] iArr2 = this.m_map_;
        System.arraycopy(iArr, 0, iArr2, 0, iArr2.length);
        this.m_isLatin1Linear_ = trieBuilder.m_isLatin1Linear_;
        this.m_isCompacted_ = trieBuilder.m_isCompacted_;
    }

    protected static final boolean equal_int(int[] iArr, int i, int i2, int i3) {
        while (i3 > 0 && iArr[i] == iArr[i2]) {
            i++;
            i2++;
            i3--;
        }
        return i3 == 0;
    }

    /* access modifiers changed from: protected */
    public void findUnusedBlocks() {
        Arrays.fill(this.m_map_, 255);
        for (int i = 0; i < this.m_indexLength_; i++) {
            this.m_map_[Math.abs(this.m_index_[i]) >> 5] = 0;
        }
        this.m_map_[0] = 0;
    }

    protected static final int findSameIndexBlock(int[] iArr, int i, int i2) {
        for (int i3 = 2048; i3 < i; i3 += 32) {
            if (equal_int(iArr, i3, i2, 32)) {
                return i3;
            }
        }
        return i;
    }
}
