package android.icu.impl;

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

    public boolean isInZeroBlock(int ch) {
        boolean z = true;
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            return true;
        }
        if (this.m_index_[ch >> 5] != 0) {
            z = false;
        }
        return z;
    }

    protected TrieBuilder() {
        this.m_index_ = new int[MAX_INDEX_LENGTH_];
        this.m_map_ = new int[34849];
        this.m_isLatin1Linear_ = false;
        this.m_isCompacted_ = false;
        this.m_indexLength_ = MAX_INDEX_LENGTH_;
    }

    protected TrieBuilder(TrieBuilder table) {
        this.m_index_ = new int[MAX_INDEX_LENGTH_];
        this.m_indexLength_ = table.m_indexLength_;
        System.arraycopy(table.m_index_, 0, this.m_index_, 0, this.m_indexLength_);
        this.m_dataCapacity_ = table.m_dataCapacity_;
        this.m_dataLength_ = table.m_dataLength_;
        this.m_map_ = new int[table.m_map_.length];
        System.arraycopy(table.m_map_, 0, this.m_map_, 0, this.m_map_.length);
        this.m_isLatin1Linear_ = table.m_isLatin1Linear_;
        this.m_isCompacted_ = table.m_isCompacted_;
    }

    protected static final boolean equal_int(int[] array, int start1, int start2, int length) {
        while (length > 0 && array[start1] == array[start2]) {
            start1++;
            start2++;
            length--;
        }
        if (length == 0) {
            return true;
        }
        return false;
    }

    protected void findUnusedBlocks() {
        Arrays.fill(this.m_map_, 255);
        for (int i = 0; i < this.m_indexLength_; i++) {
            this.m_map_[Math.abs(this.m_index_[i]) >> 5] = 0;
        }
        this.m_map_[0] = 0;
    }

    protected static final int findSameIndexBlock(int[] index, int indexLength, int otherBlock) {
        for (int block = 2048; block < indexLength; block += 32) {
            if (equal_int(index, block, otherBlock, 32)) {
                return block;
            }
        }
        return indexLength;
    }
}
