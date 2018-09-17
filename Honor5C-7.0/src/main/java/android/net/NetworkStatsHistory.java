package android.net;

import android.net.wifi.ScanResult.InformationElement;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Trace;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.Random;

public class NetworkStatsHistory implements Parcelable {
    public static final Creator<NetworkStatsHistory> CREATOR = null;
    public static final int FIELD_ACTIVE_TIME = 1;
    public static final int FIELD_ALL = -1;
    public static final int FIELD_OPERATIONS = 32;
    public static final int FIELD_RX_BYTES = 2;
    public static final int FIELD_RX_PACKETS = 4;
    public static final int FIELD_TX_BYTES = 8;
    public static final int FIELD_TX_PACKETS = 16;
    private static final int VERSION_ADD_ACTIVE = 3;
    private static final int VERSION_ADD_PACKETS = 2;
    private static final int VERSION_INIT = 1;
    private long[] activeTime;
    private int bucketCount;
    private long bucketDuration;
    private long[] bucketStart;
    private long[] operations;
    private long[] rxBytes;
    private long[] rxPackets;
    private long totalBytes;
    private long totalRxBytes;
    private long totalTxBytes;
    private long[] txBytes;
    private long[] txPackets;

    public static class DataStreamUtils {
        @Deprecated
        public static long[] readFullLongArray(DataInputStream in) throws IOException {
            int size = in.readInt();
            if (size < 0) {
                throw new ProtocolException("negative array size");
            }
            long[] values = new long[size];
            for (int i = 0; i < values.length; i += NetworkStatsHistory.VERSION_INIT) {
                values[i] = in.readLong();
            }
            return values;
        }

        public static long readVarLong(DataInputStream in) throws IOException {
            long result = 0;
            for (int shift = 0; shift < 64; shift += 7) {
                byte b = in.readByte();
                result |= ((long) (b & InformationElement.EID_EXTENDED_CAPS)) << shift;
                if ((b & KeymasterDefs.KM_ALGORITHM_HMAC) == 0) {
                    return result;
                }
            }
            throw new ProtocolException("malformed long");
        }

        public static void writeVarLong(DataOutputStream out, long value) throws IOException {
            while ((-128 & value) != 0) {
                out.writeByte((((int) value) & InformationElement.EID_EXTENDED_CAPS) | KeymasterDefs.KM_ALGORITHM_HMAC);
                value >>>= 7;
            }
            out.writeByte((int) value);
        }

        public static long[] readVarLongArray(DataInputStream in) throws IOException {
            int size = in.readInt();
            if (size == NetworkStatsHistory.FIELD_ALL) {
                return null;
            }
            if (size < 0) {
                throw new ProtocolException("negative array size");
            }
            long[] values = new long[size];
            for (int i = 0; i < values.length; i += NetworkStatsHistory.VERSION_INIT) {
                values[i] = readVarLong(in);
            }
            return values;
        }

        public static void writeVarLongArray(DataOutputStream out, long[] values, int size) throws IOException {
            if (values == null) {
                out.writeInt(NetworkStatsHistory.FIELD_ALL);
            } else if (size > values.length) {
                throw new IllegalArgumentException("size larger than length");
            } else {
                out.writeInt(size);
                for (int i = 0; i < size; i += NetworkStatsHistory.VERSION_INIT) {
                    writeVarLong(out, values[i]);
                }
            }
        }
    }

    public static class Entry {
        public static final long UNKNOWN = -1;
        public long activeTime;
        public long bucketDuration;
        public long bucketStart;
        public long operations;
        public long rxBytes;
        public long rxPackets;
        public long txBytes;
        public long txPackets;
    }

    public static class ParcelUtils {
        public static long[] readLongArray(Parcel in) {
            int size = in.readInt();
            if (size == NetworkStatsHistory.FIELD_ALL) {
                return null;
            }
            long[] values = new long[size];
            for (int i = 0; i < values.length; i += NetworkStatsHistory.VERSION_INIT) {
                values[i] = in.readLong();
            }
            return values;
        }

        public static void writeLongArray(Parcel out, long[] values, int size) {
            if (values == null) {
                out.writeInt(NetworkStatsHistory.FIELD_ALL);
            } else if (size > values.length) {
                throw new IllegalArgumentException("size larger than length");
            } else {
                out.writeInt(size);
                for (int i = 0; i < size; i += NetworkStatsHistory.VERSION_INIT) {
                    out.writeLong(values[i]);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkStatsHistory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkStatsHistory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkStatsHistory.<clinit>():void");
    }

    private void ensureBuckets(long r1, long r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkStatsHistory.ensureBuckets(long, long):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkStatsHistory.ensureBuckets(long, long):void");
    }

    public int getIndexAfter(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkStatsHistory.getIndexAfter(long):int
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkStatsHistory.getIndexAfter(long):int");
    }

    public int getIndexBefore(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkStatsHistory.getIndexBefore(long):int
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkStatsHistory.getIndexBefore(long):int");
    }

    public NetworkStatsHistory(long bucketDuration) {
        this(bucketDuration, 10, FIELD_ALL);
    }

    public NetworkStatsHistory(long bucketDuration, int initialSize) {
        this(bucketDuration, initialSize, FIELD_ALL);
    }

    public NetworkStatsHistory(long bucketDuration, int initialSize, int fields) {
        this.bucketDuration = bucketDuration;
        this.bucketStart = new long[initialSize];
        if ((fields & VERSION_INIT) != 0) {
            this.activeTime = new long[initialSize];
        }
        if ((fields & VERSION_ADD_PACKETS) != 0) {
            this.rxBytes = new long[initialSize];
        }
        if ((fields & FIELD_RX_PACKETS) != 0) {
            this.rxPackets = new long[initialSize];
        }
        if ((fields & FIELD_TX_BYTES) != 0) {
            this.txBytes = new long[initialSize];
        }
        if ((fields & FIELD_TX_PACKETS) != 0) {
            this.txPackets = new long[initialSize];
        }
        if ((fields & FIELD_OPERATIONS) != 0) {
            this.operations = new long[initialSize];
        }
        this.bucketCount = 0;
        this.totalBytes = 0;
        this.totalTxBytes = 0;
        this.totalRxBytes = 0;
    }

    public NetworkStatsHistory(NetworkStatsHistory existing, long bucketDuration) {
        this(bucketDuration, existing.estimateResizeBuckets(bucketDuration));
        recordEntireHistory(existing);
    }

    public NetworkStatsHistory(Parcel in) {
        this.bucketDuration = in.readLong();
        this.bucketStart = ParcelUtils.readLongArray(in);
        this.activeTime = ParcelUtils.readLongArray(in);
        this.rxBytes = ParcelUtils.readLongArray(in);
        this.rxPackets = ParcelUtils.readLongArray(in);
        this.txBytes = ParcelUtils.readLongArray(in);
        this.txPackets = ParcelUtils.readLongArray(in);
        this.operations = ParcelUtils.readLongArray(in);
        this.bucketCount = this.bucketStart.length;
        this.totalBytes = in.readLong();
        this.totalTxBytes = ArrayUtils.total(this.txBytes);
        this.totalRxBytes = ArrayUtils.total(this.rxBytes);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.bucketDuration);
        ParcelUtils.writeLongArray(out, this.bucketStart, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.activeTime, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.rxBytes, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.rxPackets, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.txBytes, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.txPackets, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.operations, this.bucketCount);
        out.writeLong(this.totalBytes);
    }

    public NetworkStatsHistory(DataInputStream in) throws IOException {
        int version = in.readInt();
        switch (version) {
            case VERSION_INIT /*1*/:
                this.bucketDuration = in.readLong();
                this.bucketStart = DataStreamUtils.readFullLongArray(in);
                this.rxBytes = DataStreamUtils.readFullLongArray(in);
                this.rxPackets = new long[this.bucketStart.length];
                this.txBytes = DataStreamUtils.readFullLongArray(in);
                this.txPackets = new long[this.bucketStart.length];
                this.operations = new long[this.bucketStart.length];
                this.bucketCount = this.bucketStart.length;
                this.totalBytes = ArrayUtils.total(this.rxBytes) + ArrayUtils.total(this.txBytes);
                this.totalTxBytes = ArrayUtils.total(this.txBytes);
                this.totalRxBytes = ArrayUtils.total(this.rxBytes);
                break;
            case VERSION_ADD_PACKETS /*2*/:
            case VERSION_ADD_ACTIVE /*3*/:
                long[] readVarLongArray;
                this.bucketDuration = in.readLong();
                this.bucketStart = DataStreamUtils.readVarLongArray(in);
                if (version >= VERSION_ADD_ACTIVE) {
                    readVarLongArray = DataStreamUtils.readVarLongArray(in);
                } else {
                    readVarLongArray = new long[this.bucketStart.length];
                }
                this.activeTime = readVarLongArray;
                this.rxBytes = DataStreamUtils.readVarLongArray(in);
                this.rxPackets = DataStreamUtils.readVarLongArray(in);
                this.txBytes = DataStreamUtils.readVarLongArray(in);
                this.txPackets = DataStreamUtils.readVarLongArray(in);
                this.operations = DataStreamUtils.readVarLongArray(in);
                this.bucketCount = this.bucketStart.length;
                this.totalBytes = ArrayUtils.total(this.rxBytes) + ArrayUtils.total(this.txBytes);
                this.totalTxBytes = ArrayUtils.total(this.txBytes);
                this.totalRxBytes = ArrayUtils.total(this.rxBytes);
                break;
            default:
                throw new ProtocolException("unexpected version: " + version);
        }
        if (this.bucketStart.length != this.bucketCount || this.rxBytes.length != this.bucketCount || this.rxPackets.length != this.bucketCount || this.txBytes.length != this.bucketCount || this.txPackets.length != this.bucketCount || this.operations.length != this.bucketCount) {
            throw new ProtocolException("Mismatched history lengths");
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(VERSION_ADD_ACTIVE);
        out.writeLong(this.bucketDuration);
        DataStreamUtils.writeVarLongArray(out, this.bucketStart, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.activeTime, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxBytes, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxPackets, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txBytes, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txPackets, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.operations, this.bucketCount);
    }

    public int describeContents() {
        return 0;
    }

    public int size() {
        return this.bucketCount;
    }

    public long getBucketDuration() {
        return this.bucketDuration;
    }

    public long getStart() {
        if (this.bucketCount > 0) {
            return this.bucketStart[0];
        }
        return LinkQualityInfo.UNKNOWN_LONG;
    }

    public long getEnd() {
        if (this.bucketCount > 0) {
            return this.bucketStart[this.bucketCount + FIELD_ALL] + this.bucketDuration;
        }
        return Long.MIN_VALUE;
    }

    public long getTotalBytes() {
        return this.totalBytes;
    }

    public long getTotalTxBytes() {
        return this.totalTxBytes;
    }

    public long getTotalRxBytes() {
        return this.totalRxBytes;
    }

    public Entry getValues(int i, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.bucketStart = this.bucketStart[i];
        entry.bucketDuration = this.bucketDuration;
        entry.activeTime = getLong(this.activeTime, i, -1);
        entry.rxBytes = getLong(this.rxBytes, i, -1);
        entry.rxPackets = getLong(this.rxPackets, i, -1);
        entry.txBytes = getLong(this.txBytes, i, -1);
        entry.txPackets = getLong(this.txPackets, i, -1);
        entry.operations = getLong(this.operations, i, -1);
        return entry;
    }

    @Deprecated
    public void recordData(long start, long end, long rxBytes, long txBytes) {
        recordData(start, end, new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, FIELD_ALL, 0, 0, rxBytes, 0, txBytes, 0, 0));
    }

    public void recordData(long start, long end, android.net.NetworkStats.Entry entry) {
        long rxBytes = entry.rxBytes;
        long rxPackets = entry.rxPackets;
        long txBytes = entry.txBytes;
        long txPackets = entry.txPackets;
        long operations = entry.operations;
        if (entry.isNegative()) {
            Log.d("NetworkStatsHistory", "tried recording negative data.");
        } else if (!entry.isEmpty()) {
            ensureBuckets(start, end);
            long duration = end - start;
            for (int i = getIndexAfter(end); i >= 0; i += FIELD_ALL) {
                long curStart = this.bucketStart[i];
                long curEnd = curStart + this.bucketDuration;
                if (curEnd < start) {
                    break;
                }
                if (curStart <= end) {
                    long overlap = Math.min(curEnd, end) - Math.max(curStart, start);
                    if (overlap > 0) {
                        long fracRxBytes = (rxBytes * overlap) / duration;
                        long fracRxPackets = (rxPackets * overlap) / duration;
                        long fracTxBytes = (txBytes * overlap) / duration;
                        long fracTxPackets = (txPackets * overlap) / duration;
                        long fracOperations = (operations * overlap) / duration;
                        addLong(this.activeTime, i, overlap);
                        addLong(this.rxBytes, i, fracRxBytes);
                        rxBytes -= fracRxBytes;
                        addLong(this.rxPackets, i, fracRxPackets);
                        rxPackets -= fracRxPackets;
                        addLong(this.txBytes, i, fracTxBytes);
                        txBytes -= fracTxBytes;
                        addLong(this.txPackets, i, fracTxPackets);
                        txPackets -= fracTxPackets;
                        addLong(this.operations, i, fracOperations);
                        operations -= fracOperations;
                        duration -= overlap;
                    }
                }
            }
            this.totalBytes += entry.rxBytes + entry.txBytes;
            this.totalRxBytes += entry.rxBytes;
            this.totalTxBytes += entry.txBytes;
        }
    }

    public void recordEntireHistory(NetworkStatsHistory input) {
        recordHistory(input, Long.MIN_VALUE, LinkQualityInfo.UNKNOWN_LONG);
    }

    public void recordHistory(NetworkStatsHistory input, long start, long end) {
        android.net.NetworkStats.Entry entry = new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, FIELD_ALL, 0, 0, 0, 0, 0, 0, 0);
        for (int i = 0; i < input.bucketCount; i += VERSION_INIT) {
            long bucketStart = input.bucketStart[i];
            long bucketEnd = bucketStart + input.bucketDuration;
            if (bucketStart >= start && bucketEnd <= end) {
                entry.rxBytes = getLong(input.rxBytes, i, 0);
                entry.rxPackets = getLong(input.rxPackets, i, 0);
                entry.txBytes = getLong(input.txBytes, i, 0);
                entry.txPackets = getLong(input.txPackets, i, 0);
                entry.operations = getLong(input.operations, i, 0);
                recordData(bucketStart, bucketEnd, entry);
            }
        }
    }

    private void insertBucket(int index, long start) {
        if (this.bucketCount >= this.bucketStart.length) {
            int newLength = (Math.max(this.bucketStart.length, 10) * VERSION_ADD_ACTIVE) / VERSION_ADD_PACKETS;
            this.bucketStart = Arrays.copyOf(this.bucketStart, newLength);
            if (this.activeTime != null) {
                this.activeTime = Arrays.copyOf(this.activeTime, newLength);
            }
            if (this.rxBytes != null) {
                this.rxBytes = Arrays.copyOf(this.rxBytes, newLength);
            }
            if (this.rxPackets != null) {
                this.rxPackets = Arrays.copyOf(this.rxPackets, newLength);
            }
            if (this.txBytes != null) {
                this.txBytes = Arrays.copyOf(this.txBytes, newLength);
            }
            if (this.txPackets != null) {
                this.txPackets = Arrays.copyOf(this.txPackets, newLength);
            }
            if (this.operations != null) {
                this.operations = Arrays.copyOf(this.operations, newLength);
            }
        }
        if (index < this.bucketCount) {
            int dstPos = index + VERSION_INIT;
            int length = this.bucketCount - index;
            System.arraycopy(this.bucketStart, index, this.bucketStart, dstPos, length);
            if (this.activeTime != null) {
                System.arraycopy(this.activeTime, index, this.activeTime, dstPos, length);
            }
            if (this.rxBytes != null) {
                System.arraycopy(this.rxBytes, index, this.rxBytes, dstPos, length);
            }
            if (this.rxPackets != null) {
                System.arraycopy(this.rxPackets, index, this.rxPackets, dstPos, length);
            }
            if (this.txBytes != null) {
                System.arraycopy(this.txBytes, index, this.txBytes, dstPos, length);
            }
            if (this.txPackets != null) {
                System.arraycopy(this.txPackets, index, this.txPackets, dstPos, length);
            }
            if (this.operations != null) {
                System.arraycopy(this.operations, index, this.operations, dstPos, length);
            }
        }
        this.bucketStart[index] = start;
        setLong(this.activeTime, index, 0);
        setLong(this.rxBytes, index, 0);
        setLong(this.rxPackets, index, 0);
        setLong(this.txBytes, index, 0);
        setLong(this.txPackets, index, 0);
        setLong(this.operations, index, 0);
        this.bucketCount += VERSION_INIT;
    }

    @Deprecated
    public void removeBucketsBefore(long cutoff) {
        int i = 0;
        while (i < this.bucketCount && this.bucketStart[i] + this.bucketDuration <= cutoff) {
            i += VERSION_INIT;
        }
        if (i > 0) {
            int length = this.bucketStart.length;
            this.bucketStart = Arrays.copyOfRange(this.bucketStart, i, length);
            if (this.activeTime != null) {
                this.activeTime = Arrays.copyOfRange(this.activeTime, i, length);
            }
            if (this.rxBytes != null) {
                this.rxBytes = Arrays.copyOfRange(this.rxBytes, i, length);
            }
            if (this.rxPackets != null) {
                this.rxPackets = Arrays.copyOfRange(this.rxPackets, i, length);
            }
            if (this.txBytes != null) {
                this.txBytes = Arrays.copyOfRange(this.txBytes, i, length);
            }
            if (this.txPackets != null) {
                this.txPackets = Arrays.copyOfRange(this.txPackets, i, length);
            }
            if (this.operations != null) {
                this.operations = Arrays.copyOfRange(this.operations, i, length);
            }
            this.bucketCount -= i;
        }
    }

    public Entry getValues(long start, long end, Entry recycle) {
        return getValues(start, end, LinkQualityInfo.UNKNOWN_LONG, recycle);
    }

    public Entry getValues(long start, long end, long now, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.bucketDuration = end - start;
        entry.bucketStart = start;
        entry.activeTime = this.activeTime != null ? 0 : -1;
        entry.rxBytes = this.rxBytes != null ? 0 : -1;
        entry.rxPackets = this.rxPackets != null ? 0 : -1;
        entry.txBytes = this.txBytes != null ? 0 : -1;
        entry.txPackets = this.txPackets != null ? 0 : -1;
        entry.operations = this.operations != null ? 0 : -1;
        for (int i = getIndexAfter(end); i >= 0; i += FIELD_ALL) {
            long curStart = this.bucketStart[i];
            long curEnd = curStart + this.bucketDuration;
            if (curEnd <= start) {
                break;
            }
            if (curStart < end) {
                long overlap;
                boolean activeBucket = curStart < now && curEnd > now;
                if (activeBucket) {
                    overlap = this.bucketDuration;
                } else {
                    long overlapStart;
                    long overlapEnd = curEnd < end ? curEnd : end;
                    if (curStart > start) {
                        overlapStart = curStart;
                    } else {
                        overlapStart = start;
                    }
                    overlap = overlapEnd - overlapStart;
                }
                if (overlap > 0) {
                    if (this.activeTime != null) {
                        entry.activeTime += (this.activeTime[i] * overlap) / this.bucketDuration;
                    }
                    if (this.rxBytes != null) {
                        entry.rxBytes += (this.rxBytes[i] * overlap) / this.bucketDuration;
                    }
                    if (this.rxPackets != null) {
                        entry.rxPackets += (this.rxPackets[i] * overlap) / this.bucketDuration;
                    }
                    if (this.txBytes != null) {
                        entry.txBytes += (this.txBytes[i] * overlap) / this.bucketDuration;
                    }
                    if (this.txPackets != null) {
                        entry.txPackets += (this.txPackets[i] * overlap) / this.bucketDuration;
                    }
                    if (this.operations != null) {
                        entry.operations += (this.operations[i] * overlap) / this.bucketDuration;
                    }
                }
            }
        }
        return entry;
    }

    @Deprecated
    public void generateRandom(long start, long end, long bytes) {
        Random r = new Random();
        float fractionRx = r.nextFloat();
        long rxBytes = (long) (((float) bytes) * fractionRx);
        long txBytes = (long) (((float) bytes) * (Engine.DEFAULT_VOLUME - fractionRx));
        generateRandom(start, end, rxBytes, rxBytes / Trace.TRACE_TAG_CAMERA, txBytes, txBytes / Trace.TRACE_TAG_CAMERA, rxBytes / Trace.TRACE_TAG_HAL, r);
    }

    @Deprecated
    public void generateRandom(long start, long end, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations, Random r) {
        ensureBuckets(start, end);
        android.net.NetworkStats.Entry entry = new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, FIELD_ALL, 0, 0, 0, 0, 0, 0, 0);
        while (true) {
            if (rxBytes > Trace.TRACE_TAG_CAMERA || rxPackets > 128 || txBytes > Trace.TRACE_TAG_CAMERA || txPackets > 128 || operations > 32) {
                long curStart = randomLong(r, start, end);
                long curEnd = curStart + randomLong(r, 0, (end - curStart) / 2);
                entry.rxBytes = randomLong(r, 0, rxBytes);
                entry.rxPackets = randomLong(r, 0, rxPackets);
                entry.txBytes = randomLong(r, 0, txBytes);
                entry.txPackets = randomLong(r, 0, txPackets);
                entry.operations = randomLong(r, 0, operations);
                rxBytes -= entry.rxBytes;
                rxPackets -= entry.rxPackets;
                txBytes -= entry.txBytes;
                txPackets -= entry.txPackets;
                operations -= entry.operations;
                recordData(curStart, curEnd, entry);
            } else {
                return;
            }
        }
    }

    public static long randomLong(Random r, long start, long end) {
        return (long) (((float) start) + (r.nextFloat() * ((float) (end - start))));
    }

    public boolean intersects(long start, long end) {
        long dataStart = getStart();
        long dataEnd = getEnd();
        if (start >= dataStart && start <= dataEnd) {
            return true;
        }
        if (end >= dataStart && end <= dataEnd) {
            return true;
        }
        if (dataStart >= start && dataStart <= end) {
            return true;
        }
        if (dataEnd < start || dataEnd > end) {
            return false;
        }
        return true;
    }

    public void dump(IndentingPrintWriter pw, boolean fullHistory) {
        int start = 0;
        pw.print("NetworkStatsHistory: bucketDuration=");
        pw.println(this.bucketDuration / 1000);
        pw.increaseIndent();
        if (!fullHistory) {
            start = Math.max(0, this.bucketCount - 32);
        }
        if (start > 0) {
            pw.print("(omitting ");
            pw.print(start);
            pw.println(" buckets)");
        }
        for (int i = start; i < this.bucketCount; i += VERSION_INIT) {
            pw.print("st=");
            pw.print(this.bucketStart[i] / 1000);
            if (this.rxBytes != null) {
                pw.print(" rb=");
                pw.print(this.rxBytes[i]);
            }
            if (this.rxPackets != null) {
                pw.print(" rp=");
                pw.print(this.rxPackets[i]);
            }
            if (this.txBytes != null) {
                pw.print(" tb=");
                pw.print(this.txBytes[i]);
            }
            if (this.txPackets != null) {
                pw.print(" tp=");
                pw.print(this.txPackets[i]);
            }
            if (this.operations != null) {
                pw.print(" op=");
                pw.print(this.operations[i]);
            }
            pw.println();
        }
        pw.decreaseIndent();
    }

    public void dumpCheckin(PrintWriter pw) {
        pw.print("d,");
        pw.print(this.bucketDuration / 1000);
        pw.println();
        for (int i = 0; i < this.bucketCount; i += VERSION_INIT) {
            pw.print("b,");
            pw.print(this.bucketStart[i] / 1000);
            pw.print(',');
            if (this.rxBytes != null) {
                pw.print(this.rxBytes[i]);
            } else {
                pw.print(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER);
            }
            pw.print(',');
            if (this.rxPackets != null) {
                pw.print(this.rxPackets[i]);
            } else {
                pw.print(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER);
            }
            pw.print(',');
            if (this.txBytes != null) {
                pw.print(this.txBytes[i]);
            } else {
                pw.print(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER);
            }
            pw.print(',');
            if (this.txPackets != null) {
                pw.print(this.txPackets[i]);
            } else {
                pw.print(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER);
            }
            pw.print(',');
            if (this.operations != null) {
                pw.print(this.operations[i]);
            } else {
                pw.print(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER);
            }
            pw.println();
        }
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(new IndentingPrintWriter(writer, "  "), false);
        return writer.toString();
    }

    private static long getLong(long[] array, int i, long value) {
        return array != null ? array[i] : value;
    }

    private static void setLong(long[] array, int i, long value) {
        if (array != null) {
            array[i] = value;
        }
    }

    private static void addLong(long[] array, int i, long value) {
        if (array != null) {
            array[i] = array[i] + value;
        }
    }

    public int estimateResizeBuckets(long newBucketDuration) {
        return (int) ((((long) size()) * getBucketDuration()) / newBucketDuration);
    }
}
