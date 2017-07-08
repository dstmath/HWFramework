package com.android.internal.app.procstats;

import android.os.Build;
import android.os.Parcel;
import android.util.Slog;
import java.util.ArrayList;

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
    private final ArrayList<long[]> mLongs;
    private int mNextIndex;
    private int mSequence;

    public static class Table {
        private SparseMappingTable mParent;
        private int mSequence;
        private int mSize;
        private int[] mTable;

        private int binarySearch(byte r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.SparseMappingTable.Table.binarySearch(byte):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
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
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.SparseMappingTable.Table.binarySearch(byte):int");
        }

        public int getOrAddKey(byte r1, int r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.SparseMappingTable.Table.getOrAddKey(byte, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
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
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.SparseMappingTable.Table.getOrAddKey(byte, int):int");
        }

        public Table(SparseMappingTable parent) {
            this.mSequence = 1;
            this.mParent = parent;
            this.mSequence = parent.mSequence;
        }

        public void copyFrom(Table copyFrom, int valueCount) {
            this.mTable = null;
            this.mSize = SparseMappingTable.ID_SHIFT;
            int N = copyFrom.getKeyCount();
            for (int i = SparseMappingTable.ID_SHIFT; i < N; i++) {
                int theirKey = copyFrom.getKeyAt(i);
                long[] theirLongs = (long[]) copyFrom.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(theirKey));
                int myKey = getOrAddKey(SparseMappingTable.getIdFromKey(theirKey), valueCount);
                System.arraycopy(theirLongs, SparseMappingTable.getIndexFromKey(theirKey), (long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(myKey)), SparseMappingTable.getIndexFromKey(myKey), valueCount);
            }
        }

        public int getKey(byte id) {
            assertConsistency();
            int idx = binarySearch(id);
            if (idx >= 0) {
                return this.mTable[idx];
            }
            return SparseMappingTable.INVALID_KEY;
        }

        public long getValue(int key) {
            return getValue(key, SparseMappingTable.ID_SHIFT);
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
            return getValueForId(id, SparseMappingTable.ID_SHIFT);
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
            return (long[]) this.mParent.mLongs.get(SparseMappingTable.getArrayFromKey(key));
        }

        public void setValue(int key, long value) {
            setValue(key, SparseMappingTable.ID_SHIFT, value);
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
            this.mSize = SparseMappingTable.ID_SHIFT;
            this.mSequence = this.mParent.mSequence;
        }

        public void writeToParcel(Parcel out) {
            out.writeInt(this.mSequence);
            out.writeInt(this.mSize);
            for (int i = SparseMappingTable.ID_SHIFT; i < this.mSize; i++) {
                out.writeInt(this.mTable[i]);
            }
        }

        public boolean readFromParcel(Parcel in) {
            this.mSequence = in.readInt();
            this.mSize = in.readInt();
            if (this.mSize != 0) {
                this.mTable = new int[this.mSize];
                for (int i = SparseMappingTable.ID_SHIFT; i < this.mSize; i++) {
                    this.mTable[i] = in.readInt();
                }
            } else {
                this.mTable = null;
            }
            if (validateKeys(true)) {
                return true;
            }
            this.mSize = SparseMappingTable.ID_SHIFT;
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

        private boolean validateKeys(boolean log) {
            ArrayList<long[]> longs = this.mParent.mLongs;
            int longsSize = longs.size();
            int N = this.mSize;
            for (int i = SparseMappingTable.ID_SHIFT; i < N; i++) {
                int key = this.mTable[i];
                int arrayIndex = SparseMappingTable.getArrayFromKey(key);
                int index = SparseMappingTable.getIndexFromKey(key);
                if (arrayIndex >= longsSize || index >= ((long[]) longs.get(arrayIndex)).length) {
                    if (log) {
                        Slog.w(SparseMappingTable.TAG, "Invalid stats at index " + i + " -- " + dumpInternalState());
                    }
                    return false;
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
            if (this.mTable == null) {
                sb.append("null");
            } else {
                int N = this.mTable.length;
                sb.append('[');
                for (int i = SparseMappingTable.ID_SHIFT; i < N; i++) {
                    int key = this.mTable[i];
                    sb.append("0x");
                    sb.append(Integer.toHexString((key >> SparseMappingTable.ID_SHIFT) & SparseMappingTable.ID_MASK));
                    sb.append("/0x");
                    sb.append(Integer.toHexString((key >> SparseMappingTable.ARRAY_SHIFT) & SparseMappingTable.ID_MASK));
                    sb.append("/0x");
                    sb.append(Integer.toHexString((key >> SparseMappingTable.INDEX_SHIFT) & SparseMappingTable.INDEX_MASK));
                    if (i != N + SparseMappingTable.INVALID_KEY) {
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

    private static void readCompactedLongArray(android.os.Parcel r1, long[] r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.SparseMappingTable.readCompactedLongArray(android.os.Parcel, long[], int):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.SparseMappingTable.readCompactedLongArray(android.os.Parcel, long[], int):void");
    }

    private static void writeCompactedLongArray(android.os.Parcel r1, long[] r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.procstats.SparseMappingTable.writeCompactedLongArray(android.os.Parcel, long[], int):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.procstats.SparseMappingTable.writeCompactedLongArray(android.os.Parcel, long[], int):void");
    }

    public SparseMappingTable() {
        this.mLongs = new ArrayList();
        this.mLongs.add(new long[ARRAY_SIZE]);
    }

    public void reset() {
        this.mLongs.clear();
        this.mLongs.add(new long[ARRAY_SIZE]);
        this.mNextIndex = ID_SHIFT;
        this.mSequence++;
    }

    public void writeToParcel(Parcel out) {
        out.writeInt(this.mSequence);
        out.writeInt(this.mNextIndex);
        int N = this.mLongs.size();
        out.writeInt(N);
        for (int i = ID_SHIFT; i < N + INVALID_KEY; i++) {
            long[] array = (long[]) this.mLongs.get(i);
            out.writeInt(array.length);
            writeCompactedLongArray(out, array, array.length);
        }
        long[] lastLongs = (long[]) this.mLongs.get(N + INVALID_KEY);
        out.writeInt(this.mNextIndex);
        writeCompactedLongArray(out, lastLongs, this.mNextIndex);
    }

    public void readFromParcel(Parcel in) {
        this.mSequence = in.readInt();
        this.mNextIndex = in.readInt();
        this.mLongs.clear();
        int N = in.readInt();
        for (int i = ID_SHIFT; i < N; i++) {
            int size = in.readInt();
            long[] array = new long[size];
            readCompactedLongArray(in, array, size);
            this.mLongs.add(array);
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
            int i = ID_SHIFT;
            while (i < N) {
                long[] array = (long[]) this.mLongs.get(i);
                int j = ID_SHIFT;
                while (j < array.length && (i != N + INVALID_KEY || j != this.mNextIndex)) {
                    sb.append(String.format(" %4d %d 0x%016x %-19d\n", new Object[]{Integer.valueOf(i), Integer.valueOf(j), Long.valueOf(array[j]), Long.valueOf(array[j])}));
                    j++;
                }
                i++;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static byte getIdFromKey(int key) {
        return (byte) ((key >> ID_SHIFT) & ID_MASK);
    }

    public static int getArrayFromKey(int key) {
        return (key >> ARRAY_SHIFT) & ID_MASK;
    }

    public static int getIndexFromKey(int key) {
        return (key >> INDEX_SHIFT) & INDEX_MASK;
    }

    private static void logOrThrow(String message) {
        logOrThrow(message, new RuntimeException("Stack trace"));
    }

    private static void logOrThrow(String message, Throwable th) {
        Slog.e(TAG, message, th);
        if (Build.TYPE.equals("eng")) {
            throw new RuntimeException(message, th);
        }
    }
}
