package com.android.internal.app.procstats;

import android.os.Build;
import android.os.Parcel;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.util.GrowingArrayUtils;
import java.util.ArrayList;
import libcore.util.EmptyArray;

public class SparseMappingTable {
    private static final int ARRAY_MASK = 255;
    private static final int ARRAY_SHIFT = 8;
    public static final int ARRAY_SIZE = 4096;
    private static final int ID_MASK = 255;
    private static final int ID_SHIFT = 0;
    private static final int INDEX_MASK = 65535;
    private static final int INDEX_SHIFT = 16;
    public static final int INVALID_KEY = -1;
    private static final String TAG = "SparseMappingTable";
    private final ArrayList<long[]> mLongs = new ArrayList<>();
    private int mNextIndex;
    private int mSequence;

    static /* synthetic */ int access$212(SparseMappingTable x0, int x1) {
        int i = x0.mNextIndex + x1;
        x0.mNextIndex = i;
        return i;
    }

    public static class Table {
        private SparseMappingTable mParent;
        private int mSequence = 1;
        private int mSize;
        private int[] mTable;

        public Table(SparseMappingTable parent) {
            this.mParent = parent;
            this.mSequence = parent.mSequence;
        }

        public void copyFrom(Table copyFrom, int valueCount) {
            this.mTable = null;
            this.mSize = 0;
            int N = copyFrom.getKeyCount();
            for (int i = 0; i < N; i++) {
                int theirKey = copyFrom.getKeyAt(i);
                int myKey = getOrAddKey(SparseMappingTable.getIdFromKey(theirKey), valueCount);
                System.arraycopy((long[]) copyFrom.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(theirKey)), SparseMappingTable.getIndexFromKey(theirKey), (long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(myKey)), SparseMappingTable.getIndexFromKey(myKey), valueCount);
            }
        }

        public int getOrAddKey(byte id, int count) {
            assertConsistency();
            int idx = binarySearch(id);
            if (idx >= 0) {
                return this.mTable[idx];
            }
            ArrayList<long[]> list = this.mParent.mLongs;
            int whichArray = list.size() - 1;
            if (this.mParent.mNextIndex + count > list.get(whichArray).length) {
                list.add(new long[4096]);
                whichArray++;
                this.mParent.mNextIndex = 0;
            }
            int key = (whichArray << 8) | (this.mParent.mNextIndex << 16) | (id << 0);
            SparseMappingTable.access$212(this.mParent, count);
            int[] iArr = this.mTable;
            if (iArr == null) {
                iArr = EmptyArray.INT;
            }
            this.mTable = GrowingArrayUtils.insert(iArr, this.mSize, ~idx, key);
            this.mSize++;
            return key;
        }

        public int getKey(byte id) {
            assertConsistency();
            int idx = binarySearch(id);
            if (idx >= 0) {
                return this.mTable[idx];
            }
            return -1;
        }

        public long getValue(int key) {
            return getValue(key, 0);
        }

        public long getValue(int key, int index) {
            assertConsistency();
            try {
                return ((long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(key)))[SparseMappingTable.getIndexFromKey(key) + index];
            } catch (IndexOutOfBoundsException ex) {
                SparseMappingTable.logOrThrow("key=0x" + Integer.toHexString(key) + " index=" + index + " -- " + dumpInternalState(), ex);
                return 0;
            }
        }

        public long getValueForId(byte id) {
            return getValueForId(id, 0);
        }

        public long getValueForId(byte id, int index) {
            assertConsistency();
            int idx = binarySearch(id);
            if (idx < 0) {
                return 0;
            }
            int key = this.mTable[idx];
            try {
                return ((long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(key)))[SparseMappingTable.getIndexFromKey(key) + index];
            } catch (IndexOutOfBoundsException ex) {
                SparseMappingTable.logOrThrow("id=0x" + Integer.toHexString(id) + " idx=" + idx + " key=0x" + Integer.toHexString(key) + " index=" + index + " -- " + dumpInternalState(), ex);
                return 0;
            }
        }

        public long[] getArrayForKey(int key) {
            assertConsistency();
            try {
                return (long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(key));
            } catch (IndexOutOfBoundsException ex) {
                Slog.w(SparseMappingTable.TAG, "getArrayForKey err", ex);
                return new long[4096];
            }
        }

        public void setValue(int key, long value) {
            setValue(key, 0, value);
        }

        public void setValue(int key, int index, long value) {
            assertConsistency();
            if (value < 0) {
                SparseMappingTable.logOrThrow("can't store negative values key=0x" + Integer.toHexString(key) + " index=" + index + " value=" + value + " -- " + dumpInternalState());
                return;
            }
            try {
                ((long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(key)))[SparseMappingTable.getIndexFromKey(key) + index] = value;
            } catch (IndexOutOfBoundsException ex) {
                SparseMappingTable.logOrThrow("key=0x" + Integer.toHexString(key) + " index=" + index + " value=" + value + " -- " + dumpInternalState(), ex);
            }
        }

        public void resetTable() {
            this.mTable = null;
            this.mSize = 0;
            this.mSequence = this.mParent.mSequence;
        }

        public void writeToParcel(Parcel out) {
            out.writeInt(this.mSequence);
            out.writeInt(this.mSize);
            for (int i = 0; i < this.mSize; i++) {
                out.writeInt(this.mTable[i]);
            }
        }

        public boolean readFromParcel(Parcel in) {
            this.mSequence = in.readInt();
            this.mSize = in.readInt();
            int i = this.mSize;
            if (i != 0) {
                this.mTable = new int[i];
                for (int i2 = 0; i2 < this.mSize; i2++) {
                    this.mTable[i2] = in.readInt();
                }
            } else {
                this.mTable = null;
            }
            if (validateKeys(true)) {
                return true;
            }
            this.mSize = 0;
            this.mTable = null;
            return false;
        }

        public int getKeyCount() {
            return this.mSize;
        }

        public int getKeyAt(int i) {
            return this.mTable[i];
        }

        private void assertConsistency() {
        }

        private int binarySearch(byte id) {
            int lo = 0;
            int hi = this.mSize - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                byte midId = (byte) ((this.mTable[mid] >> 0) & 255);
                if (midId < id) {
                    lo = mid + 1;
                } else if (midId <= id) {
                    return mid;
                } else {
                    hi = mid - 1;
                }
            }
            return ~lo;
        }

        private boolean validateKeys(boolean log) {
            ArrayList<long[]> longs = this.mParent.mLongs;
            int longsSize = longs.size();
            int N = this.mSize;
            for (int i = 0; i < N; i++) {
                int key = this.mTable[i];
                int arrayIndex = SparseMappingTable.getArrayFromKey(key);
                int index = SparseMappingTable.getIndexFromKey(key);
                if (arrayIndex >= longsSize || index >= longs.get(arrayIndex).length) {
                    if (!log) {
                        return false;
                    } else {
                        Slog.w(SparseMappingTable.TAG, "Invalid stats at index " + i + " -- " + dumpInternalState());
                        return false;
                    }
                }
            }
            return true;
        }

        public String dumpInternalState() {
            StringBuilder sb = new StringBuilder();
            sb.append("SparseMappingTable.Table{mSequence=");
            sb.append(this.mSequence);
            sb.append(" mParent.mSequence=");
            sb.append(this.mParent.mSequence);
            sb.append(" mParent.mLongs.size()=");
            sb.append(this.mParent.mLongs.size());
            sb.append(" mSize=");
            sb.append(this.mSize);
            sb.append(" mTable=");
            int[] iArr = this.mTable;
            if (iArr == null) {
                sb.append("null");
            } else {
                int N = iArr.length;
                sb.append('[');
                for (int i = 0; i < N; i++) {
                    int key = this.mTable[i];
                    sb.append("0x");
                    sb.append(Integer.toHexString((key >> 0) & 255));
                    sb.append("/0x");
                    sb.append(Integer.toHexString((key >> 8) & 255));
                    sb.append("/0x");
                    sb.append(Integer.toHexString((key >> 16) & 65535));
                    if (i != N - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(']');
            }
            sb.append(" clazz=");
            sb.append(getClass().getName());
            sb.append('}');
            return sb.toString();
        }
    }

    public SparseMappingTable() {
        this.mLongs.add(new long[4096]);
    }

    public void reset() {
        this.mLongs.clear();
        this.mLongs.add(new long[4096]);
        this.mNextIndex = 0;
        this.mSequence++;
    }

    public void writeToParcel(Parcel out) {
        out.writeInt(this.mSequence);
        out.writeInt(this.mNextIndex);
        int N = this.mLongs.size();
        out.writeInt(N);
        for (int i = 0; i < N - 1; i++) {
            long[] array = this.mLongs.get(i);
            out.writeInt(array.length);
            writeCompactedLongArray(out, array, array.length);
        }
        out.writeInt(this.mNextIndex);
        writeCompactedLongArray(out, this.mLongs.get(N - 1), this.mNextIndex);
    }

    public void readFromParcel(Parcel in) {
        this.mSequence = in.readInt();
        this.mNextIndex = in.readInt();
        this.mLongs.clear();
        int N = in.readInt();
        for (int i = 0; i < N; i++) {
            int size = in.readInt();
            long[] array = new long[size];
            readCompactedLongArray(in, array, size);
            this.mLongs.add(array);
        }
        if (N > 0 && this.mLongs.get(N - 1).length != this.mNextIndex) {
            EventLog.writeEvent(1397638484, "73252178", -1, "");
            throw new IllegalStateException("Expected array of length " + this.mNextIndex + " but was " + this.mLongs.get(N - 1).length);
        }
    }

    public String dumpInternalState(boolean includeData) {
        StringBuilder sb = new StringBuilder();
        sb.append("SparseMappingTable{");
        sb.append("mSequence=");
        sb.append(this.mSequence);
        sb.append(" mNextIndex=");
        sb.append(this.mNextIndex);
        sb.append(" mLongs.size=");
        int N = this.mLongs.size();
        sb.append(N);
        sb.append("\n");
        if (includeData) {
            int i = 0;
            while (i < N) {
                long[] array = this.mLongs.get(i);
                int j = 0;
                while (j < array.length && (i != N - 1 || j != this.mNextIndex)) {
                    sb.append(String.format(" %4d %d 0x%016x %-19d\n", Integer.valueOf(i), Integer.valueOf(j), Long.valueOf(array[j]), Long.valueOf(array[j])));
                    j++;
                }
                i++;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static void writeCompactedLongArray(Parcel out, long[] array, int num) {
        for (int i = 0; i < num; i++) {
            long val = array[i];
            if (val < 0) {
                Slog.w(TAG, "Time val negative: " + val);
                val = 0;
            }
            if (val <= 2147483647L) {
                out.writeInt((int) val);
            } else {
                out.writeInt(~((int) (2147483647L & (val >> 32))));
                out.writeInt((int) (4294967295L & val));
            }
        }
    }

    private static void readCompactedLongArray(Parcel in, long[] array, int num) {
        int alen = array.length;
        if (num > alen) {
            logOrThrow("bad array lengths: got " + num + " array is " + alen);
            return;
        }
        int i = 0;
        while (i < num) {
            int val = in.readInt();
            if (val >= 0) {
                array[i] = (long) val;
            } else {
                array[i] = (((long) (~val)) << 32) | ((long) in.readInt());
            }
            i++;
        }
        while (i < alen) {
            array[i] = 0;
            i++;
        }
    }

    public static byte getIdFromKey(int key) {
        return (byte) ((key >> 0) & 255);
    }

    public static int getArrayFromKey(int key) {
        return (key >> 8) & 255;
    }

    public static int getIndexFromKey(int key) {
        return (key >> 16) & 65535;
    }

    /* access modifiers changed from: private */
    public static void logOrThrow(String message) {
        logOrThrow(message, new RuntimeException("Stack trace"));
    }

    /* access modifiers changed from: private */
    public static void logOrThrow(String message, Throwable th) {
        Slog.e(TAG, message, th);
        if (Build.IS_ENG) {
            throw new RuntimeException(message, th);
        }
    }
}
