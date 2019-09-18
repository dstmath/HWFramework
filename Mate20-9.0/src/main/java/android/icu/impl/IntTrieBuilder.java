package android.icu.impl;

import android.icu.impl.Trie;
import android.icu.impl.TrieBuilder;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class IntTrieBuilder extends TrieBuilder {
    protected int[] m_data_;
    protected int m_initialValue_;
    private int m_leadUnitValue_;

    public IntTrieBuilder(IntTrieBuilder table) {
        super(table);
        this.m_data_ = new int[this.m_dataCapacity_];
        System.arraycopy(table.m_data_, 0, this.m_data_, 0, this.m_dataLength_);
        this.m_initialValue_ = table.m_initialValue_;
        this.m_leadUnitValue_ = table.m_leadUnitValue_;
    }

    public IntTrieBuilder(int[] aliasdata, int maxdatalength, int initialvalue, int leadunitvalue, boolean latin1linear) {
        if (maxdatalength < 32 || (latin1linear && maxdatalength < 1024)) {
            throw new IllegalArgumentException("Argument maxdatalength is too small");
        }
        if (aliasdata != null) {
            this.m_data_ = aliasdata;
        } else {
            this.m_data_ = new int[maxdatalength];
        }
        int j = 32;
        if (latin1linear) {
            int j2 = 32;
            int j3 = 0;
            while (true) {
                int i = j3 + 1;
                this.m_index_[j3] = j2;
                j2 += 32;
                if (i >= 8) {
                    break;
                }
                j3 = i;
            }
            j = j2;
        }
        this.m_dataLength_ = j;
        Arrays.fill(this.m_data_, 0, this.m_dataLength_, initialvalue);
        this.m_initialValue_ = initialvalue;
        this.m_leadUnitValue_ = leadunitvalue;
        this.m_dataCapacity_ = maxdatalength;
        this.m_isLatin1Linear_ = latin1linear;
        this.m_isCompacted_ = false;
    }

    public int getValue(int ch) {
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            return 0;
        }
        return this.m_data_[Math.abs(this.m_index_[ch >> 5]) + (ch & 31)];
    }

    public int getValue(int ch, boolean[] inBlockZero) {
        boolean z = true;
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            if (inBlockZero != null) {
                inBlockZero[0] = true;
            }
            return 0;
        }
        int block = this.m_index_[ch >> 5];
        if (inBlockZero != null) {
            if (block != 0) {
                z = false;
            }
            inBlockZero[0] = z;
        }
        return this.m_data_[Math.abs(block) + (ch & 31)];
    }

    public boolean setValue(int ch, int value) {
        if (this.m_isCompacted_ || ch > 1114111 || ch < 0) {
            return false;
        }
        int block = getDataBlock(ch);
        if (block < 0) {
            return false;
        }
        this.m_data_[(ch & 31) + block] = value;
        return true;
    }

    public IntTrie serialize(TrieBuilder.DataManipulate datamanipulate, Trie.DataManipulate triedatamanipulate) {
        if (datamanipulate != null) {
            if (!this.m_isCompacted_) {
                compact(false);
                fold(datamanipulate);
                compact(true);
                this.m_isCompacted_ = true;
            }
            if (this.m_dataLength_ < 262144) {
                char[] index = new char[this.m_indexLength_];
                int[] data = new int[this.m_dataLength_];
                for (int i = 0; i < this.m_indexLength_; i++) {
                    index[i] = (char) (this.m_index_[i] >>> 2);
                }
                System.arraycopy(this.m_data_, 0, data, 0, this.m_dataLength_);
                int options = 37 | 256;
                if (this.m_isLatin1Linear_) {
                    options |= 512;
                }
                IntTrie intTrie = new IntTrie(index, data, this.m_initialValue_, options, triedatamanipulate);
                return intTrie;
            }
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        throw new IllegalArgumentException("Parameters can not be null");
    }

    public int serialize(OutputStream os, boolean reduceTo16Bits, TrieBuilder.DataManipulate datamanipulate) throws IOException {
        int length;
        int length2;
        if (datamanipulate != null) {
            int i = 0;
            if (!this.m_isCompacted_) {
                compact(false);
                fold(datamanipulate);
                compact(true);
                this.m_isCompacted_ = true;
            }
            if (reduceTo16Bits) {
                length = this.m_dataLength_ + this.m_indexLength_;
            } else {
                length = this.m_dataLength_;
            }
            if (length < 262144) {
                int length3 = 16 + (this.m_indexLength_ * 2);
                if (reduceTo16Bits) {
                    length2 = length3 + (this.m_dataLength_ * 2);
                } else {
                    length2 = length3 + (4 * this.m_dataLength_);
                }
                if (os == null) {
                    return length2;
                }
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeInt(1416784229);
                int options = 37;
                if (!reduceTo16Bits) {
                    options = 37 | 256;
                }
                if (this.m_isLatin1Linear_) {
                    options |= 512;
                }
                dos.writeInt(options);
                dos.writeInt(this.m_indexLength_);
                dos.writeInt(this.m_dataLength_);
                if (reduceTo16Bits) {
                    for (int i2 = 0; i2 < this.m_indexLength_; i2++) {
                        dos.writeChar((this.m_index_[i2] + this.m_indexLength_) >>> 2);
                    }
                    while (i < this.m_dataLength_) {
                        dos.writeChar(this.m_data_[i] & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH);
                        i++;
                    }
                } else {
                    for (int i3 = 0; i3 < this.m_indexLength_; i3++) {
                        dos.writeChar(this.m_index_[i3] >>> 2);
                    }
                    while (i < this.m_dataLength_) {
                        dos.writeInt(this.m_data_[i]);
                        i++;
                    }
                }
                return length2;
            }
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        throw new IllegalArgumentException("Parameters can not be null");
    }

    public boolean setRange(int start, int limit, int value, boolean overwrite) {
        int start2;
        int i = start;
        int i2 = limit;
        int i3 = value;
        if (this.m_isCompacted_ || i < 0 || i > 1114111 || i2 < 0 || i2 > 1114112 || i > i2) {
            return false;
        }
        if (i == i2) {
            return true;
        }
        if ((i & 31) != 0) {
            int block = getDataBlock(start);
            if (block < 0) {
                return false;
            }
            int nextStart = (i + 32) & -32;
            if (nextStart <= i2) {
                fillBlock(block, i & 31, 32, i3, overwrite);
                start2 = nextStart;
            } else {
                fillBlock(block, i & 31, i2 & 31, i3, overwrite);
                return true;
            }
        } else {
            start2 = i;
        }
        int rest = i2 & 31;
        int limit2 = i2 & -32;
        int repeatBlock = 0;
        if (i3 != this.m_initialValue_) {
            repeatBlock = -1;
        }
        int start3 = start2;
        int repeatBlock2 = repeatBlock;
        while (start3 < limit2) {
            int block2 = this.m_index_[start3 >> 5];
            if (block2 > 0) {
                fillBlock(block2, 0, 32, i3, overwrite);
            } else if (this.m_data_[-block2] != i3 && (block2 == 0 || overwrite)) {
                if (repeatBlock2 >= 0) {
                    this.m_index_[start3 >> 5] = -repeatBlock2;
                } else {
                    repeatBlock2 = getDataBlock(start3);
                    if (repeatBlock2 < 0) {
                        return false;
                    }
                    this.m_index_[start3 >> 5] = -repeatBlock2;
                    fillBlock(repeatBlock2, 0, 32, i3, true);
                }
            }
            start3 += 32;
        }
        if (rest > 0) {
            int block3 = getDataBlock(start3);
            if (block3 < 0) {
                return false;
            }
            fillBlock(block3, 0, rest, i3, overwrite);
        }
        return true;
    }

    private int allocDataBlock() {
        int newBlock = this.m_dataLength_;
        int newTop = newBlock + 32;
        if (newTop > this.m_dataCapacity_) {
            return -1;
        }
        this.m_dataLength_ = newTop;
        return newBlock;
    }

    private int getDataBlock(int ch) {
        int ch2 = ch >> 5;
        int indexValue = this.m_index_[ch2];
        if (indexValue > 0) {
            return indexValue;
        }
        int newBlock = allocDataBlock();
        if (newBlock < 0) {
            return -1;
        }
        this.m_index_[ch2] = newBlock;
        System.arraycopy(this.m_data_, Math.abs(indexValue), this.m_data_, newBlock, 128);
        return newBlock;
    }

    private void compact(boolean overlap) {
        int i;
        int i2;
        if (!this.m_isCompacted_) {
            findUnusedBlocks();
            int overlapStart = 32;
            if (this.m_isLatin1Linear_) {
                overlapStart = 32 + 256;
            }
            int start = 32;
            int newStart = 32;
            while (true) {
                i = 0;
                if (start >= this.m_dataLength_) {
                    break;
                } else if (this.m_map_[start >>> 5] < 0) {
                    start += 32;
                } else {
                    if (start >= overlapStart) {
                        int[] iArr = this.m_data_;
                        if (overlap) {
                            i2 = 4;
                        } else {
                            i2 = 32;
                        }
                        int i3 = findSameDataBlock(iArr, newStart, start, i2);
                        if (i3 >= 0) {
                            this.m_map_[start >>> 5] = i3;
                            start += 32;
                        }
                    }
                    if (overlap && start >= overlapStart) {
                        i = 28;
                        while (i > 0 && !equal_int(this.m_data_, newStart - i, start, i)) {
                            i -= 4;
                        }
                    }
                    if (i > 0) {
                        this.m_map_[start >>> 5] = newStart - i;
                        start += i;
                        int i4 = 32 - i;
                        while (i4 > 0) {
                            this.m_data_[newStart] = this.m_data_[start];
                            i4--;
                            newStart++;
                            start++;
                        }
                    } else if (newStart < start) {
                        this.m_map_[start >>> 5] = newStart;
                        int i5 = 32;
                        while (i5 > 0) {
                            this.m_data_[newStart] = this.m_data_[start];
                            i5--;
                            newStart++;
                            start++;
                        }
                    } else {
                        this.m_map_[start >>> 5] = start;
                        newStart += 32;
                        start = newStart;
                    }
                }
            }
            while (true) {
                int i6 = i;
                if (i6 < this.m_indexLength_) {
                    this.m_index_[i6] = this.m_map_[Math.abs(this.m_index_[i6]) >>> 5];
                    i = i6 + 1;
                } else {
                    this.m_dataLength_ = newStart;
                    return;
                }
            }
        }
    }

    private static final int findSameDataBlock(int[] data, int dataLength, int otherBlock, int step) {
        int dataLength2 = dataLength - 32;
        int block = 0;
        while (block <= dataLength2) {
            if (equal_int(data, block, otherBlock, 32)) {
                return block;
            }
            block += step;
        }
        return -1;
    }

    private final void fold(TrieBuilder.DataManipulate manipulate) {
        int[] leadIndexes = new int[32];
        int[] index = this.m_index_;
        System.arraycopy(index, 1728, leadIndexes, 0, 32);
        int block = 0;
        if (this.m_leadUnitValue_ != this.m_initialValue_) {
            int block2 = allocDataBlock();
            if (block2 >= 0) {
                fillBlock(block2, 0, 32, this.m_leadUnitValue_, true);
                block = -block2;
            } else {
                throw new IllegalStateException("Internal error: Out of memory space");
            }
        }
        for (int c = 1728; c < 1760; c++) {
            this.m_index_[c] = block;
        }
        int indexLength = 2048;
        int c2 = 65536;
        while (c2 < 1114112) {
            if (index[c2 >> 5] != 0) {
                int c3 = c2 & -1024;
                int block3 = findSameIndexBlock(index, indexLength, c3 >> 5);
                int value = manipulate.getFoldedValue(c3, block3 + 32);
                if (value != getValue(UTF16.getLeadSurrogate(c3))) {
                    if (!setValue(UTF16.getLeadSurrogate(c3), value)) {
                        throw new ArrayIndexOutOfBoundsException("Data table overflow");
                    } else if (block3 == indexLength) {
                        System.arraycopy(index, c3 >> 5, index, indexLength, 32);
                        indexLength += 32;
                    }
                }
                c2 = c3 + 1024;
            } else {
                c2 += 32;
            }
        }
        if (indexLength < 34816) {
            System.arraycopy(index, 2048, index, 2080, indexLength - 2048);
            System.arraycopy(leadIndexes, 0, index, 2048, 32);
            this.m_indexLength_ = indexLength + 32;
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Index table overflow");
    }

    private void fillBlock(int block, int start, int limit, int value, boolean overwrite) {
        int limit2 = limit + block;
        int block2 = block + start;
        if (overwrite) {
            while (block2 < limit2) {
                this.m_data_[block2] = value;
                block2++;
            }
            return;
        }
        while (block2 < limit2) {
            if (this.m_data_[block2] == this.m_initialValue_) {
                this.m_data_[block2] = value;
            }
            block2++;
        }
    }
}
