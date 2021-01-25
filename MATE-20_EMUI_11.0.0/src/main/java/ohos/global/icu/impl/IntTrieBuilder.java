package ohos.global.icu.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import ohos.global.icu.impl.Trie;
import ohos.global.icu.impl.TrieBuilder;
import ohos.global.icu.text.UTF16;

public class IntTrieBuilder extends TrieBuilder {
    protected int[] m_data_;
    protected int m_initialValue_;
    private int m_leadUnitValue_;

    public IntTrieBuilder(IntTrieBuilder intTrieBuilder) {
        super(intTrieBuilder);
        this.m_data_ = new int[this.m_dataCapacity_];
        System.arraycopy(intTrieBuilder.m_data_, 0, this.m_data_, 0, this.m_dataLength_);
        this.m_initialValue_ = intTrieBuilder.m_initialValue_;
        this.m_leadUnitValue_ = intTrieBuilder.m_leadUnitValue_;
    }

    public IntTrieBuilder(int[] iArr, int i, int i2, int i3, boolean z) {
        int i4 = 32;
        if (i < 32 || (z && i < 1024)) {
            throw new IllegalArgumentException("Argument maxdatalength is too small");
        }
        if (iArr != null) {
            this.m_data_ = iArr;
        } else {
            this.m_data_ = new int[i];
        }
        if (z) {
            int i5 = 0;
            int i6 = 32;
            while (true) {
                int i7 = i5 + 1;
                this.m_index_[i5] = i6;
                i6 += 32;
                if (i7 >= 8) {
                    break;
                }
                i5 = i7;
            }
            i4 = i6;
        }
        this.m_dataLength_ = i4;
        Arrays.fill(this.m_data_, 0, this.m_dataLength_, i2);
        this.m_initialValue_ = i2;
        this.m_leadUnitValue_ = i3;
        this.m_dataCapacity_ = i;
        this.m_isLatin1Linear_ = z;
        this.m_isCompacted_ = false;
    }

    public int getValue(int i) {
        if (this.m_isCompacted_ || i > 1114111 || i < 0) {
            return 0;
        }
        return this.m_data_[Math.abs(this.m_index_[i >> 5]) + (i & 31)];
    }

    public int getValue(int i, boolean[] zArr) {
        boolean z = true;
        if (this.m_isCompacted_ || i > 1114111 || i < 0) {
            if (zArr != null) {
                zArr[0] = true;
            }
            return 0;
        }
        int i2 = this.m_index_[i >> 5];
        if (zArr != null) {
            if (i2 != 0) {
                z = false;
            }
            zArr[0] = z;
        }
        return this.m_data_[Math.abs(i2) + (i & 31)];
    }

    public boolean setValue(int i, int i2) {
        int dataBlock;
        if (this.m_isCompacted_ || i > 1114111 || i < 0 || (dataBlock = getDataBlock(i)) < 0) {
            return false;
        }
        this.m_data_[dataBlock + (i & 31)] = i2;
        return true;
    }

    public IntTrie serialize(TrieBuilder.DataManipulate dataManipulate, Trie.DataManipulate dataManipulate2) {
        if (dataManipulate != null) {
            if (!this.m_isCompacted_) {
                compact(false);
                fold(dataManipulate);
                compact(true);
                this.m_isCompacted_ = true;
            }
            if (this.m_dataLength_ < 262144) {
                char[] cArr = new char[this.m_indexLength_];
                int[] iArr = new int[this.m_dataLength_];
                for (int i = 0; i < this.m_indexLength_; i++) {
                    cArr[i] = (char) (this.m_index_[i] >>> 2);
                }
                System.arraycopy(this.m_data_, 0, iArr, 0, this.m_dataLength_);
                int i2 = 293;
                if (this.m_isLatin1Linear_) {
                    i2 = 805;
                }
                return new IntTrie(cArr, iArr, this.m_initialValue_, i2, dataManipulate2);
            }
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        throw new IllegalArgumentException("Parameters can not be null");
    }

    public int serialize(OutputStream outputStream, boolean z, TrieBuilder.DataManipulate dataManipulate) throws IOException {
        int i;
        int i2;
        if (dataManipulate != null) {
            int i3 = 0;
            if (!this.m_isCompacted_) {
                compact(false);
                fold(dataManipulate);
                compact(true);
                this.m_isCompacted_ = true;
            }
            if (z) {
                i = this.m_dataLength_ + this.m_indexLength_;
            } else {
                i = this.m_dataLength_;
            }
            if (i < 262144) {
                int i4 = (this.m_indexLength_ * 2) + 16;
                if (z) {
                    i2 = this.m_dataLength_ * 2;
                } else {
                    i2 = this.m_dataLength_ * 4;
                }
                int i5 = i4 + i2;
                if (outputStream == null) {
                    return i5;
                }
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeInt(1416784229);
                int i6 = 37;
                if (!z) {
                    i6 = 293;
                }
                if (this.m_isLatin1Linear_) {
                    i6 |= 512;
                }
                dataOutputStream.writeInt(i6);
                dataOutputStream.writeInt(this.m_indexLength_);
                dataOutputStream.writeInt(this.m_dataLength_);
                if (z) {
                    for (int i7 = 0; i7 < this.m_indexLength_; i7++) {
                        dataOutputStream.writeChar((this.m_index_[i7] + this.m_indexLength_) >>> 2);
                    }
                    while (i3 < this.m_dataLength_) {
                        dataOutputStream.writeChar(this.m_data_[i3] & 65535);
                        i3++;
                    }
                } else {
                    for (int i8 = 0; i8 < this.m_indexLength_; i8++) {
                        dataOutputStream.writeChar(this.m_index_[i8] >>> 2);
                    }
                    while (i3 < this.m_dataLength_) {
                        dataOutputStream.writeInt(this.m_data_[i3]);
                        i3++;
                    }
                }
                return i5;
            }
            throw new ArrayIndexOutOfBoundsException("Data length too small");
        }
        throw new IllegalArgumentException("Parameters can not be null");
    }

    public boolean setRange(int i, int i2, int i3, boolean z) {
        int i4;
        if (this.m_isCompacted_ || i < 0 || i > 1114111 || i2 < 0 || i2 > 1114112 || i > i2) {
            return false;
        }
        if (i == i2) {
            return true;
        }
        int i5 = i & 31;
        if (i5 != 0) {
            int dataBlock = getDataBlock(i);
            if (dataBlock < 0) {
                return false;
            }
            i4 = (i + 32) & -32;
            if (i4 <= i2) {
                fillBlock(dataBlock, i5, 32, i3, z);
            } else {
                fillBlock(dataBlock, i5, i2 & 31, i3, z);
                return true;
            }
        } else {
            i4 = i;
        }
        int i6 = i2 & 31;
        int i7 = i2 & -32;
        int i8 = i3 == this.m_initialValue_ ? 0 : -1;
        while (i4 < i7) {
            int i9 = i4 >> 5;
            int i10 = this.m_index_[i9];
            if (i10 > 0) {
                fillBlock(i10, 0, 32, i3, z);
            } else if (this.m_data_[-i10] != i3 && (i10 == 0 || z)) {
                if (i8 >= 0) {
                    this.m_index_[i9] = -i8;
                } else {
                    i8 = getDataBlock(i4);
                    if (i8 < 0) {
                        return false;
                    }
                    this.m_index_[i9] = -i8;
                    fillBlock(i8, 0, 32, i3, true);
                }
            }
            i4 += 32;
        }
        if (i6 > 0) {
            int dataBlock2 = getDataBlock(i4);
            if (dataBlock2 < 0) {
                return false;
            }
            fillBlock(dataBlock2, 0, i6, i3, z);
        }
        return true;
    }

    private int allocDataBlock() {
        int i = this.m_dataLength_;
        int i2 = i + 32;
        if (i2 > this.m_dataCapacity_) {
            return -1;
        }
        this.m_dataLength_ = i2;
        return i;
    }

    private int getDataBlock(int i) {
        int i2 = i >> 5;
        int i3 = this.m_index_[i2];
        if (i3 > 0) {
            return i3;
        }
        int allocDataBlock = allocDataBlock();
        if (allocDataBlock < 0) {
            return -1;
        }
        this.m_index_[i2] = allocDataBlock;
        System.arraycopy(this.m_data_, Math.abs(i3), this.m_data_, allocDataBlock, 128);
        return allocDataBlock;
    }

    private void compact(boolean z) {
        int i;
        if (!this.m_isCompacted_) {
            findUnusedBlocks();
            int i2 = this.m_isLatin1Linear_ ? 288 : 32;
            int i3 = 32;
            int i4 = 32;
            while (true) {
                i = 0;
                if (i3 >= this.m_dataLength_) {
                    break;
                }
                int i5 = i3 >>> 5;
                if (this.m_map_[i5] >= 0) {
                    if (i3 >= i2) {
                        int findSameDataBlock = findSameDataBlock(this.m_data_, i4, i3, z ? 4 : 32);
                        if (findSameDataBlock >= 0) {
                            this.m_map_[i5] = findSameDataBlock;
                        }
                    }
                    if (z && i3 >= i2) {
                        i = 28;
                        while (i > 0 && !equal_int(this.m_data_, i4 - i, i3, i)) {
                            i -= 4;
                        }
                    }
                    if (i > 0) {
                        this.m_map_[i5] = i4 - i;
                        i3 += i;
                        int i6 = 32 - i;
                        while (i6 > 0) {
                            int[] iArr = this.m_data_;
                            iArr[i4] = iArr[i3];
                            i6--;
                            i4++;
                            i3++;
                        }
                    } else if (i4 < i3) {
                        this.m_map_[i5] = i4;
                        int i7 = i3;
                        int i8 = 32;
                        while (i8 > 0) {
                            int[] iArr2 = this.m_data_;
                            iArr2[i4] = iArr2[i7];
                            i8--;
                            i4++;
                            i7++;
                        }
                        i3 = i7;
                    } else {
                        this.m_map_[i5] = i3;
                        i4 += 32;
                        i3 = i4;
                    }
                }
                i3 += 32;
            }
            while (i < this.m_indexLength_) {
                this.m_index_[i] = this.m_map_[Math.abs(this.m_index_[i]) >>> 5];
                i++;
            }
            this.m_dataLength_ = i4;
        }
    }

    private static final int findSameDataBlock(int[] iArr, int i, int i2, int i3) {
        int i4 = i - 32;
        int i5 = 0;
        while (i5 <= i4) {
            if (equal_int(iArr, i5, i2, 32)) {
                return i5;
            }
            i5 += i3;
        }
        return -1;
    }

    private final void fold(TrieBuilder.DataManipulate dataManipulate) {
        int i;
        int[] iArr = new int[32];
        int[] iArr2 = this.m_index_;
        System.arraycopy(iArr2, 1728, iArr, 0, 32);
        if (this.m_leadUnitValue_ == this.m_initialValue_) {
            i = 0;
        } else {
            int allocDataBlock = allocDataBlock();
            if (allocDataBlock >= 0) {
                fillBlock(allocDataBlock, 0, 32, this.m_leadUnitValue_, true);
                i = -allocDataBlock;
            } else {
                throw new IllegalStateException("Internal error: Out of memory space");
            }
        }
        for (int i2 = 1728; i2 < 1760; i2++) {
            this.m_index_[i2] = i;
        }
        int i3 = 65536;
        int i4 = 2048;
        while (i3 < 1114112) {
            if (iArr2[i3 >> 5] != 0) {
                int i5 = i3 & -1024;
                int i6 = i5 >> 5;
                int findSameIndexBlock = findSameIndexBlock(iArr2, i4, i6);
                int foldedValue = dataManipulate.getFoldedValue(i5, findSameIndexBlock + 32);
                if (foldedValue != getValue(UTF16.getLeadSurrogate(i5))) {
                    if (!setValue(UTF16.getLeadSurrogate(i5), foldedValue)) {
                        throw new ArrayIndexOutOfBoundsException("Data table overflow");
                    } else if (findSameIndexBlock == i4) {
                        System.arraycopy(iArr2, i6, iArr2, i4, 32);
                        i4 += 32;
                    }
                }
                i3 = i5 + 1024;
            } else {
                i3 += 32;
            }
        }
        if (i4 < 34816) {
            System.arraycopy(iArr2, 2048, iArr2, 2080, i4 - 2048);
            System.arraycopy(iArr, 0, iArr2, 2048, 32);
            this.m_indexLength_ = i4 + 32;
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Index table overflow");
    }

    private void fillBlock(int i, int i2, int i3, int i4, boolean z) {
        int i5 = i3 + i;
        int i6 = i + i2;
        if (z) {
            while (i6 < i5) {
                this.m_data_[i6] = i4;
                i6++;
            }
            return;
        }
        while (i6 < i5) {
            int[] iArr = this.m_data_;
            if (iArr[i6] == this.m_initialValue_) {
                iArr[i6] = i4;
            }
            i6++;
        }
    }
}
