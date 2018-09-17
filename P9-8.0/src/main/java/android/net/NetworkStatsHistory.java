package android.net;

import android.os.LooperProto;
import android.os.MessageProto;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import android.util.MathUtils;
import android.util.proto.ProtoOutputStream;
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
    public static final Creator<NetworkStatsHistory> CREATOR = new Creator<NetworkStatsHistory>() {
        public NetworkStatsHistory createFromParcel(Parcel in) {
            return new NetworkStatsHistory(in);
        }

        public NetworkStatsHistory[] newArray(int size) {
            return new NetworkStatsHistory[size];
        }
    };
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
            for (int i = 0; i < values.length; i++) {
                values[i] = in.readLong();
            }
            return values;
        }

        public static long readVarLong(DataInputStream in) throws IOException {
            long result = 0;
            for (int shift = 0; shift < 64; shift += 7) {
                byte b = in.readByte();
                result |= ((long) (b & 127)) << shift;
                if ((b & 128) == 0) {
                    return result;
                }
            }
            throw new ProtocolException("malformed long");
        }

        public static void writeVarLong(DataOutputStream out, long value) throws IOException {
            while ((-128 & value) != 0) {
                out.writeByte((((int) value) & 127) | 128);
                value >>>= 7;
            }
            out.writeByte((int) value);
        }

        public static long[] readVarLongArray(DataInputStream in) throws IOException {
            int size = in.readInt();
            if (size == -1) {
                return null;
            }
            if (size < 0) {
                throw new ProtocolException("negative array size");
            }
            long[] values = new long[size];
            for (int i = 0; i < values.length; i++) {
                values[i] = readVarLong(in);
            }
            return values;
        }

        public static void writeVarLongArray(DataOutputStream out, long[] values, int size) throws IOException {
            if (values == null) {
                out.writeInt(-1);
            } else if (size > values.length) {
                throw new IllegalArgumentException("size larger than length");
            } else {
                out.writeInt(size);
                for (int i = 0; i < size; i++) {
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
            if (size == -1) {
                return null;
            }
            long[] values = new long[size];
            for (int i = 0; i < values.length; i++) {
                values[i] = in.readLong();
            }
            return values;
        }

        public static void writeLongArray(Parcel out, long[] values, int size) {
            if (values == null) {
                out.writeInt(-1);
            } else if (size > values.length) {
                throw new IllegalArgumentException("size larger than length");
            } else {
                out.writeInt(size);
                for (int i = 0; i < size; i++) {
                    out.writeLong(values[i]);
                }
            }
        }
    }

    public NetworkStatsHistory(long bucketDuration) {
        this(bucketDuration, 10, -1);
    }

    public NetworkStatsHistory(long bucketDuration, int initialSize) {
        this(bucketDuration, initialSize, -1);
    }

    public NetworkStatsHistory(long bucketDuration, int initialSize, int fields) {
        this.bucketDuration = bucketDuration;
        this.bucketStart = new long[initialSize];
        if ((fields & 1) != 0) {
            this.activeTime = new long[initialSize];
        }
        if ((fields & 2) != 0) {
            this.rxBytes = new long[initialSize];
        }
        if ((fields & 4) != 0) {
            this.rxPackets = new long[initialSize];
        }
        if ((fields & 8) != 0) {
            this.txBytes = new long[initialSize];
        }
        if ((fields & 16) != 0) {
            this.txPackets = new long[initialSize];
        }
        if ((fields & 32) != 0) {
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
            case 1:
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
            case 2:
            case 3:
                long[] readVarLongArray;
                this.bucketDuration = in.readLong();
                this.bucketStart = DataStreamUtils.readVarLongArray(in);
                if (version >= 3) {
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
        out.writeInt(3);
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
        return Long.MAX_VALUE;
    }

    public long getEnd() {
        if (this.bucketCount > 0) {
            return this.bucketStart[this.bucketCount - 1] + this.bucketDuration;
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

    public int getIndexBefore(long time) {
        int index = Arrays.binarySearch(this.bucketStart, 0, this.bucketCount, time);
        if (index < 0) {
            index = (~index) - 1;
        } else {
            index--;
        }
        return MathUtils.constrain(index, 0, this.bucketCount - 1);
    }

    public int getIndexAfter(long time) {
        int index = Arrays.binarySearch(this.bucketStart, 0, this.bucketCount, time);
        if (index < 0) {
            index = ~index;
        } else {
            index++;
        }
        return MathUtils.constrain(index, 0, this.bucketCount - 1);
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
        recordData(start, end, new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, rxBytes, 0, txBytes, 0, 0));
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
            for (int i = getIndexAfter(end); i >= 0; i--) {
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
        recordHistory(input, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public void recordHistory(NetworkStatsHistory input, long start, long end) {
        android.net.NetworkStats.Entry entry = new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        for (int i = 0; i < input.bucketCount; i++) {
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

    private void ensureBuckets(long start, long end) {
        end += (this.bucketDuration - (end % this.bucketDuration)) % this.bucketDuration;
        long now = start - (start % this.bucketDuration);
        while (now < end) {
            int index = Arrays.binarySearch(this.bucketStart, 0, this.bucketCount, now);
            if (index < 0) {
                insertBucket(~index, now);
            }
            now += this.bucketDuration;
        }
    }

    private void insertBucket(int index, long start) {
        if (this.bucketCount >= this.bucketStart.length) {
            int newLength = (Math.max(this.bucketStart.length, 10) * 3) / 2;
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
            int dstPos = index + 1;
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
        this.bucketCount++;
    }

    @Deprecated
    public void removeBucketsBefore(long cutoff) {
        int i = 0;
        while (i < this.bucketCount && this.bucketStart[i] + this.bucketDuration <= cutoff) {
            i++;
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
        return getValues(start, end, Long.MAX_VALUE, recycle);
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
        for (int i = getIndexAfter(end); i >= 0; i--) {
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
                    overlap = (curEnd < end ? curEnd : end) - (curStart > start ? curStart : start);
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
        long txBytes = (long) (((float) bytes) * (1.0f - fractionRx));
        generateRandom(start, end, rxBytes, rxBytes / 1024, txBytes, txBytes / 1024, rxBytes / 2048, r);
    }

    @Deprecated
    public void generateRandom(long start, long end, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations, Random r) {
        ensureBuckets(start, end);
        android.net.NetworkStats.Entry entry = new android.net.NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        while (true) {
            if (rxBytes > 1024 || rxPackets > 128 || txBytes > 1024 || txPackets > 128 || operations > 32) {
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
        pw.print("NetworkStatsHistory: bucketDuration=");
        pw.println(this.bucketDuration / 1000);
        pw.increaseIndent();
        int start = fullHistory ? 0 : Math.max(0, this.bucketCount - 32);
        if (start > 0) {
            pw.print("(omitting ");
            pw.print(start);
            pw.println(" buckets)");
        }
        for (int i = start; i < this.bucketCount; i++) {
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
        for (int i = 0; i < this.bucketCount; i++) {
            pw.print("b,");
            pw.print(this.bucketStart[i] / 1000);
            pw.print(',');
            if (this.rxBytes != null) {
                pw.print(this.rxBytes[i]);
            } else {
                pw.print("*");
            }
            pw.print(',');
            if (this.rxPackets != null) {
                pw.print(this.rxPackets[i]);
            } else {
                pw.print("*");
            }
            pw.print(',');
            if (this.txBytes != null) {
                pw.print(this.txBytes[i]);
            } else {
                pw.print("*");
            }
            pw.print(',');
            if (this.txPackets != null) {
                pw.print(this.txPackets[i]);
            } else {
                pw.print("*");
            }
            pw.print(',');
            if (this.operations != null) {
                pw.print(this.operations[i]);
            } else {
                pw.print("*");
            }
            pw.println();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long tag) {
        long start = proto.start(tag);
        proto.write(MessageProto.WHEN, this.bucketDuration);
        for (int i = 0; i < this.bucketCount; i++) {
            long startBucket = proto.start(2272037699586L);
            proto.write(MessageProto.WHEN, this.bucketStart[i]);
            writeToProto(proto, LooperProto.THREAD_ID, this.rxBytes, i);
            writeToProto(proto, 1116691496963L, this.rxPackets, i);
            writeToProto(proto, 1116691496964L, this.txBytes, i);
            writeToProto(proto, 1116691496965L, this.txPackets, i);
            writeToProto(proto, 1116691496966L, this.operations, i);
            proto.end(startBucket);
        }
        proto.end(start);
    }

    private static void writeToProto(ProtoOutputStream proto, long tag, long[] array, int index) {
        if (array != null) {
            proto.write(tag, array[index]);
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
