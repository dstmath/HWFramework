package android.icu.impl;

import android.icu.impl.TrieBuilder.DataManipulate;
import java.util.Arrays;
import java.util.Comparator;
import org.w3c.dom.traversal.NodeFilter;

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

    private static class DefaultGetFoldedValue implements DataManipulate {
        private IntTrieBuilder builder;

        public DefaultGetFoldedValue(IntTrieBuilder inBuilder) {
            this.builder = inBuilder;
        }

        public int getFoldedValue(int start, int offset) {
            int initialValue = this.builder.m_initialValue_;
            int limit = start + NodeFilter.SHOW_DOCUMENT_FRAGMENT;
            while (start < limit) {
                boolean[] inBlockZero = new boolean[1];
                int value = this.builder.getValue(start, inBlockZero);
                if (inBlockZero[0]) {
                    start += 32;
                } else if (value != initialValue) {
                    return offset;
                } else {
                    start++;
                }
            }
            return 0;
        }
    }

    private static class DefaultGetFoldingOffset implements Trie.DataManipulate {
        private DefaultGetFoldingOffset() {
        }

        public int getFoldingOffset(int value) {
            return value;
        }
    }

    public void setValue(int r1, int r2, int r3, int r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.PropsVectors.setValue(int, int, int, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.PropsVectors.setValue(int, int, int, int, int):void");
    }

    private boolean areElementsSame(int index1, int[] target, int index2, int length) {
        for (int i = 0; i < length; i++) {
            if (this.v[index1 + i] != target[index2 + i]) {
                return false;
            }
        }
        return true;
    }

    private int findRow(int rangeStart) {
        int index = this.prevRow * this.columns;
        if (rangeStart >= this.v[index]) {
            if (rangeStart < this.v[index + 1]) {
                return index;
            }
            index += this.columns;
            if (rangeStart < this.v[index + 1]) {
                this.prevRow++;
                return index;
            }
            index += this.columns;
            if (rangeStart < this.v[index + 1]) {
                this.prevRow += 2;
                return index;
            } else if (rangeStart - this.v[index + 1] < 10) {
                this.prevRow += 2;
                do {
                    this.prevRow++;
                    index += this.columns;
                } while (rangeStart >= this.v[index + 1]);
                return index;
            }
        } else if (rangeStart < this.v[1]) {
            this.prevRow = 0;
            return 0;
        }
        int start = 0;
        int limit = this.rows;
        while (start < limit - 1) {
            int mid = (start + limit) / 2;
            index = this.columns * mid;
            if (rangeStart < this.v[index]) {
                limit = mid;
            } else if (rangeStart < this.v[index + 1]) {
                this.prevRow = mid;
                return index;
            } else {
                start = mid;
            }
        }
        this.prevRow = start;
        return start * this.columns;
    }

    public PropsVectors(int numOfColumns) {
        if (numOfColumns < 1) {
            throw new IllegalArgumentException("numOfColumns need to be no less than 1; but it is " + numOfColumns);
        }
        this.columns = numOfColumns + 2;
        this.v = new int[(this.columns * INITIAL_ROWS)];
        this.maxRows = INITIAL_ROWS;
        this.rows = 3;
        this.prevRow = 0;
        this.isCompacted = false;
        this.v[0] = 0;
        this.v[1] = INITIAL_VALUE_CP;
        int index = this.columns;
        for (int cp = INITIAL_VALUE_CP; cp <= MAX_CP; cp++) {
            this.v[index] = cp;
            this.v[index + 1] = cp + 1;
            index += this.columns;
        }
    }

    public int getValue(int c, int column) {
        if (this.isCompacted || c < 0 || c > MAX_CP || column < 0 || column >= this.columns - 2) {
            return 0;
        }
        return this.v[(findRow(c) + 2) + column];
    }

    public int[] getRow(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex < 0 || rowIndex > this.rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        } else {
            int[] rowToReturn = new int[(this.columns - 2)];
            System.arraycopy(this.v, (this.columns * rowIndex) + 2, rowToReturn, 0, this.columns - 2);
            return rowToReturn;
        }
    }

    public int getRowStart(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex >= 0 && rowIndex <= this.rows) {
            return this.v[this.columns * rowIndex];
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public int getRowEnd(int rowIndex) {
        if (this.isCompacted) {
            throw new IllegalStateException("Illegal Invocation of the method after compact()");
        } else if (rowIndex >= 0 && rowIndex <= this.rows) {
            return this.v[(this.columns * rowIndex) + 1] - 1;
        } else {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
    }

    public void compact(CompactHandler compactor) {
        if (!this.isCompacted) {
            int i;
            int start;
            this.isCompacted = true;
            int valueColumns = this.columns - 2;
            Integer[] indexArray = new Integer[this.rows];
            for (i = 0; i < this.rows; i++) {
                indexArray[i] = Integer.valueOf(this.columns * i);
            }
            Arrays.sort(indexArray, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    int i;
                    int indexOfRow1 = o1.intValue();
                    int indexOfRow2 = o2.intValue();
                    int count = PropsVectors.this.columns;
                    int index = 2;
                    while (PropsVectors.this.v[indexOfRow1 + index] == PropsVectors.this.v[indexOfRow2 + index]) {
                        index++;
                        if (index == PropsVectors.this.columns) {
                            index = 0;
                        }
                        count--;
                        if (count <= 0) {
                            return 0;
                        }
                    }
                    if (PropsVectors.this.v[indexOfRow1 + index] < PropsVectors.this.v[indexOfRow2 + index]) {
                        i = -1;
                    } else {
                        i = 1;
                    }
                    return i;
                }
            });
            int count = -valueColumns;
            i = 0;
            while (i < this.rows) {
                start = this.v[indexArray[i].intValue()];
                if (count < 0 || !areElementsSame(indexArray[i].intValue() + 2, this.v, indexArray[i - 1].intValue() + 2, valueColumns)) {
                    count += valueColumns;
                }
                if (start == INITIAL_VALUE_CP) {
                    compactor.setRowIndexForInitialValue(count);
                } else if (start == MAX_CP) {
                    compactor.setRowIndexForErrorValue(count);
                }
                i++;
            }
            count += valueColumns;
            compactor.startRealValues(count);
            int[] temp = new int[count];
            count = -valueColumns;
            i = 0;
            while (i < this.rows) {
                start = this.v[indexArray[i].intValue()];
                int limit = this.v[indexArray[i].intValue() + 1];
                if (count < 0 || !areElementsSame(indexArray[i].intValue() + 2, temp, count, valueColumns)) {
                    count += valueColumns;
                    System.arraycopy(this.v, indexArray[i].intValue() + 2, temp, count, valueColumns);
                }
                if (start < INITIAL_VALUE_CP) {
                    compactor.setRowIndexForRange(start, limit - 1, count);
                }
                i++;
            }
            this.v = temp;
            this.rows = (count / valueColumns) + 1;
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
        PVecToTrieCompactHandler compactor = new PVecToTrieCompactHandler();
        compact(compactor);
        return compactor.builder.serialize(new DefaultGetFoldedValue(compactor.builder), new DefaultGetFoldingOffset());
    }
}
