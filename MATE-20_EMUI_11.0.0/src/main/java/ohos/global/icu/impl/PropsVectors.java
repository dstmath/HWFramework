package ohos.global.icu.impl;

import java.util.Arrays;
import java.util.Comparator;
import ohos.global.icu.impl.Trie;
import ohos.global.icu.impl.TrieBuilder;

public class PropsVectors {
    public static final int ERROR_VALUE_CP = 1114113;
    public static final int FIRST_SPECIAL_CP = 1114112;
    public static final int INITIAL_ROWS = 4096;
    public static final int INITIAL_VALUE_CP = 1114112;
    public static final int MAX_CP = 1114113;
    public static final int MAX_ROWS = 1114114;
    public static final int MEDIUM_ROWS = 65536;
    private int columns;
    private boolean isCompacted;
    private int maxRows;
    private int prevRow;
    private int rows;
    private int[] v;

    public interface CompactHandler {
        void setRowIndexForErrorValue(int i);

        void setRowIndexForInitialValue(int i);

        void setRowIndexForRange(int i, int i2, int i3);

        void startRealValues(int i);
    }

    private boolean areElementsSame(int i, int[] iArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            if (this.v[i + i4] != iArr[i2 + i4]) {
                return false;
            }
        }
        return true;
    }

    private int findRow(int i) {
        int i2 = this.prevRow;
        int i3 = this.columns;
        int i4 = i2 * i3;
        int[] iArr = this.v;
        int i5 = 0;
        if (i >= iArr[i4]) {
            if (i < iArr[i4 + 1]) {
                return i4;
            }
            int i6 = i4 + i3;
            if (i < iArr[i6 + 1]) {
                this.prevRow = i2 + 1;
                return i6;
            }
            int i7 = i6 + i3;
            int i8 = i7 + 1;
            if (i < iArr[i8]) {
                this.prevRow = i2 + 2;
                return i7;
            } else if (i - iArr[i8] < 10) {
                this.prevRow = i2 + 2;
                do {
                    this.prevRow++;
                    i7 += this.columns;
                } while (i >= this.v[i7 + 1]);
                return i7;
            }
        } else if (i < iArr[1]) {
            this.prevRow = 0;
            return 0;
        }
        int i9 = this.rows;
        while (i5 < i9 - 1) {
            int i10 = (i5 + i9) / 2;
            int i11 = this.columns * i10;
            int[] iArr2 = this.v;
            if (i < iArr2[i11]) {
                i9 = i10;
            } else if (i < iArr2[i11 + 1]) {
                this.prevRow = i10;
                return i11;
            } else {
                i5 = i10;
            }
        }
        this.prevRow = i5;
        return i5 * this.columns;
    }

    public PropsVectors(int i) {
        if (i >= 1) {
            this.columns = i + 2;
            int i2 = this.columns;
            this.v = new int[(i2 * 4096)];
            this.maxRows = 4096;
            this.rows = 3;
            this.prevRow = 0;
            this.isCompacted = false;
            int[] iArr = this.v;
            iArr[0] = 0;
            int i3 = 1114112;
            iArr[1] = 1114112;
            while (i3 <= 1114113) {
                int[] iArr2 = this.v;
                iArr2[i2] = i3;
                i3++;
                iArr2[i2 + 1] = i3;
                i2 += this.columns;
            }
            return;
        }
        throw new IllegalArgumentException("numOfColumns need to be no less than 1; but it is " + i);
    }

    public void setValue(int i, int i2, int i3, int i4, int i5) {
        int i6;
        if (i < 0 || i > i2 || i2 > 1114113 || i3 < 0 || i3 >= this.columns - 2) {
            throw new IllegalArgumentException();
        } else if (!this.isCompacted) {
            int i7 = i2 + 1;
            int i8 = i3 + 2;
            int i9 = i4 & i5;
            int findRow = findRow(i);
            int findRow2 = findRow(i2);
            int[] iArr = this.v;
            int i10 = 1;
            boolean z = (i == iArr[findRow] || i9 == (iArr[findRow + i8] & i5)) ? false : true;
            int[] iArr2 = this.v;
            boolean z2 = (i7 == iArr2[findRow2 + 1] || i9 == (iArr2[findRow2 + i8] & i5)) ? false : true;
            if (z || z2) {
                if (!z) {
                    i10 = 0;
                }
                if (z2) {
                    i10++;
                }
                int i11 = this.rows + i10;
                int i12 = this.maxRows;
                if (i11 > i12) {
                    int i13 = MAX_ROWS;
                    if (i12 < 65536) {
                        i13 = 65536;
                    } else if (i12 >= 1114114) {
                        throw new IndexOutOfBoundsException("MAX_ROWS exceeded! Increase it to a higher valuein the implementation");
                    }
                    int i14 = this.columns;
                    int[] iArr3 = new int[(i13 * i14)];
                    i6 = i9;
                    System.arraycopy(this.v, 0, iArr3, 0, this.rows * i14);
                    this.v = iArr3;
                    this.maxRows = i13;
                } else {
                    i6 = i9;
                }
                int i15 = this.rows;
                int i16 = this.columns;
                int i17 = (i15 * i16) - (findRow2 + i16);
                if (i17 > 0) {
                    int[] iArr4 = this.v;
                    System.arraycopy(iArr4, findRow2 + i16, iArr4, ((i10 + 1) * i16) + findRow2, i17);
                }
                this.rows += i10;
                if (z) {
                    int i18 = this.columns;
                    int[] iArr5 = this.v;
                    System.arraycopy(iArr5, findRow, iArr5, i18 + findRow, (findRow2 - findRow) + i18);
                    int i19 = this.columns;
                    findRow2 += i19;
                    int[] iArr6 = this.v;
                    iArr6[findRow + i19] = i;
                    iArr6[findRow + 1] = i;
                    findRow += i19;
                }
                if (z2) {
                    int[] iArr7 = this.v;
                    int i20 = this.columns;
                    System.arraycopy(iArr7, findRow2, iArr7, findRow2 + i20, i20);
                    int[] iArr8 = this.v;
                    iArr8[this.columns + findRow2] = i7;
                    iArr8[findRow2 + 1] = i7;
                }
            } else {
                i6 = i9;
            }
            this.prevRow = findRow2 / this.columns;
            int i21 = findRow + i8;
            int i22 = findRow2 + i8;
            int i23 = ~i5;
            while (true) {
                int[] iArr9 = this.v;
                iArr9[i21] = (iArr9[i21] & i23) | i6;
                if (i21 != i22) {
                    i21 += this.columns;
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalStateException("Shouldn't be called aftercompact()!");
        }
    }

    public int getValue(int i, int i2) {
        if (this.isCompacted || i < 0 || i > 1114113 || i2 < 0 || i2 >= this.columns - 2) {
            return 0;
        }
        return this.v[findRow(i) + 2 + i2];
    }

    public int[] getRow(int i) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (i < 0 || i > this.rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        } else {
            int i2 = this.columns;
            int[] iArr = new int[(i2 - 2)];
            System.arraycopy(this.v, (i * i2) + 2, iArr, 0, i2 - 2);
            return iArr;
        }
    }

    public int getRowStart(int i) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (i >= 0 && i <= this.rows) {
            return this.v[i * this.columns];
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public int getRowEnd(int i) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (i >= 0 && i <= this.rows) {
            return this.v[(i * this.columns) + 1] - 1;
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public void compact(CompactHandler compactHandler) {
        if (!this.isCompacted) {
            this.isCompacted = true;
            int i = this.columns - 2;
            Integer[] numArr = new Integer[this.rows];
            for (int i2 = 0; i2 < this.rows; i2++) {
                numArr[i2] = Integer.valueOf(this.columns * i2);
            }
            Arrays.sort(numArr, new Comparator<Integer>() {
                /* class ohos.global.icu.impl.PropsVectors.AnonymousClass1 */

                public int compare(Integer num, Integer num2) {
                    int intValue = num.intValue();
                    int intValue2 = num2.intValue();
                    int i = PropsVectors.this.columns;
                    int i2 = 2;
                    do {
                        int i3 = intValue + i2;
                        int i4 = intValue2 + i2;
                        if (PropsVectors.this.v[i3] == PropsVectors.this.v[i4]) {
                            i2++;
                            if (i2 == PropsVectors.this.columns) {
                                i2 = 0;
                            }
                            i--;
                        } else if (PropsVectors.this.v[i3] < PropsVectors.this.v[i4]) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } while (i > 0);
                    return 0;
                }
            });
            int i3 = -i;
            int i4 = i3;
            for (int i5 = 0; i5 < this.rows; i5++) {
                int i6 = this.v[numArr[i5].intValue()];
                if (i4 < 0 || !areElementsSame(numArr[i5].intValue() + 2, this.v, numArr[i5 - 1].intValue() + 2, i)) {
                    i4 += i;
                }
                if (i6 == 1114112) {
                    compactHandler.setRowIndexForInitialValue(i4);
                } else if (i6 == 1114113) {
                    compactHandler.setRowIndexForErrorValue(i4);
                }
            }
            int i7 = i4 + i;
            compactHandler.startRealValues(i7);
            int[] iArr = new int[i7];
            for (int i8 = 0; i8 < this.rows; i8++) {
                int i9 = this.v[numArr[i8].intValue()];
                int i10 = this.v[numArr[i8].intValue() + 1];
                if (i3 < 0 || !areElementsSame(numArr[i8].intValue() + 2, iArr, i3, i)) {
                    i3 += i;
                    System.arraycopy(this.v, numArr[i8].intValue() + 2, iArr, i3, i);
                }
                if (i9 < 1114112) {
                    compactHandler.setRowIndexForRange(i9, i10 - 1, i3);
                }
            }
            this.v = iArr;
            this.rows = (i3 / i) + 1;
        }
    }

    public int[] getCompactedArray() {
        if (this.isCompacted) {
            return this.v;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public int getCompactedRows() {
        if (this.isCompacted) {
            return this.rows;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public int getCompactedColumns() {
        if (this.isCompacted) {
            return this.columns - 2;
        }
        throw new IllegalStateException("Illegal Invocation of the method before compact()");
    }

    public IntTrie compactToTrieWithRowIndexes() {
        PVecToTrieCompactHandler pVecToTrieCompactHandler = new PVecToTrieCompactHandler();
        compact(pVecToTrieCompactHandler);
        return pVecToTrieCompactHandler.builder.serialize(new DefaultGetFoldedValue(pVecToTrieCompactHandler.builder), new DefaultGetFoldingOffset());
    }

    private static class DefaultGetFoldingOffset implements Trie.DataManipulate {
        @Override // ohos.global.icu.impl.Trie.DataManipulate
        public int getFoldingOffset(int i) {
            return i;
        }

        private DefaultGetFoldingOffset() {
        }
    }

    private static class DefaultGetFoldedValue implements TrieBuilder.DataManipulate {
        private IntTrieBuilder builder;

        public DefaultGetFoldedValue(IntTrieBuilder intTrieBuilder) {
            this.builder = intTrieBuilder;
        }

        @Override // ohos.global.icu.impl.TrieBuilder.DataManipulate
        public int getFoldedValue(int i, int i2) {
            int i3 = this.builder.m_initialValue_;
            int i4 = i + 1024;
            while (i < i4) {
                boolean[] zArr = new boolean[1];
                int value = this.builder.getValue(i, zArr);
                if (zArr[0]) {
                    i += 32;
                } else if (value != i3) {
                    return i2;
                } else {
                    i++;
                }
            }
            return 0;
        }
    }
}
