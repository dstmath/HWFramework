package android.net;

import android.app.AlarmClockInfoProto;
import android.bluetooth.BluetoothHidDevice;
import android.content.ClipDescriptionProto;
import android.net.NetworkStats;
import android.os.Parcel;
import android.os.Parcelable;
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
import libcore.util.EmptyArray;

public class NetworkStatsHistory implements Parcelable {
    public static final Parcelable.Creator<NetworkStatsHistory> CREATOR = new Parcelable.Creator<NetworkStatsHistory>() {
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
    public static final int FIELD_RX_BYTES_MP = 64;
    public static final int FIELD_RX_PACKETS = 4;
    public static final int FIELD_RX_PACKETS_MP = 128;
    public static final int FIELD_TX_BYTES = 8;
    public static final int FIELD_TX_BYTES_MP = 256;
    public static final int FIELD_TX_PACKETS = 16;
    public static final int FIELD_TX_PACKETS_MP = 512;
    private static final int HW_VERSION_ADD_MULTIPATH = 11;
    private static final int VERSION_ADD_ACTIVE = 3;
    private static final int VERSION_ADD_PACKETS = 2;
    private static final int VERSION_INIT = 1;
    private long[] activeTime;
    private int bucketCount;
    private long bucketDuration;
    private long[] bucketStart;
    private long[] operations;
    private long[] rxBytes;
    private long[] rxBytes_mp;
    private long[] rxPackets;
    private long[] rxPackets_mp;
    private long totalBytes;
    private long totalRxBytes;
    private long totalTxBytes;
    private long[] txBytes;
    private long[] txBytes_mp;
    private long[] txPackets;
    private long[] txPackets_mp;

    public static class DataStreamUtils {
        @Deprecated
        public static long[] readFullLongArray(DataInputStream in) throws IOException {
            int size = in.readInt();
            if (size >= 0) {
                long[] values = new long[size];
                for (int i = 0; i < values.length; i++) {
                    values[i] = in.readLong();
                }
                return values;
            }
            throw new ProtocolException("negative array size");
        }

        public static long readVarLong(DataInputStream in) throws IOException {
            long result = 0;
            for (int shift = 0; shift < 64; shift += 7) {
                byte b = in.readByte();
                result |= ((long) (b & Byte.MAX_VALUE)) << shift;
                if ((b & BluetoothHidDevice.SUBCLASS1_MOUSE) == 0) {
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
            if (size >= 0) {
                long[] values = new long[size];
                for (int i = 0; i < values.length; i++) {
                    values[i] = readVarLong(in);
                }
                return values;
            }
            throw new ProtocolException("negative array size");
        }

        public static void writeVarLongArray(DataOutputStream out, long[] values, int size) throws IOException {
            if (values == null) {
                out.writeInt(-1);
            } else if (size <= values.length) {
                out.writeInt(size);
                for (int i = 0; i < size; i++) {
                    writeVarLong(out, values[i]);
                }
            } else {
                throw new IllegalArgumentException("size larger than length");
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
        public long rxBytes_mp;
        public long rxPackets;
        public long rxPackets_mp;
        public long txBytes;
        public long txBytes_mp;
        public long txPackets;
        public long txPackets_mp;

        public String toString() {
            return "bucketDuration=" + this.bucketDuration + " bucketStart=" + this.bucketStart + " activeTime=" + this.activeTime + " rxBytes=" + this.rxBytes + " rxPackets=" + this.rxPackets + " txBytes=" + this.txBytes + " txPackets=" + this.txPackets + " operations=" + this.operations + " rxBytes_mp=" + this.rxBytes_mp + " rxPackets_mp=" + this.rxPackets_mp + " txBytes_mp=" + this.txBytes_mp + " txPackets_mp=" + this.txPackets_mp;
        }
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
            } else if (size <= values.length) {
                out.writeInt(size);
                for (int i = 0; i < size; i++) {
                    out.writeLong(values[i]);
                }
            } else {
                throw new IllegalArgumentException("size larger than length");
            }
        }
    }

    public NetworkStatsHistory(long bucketDuration2) {
        this(bucketDuration2, 10, -1);
    }

    public NetworkStatsHistory(long bucketDuration2, int initialSize) {
        this(bucketDuration2, initialSize, -1);
    }

    public NetworkStatsHistory(long bucketDuration2, int initialSize, int fields) {
        this.bucketDuration = bucketDuration2;
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
        if ((fields & 64) != 0) {
            this.rxBytes_mp = new long[initialSize];
        }
        if ((fields & 128) != 0) {
            this.rxPackets_mp = new long[initialSize];
        }
        if ((fields & 256) != 0) {
            this.txBytes_mp = new long[initialSize];
        }
        if ((fields & 512) != 0) {
            this.txPackets_mp = new long[initialSize];
        }
        this.bucketCount = 0;
        this.totalBytes = 0;
        this.totalTxBytes = 0;
        this.totalRxBytes = 0;
    }

    public NetworkStatsHistory(NetworkStatsHistory existing, long bucketDuration2) {
        this(bucketDuration2, existing.estimateResizeBuckets(bucketDuration2));
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
        this.rxBytes_mp = ParcelUtils.readLongArray(in);
        this.rxPackets_mp = ParcelUtils.readLongArray(in);
        this.txBytes_mp = ParcelUtils.readLongArray(in);
        this.txPackets_mp = ParcelUtils.readLongArray(in);
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
        ParcelUtils.writeLongArray(out, this.rxBytes_mp, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.rxPackets_mp, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.txBytes_mp, this.bucketCount);
        ParcelUtils.writeLongArray(out, this.txPackets_mp, this.bucketCount);
    }

    public NetworkStatsHistory(DataInputStream in) throws IOException {
        long[] jArr;
        int version = in.readInt();
        if (version != 11) {
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
                    this.bucketDuration = in.readLong();
                    this.bucketStart = DataStreamUtils.readVarLongArray(in);
                    if (version >= 3) {
                        jArr = DataStreamUtils.readVarLongArray(in);
                    } else {
                        jArr = new long[this.bucketStart.length];
                    }
                    this.activeTime = jArr;
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
        } else {
            this.bucketDuration = in.readLong();
            this.bucketStart = DataStreamUtils.readVarLongArray(in);
            this.activeTime = DataStreamUtils.readVarLongArray(in);
            this.rxBytes = DataStreamUtils.readVarLongArray(in);
            this.rxPackets = DataStreamUtils.readVarLongArray(in);
            this.txBytes = DataStreamUtils.readVarLongArray(in);
            this.txPackets = DataStreamUtils.readVarLongArray(in);
            this.operations = DataStreamUtils.readVarLongArray(in);
            this.bucketCount = this.bucketStart.length;
            this.totalBytes = ArrayUtils.total(this.rxBytes) + ArrayUtils.total(this.txBytes);
            this.totalTxBytes = ArrayUtils.total(this.txBytes);
            this.totalRxBytes = ArrayUtils.total(this.rxBytes);
            this.rxBytes_mp = DataStreamUtils.readVarLongArray(in);
            this.rxPackets_mp = DataStreamUtils.readVarLongArray(in);
            this.txBytes_mp = DataStreamUtils.readVarLongArray(in);
            this.txPackets_mp = DataStreamUtils.readVarLongArray(in);
        }
        if (this.rxBytes == null || this.rxPackets == null || this.txBytes == null || this.txPackets == null || this.operations == null) {
            throw new ProtocolException("Invalid input values");
        } else if (this.bucketStart.length != this.bucketCount || this.rxBytes.length != this.bucketCount || this.rxPackets.length != this.bucketCount || this.txBytes.length != this.bucketCount || this.txPackets.length != this.bucketCount || this.operations.length != this.bucketCount) {
            throw new ProtocolException("Mismatched history lengths");
        }
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(11);
        out.writeLong(this.bucketDuration);
        DataStreamUtils.writeVarLongArray(out, this.bucketStart, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.activeTime, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxBytes, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxPackets, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txBytes, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txPackets, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.operations, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxBytes_mp, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.rxPackets_mp, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txBytes_mp, this.bucketCount);
        DataStreamUtils.writeVarLongArray(out, this.txPackets_mp, this.bucketCount);
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
        int index;
        int index2 = Arrays.binarySearch(this.bucketStart, 0, this.bucketCount, time);
        if (index2 < 0) {
            index = (~index2) - 1;
        } else {
            index = index2 - 1;
        }
        return MathUtils.constrain(index, 0, this.bucketCount - 1);
    }

    public int getIndexAfter(long time) {
        int index;
        int index2 = Arrays.binarySearch(this.bucketStart, 0, this.bucketCount, time);
        if (index2 < 0) {
            index = ~index2;
        } else {
            index = index2 + 1;
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
        entry.rxBytes_mp = getLong(this.rxBytes_mp, i, -1);
        entry.rxPackets_mp = getLong(this.rxPackets_mp, i, -1);
        entry.txBytes_mp = getLong(this.txBytes_mp, i, -1);
        entry.txPackets_mp = getLong(this.txPackets_mp, i, -1);
        return entry;
    }

    public void setValues(int i, Entry entry) {
        if (this.rxBytes != null) {
            this.totalBytes -= this.rxBytes[i];
        }
        if (this.txBytes != null) {
            this.totalBytes -= this.txBytes[i];
        }
        this.bucketStart[i] = entry.bucketStart;
        setLong(this.activeTime, i, entry.activeTime);
        setLong(this.rxBytes, i, entry.rxBytes);
        setLong(this.rxPackets, i, entry.rxPackets);
        setLong(this.txBytes, i, entry.txBytes);
        setLong(this.txPackets, i, entry.txPackets);
        setLong(this.operations, i, entry.operations);
        setLong(this.rxBytes_mp, i, entry.rxBytes_mp);
        setLong(this.rxPackets_mp, i, entry.rxPackets_mp);
        setLong(this.txBytes_mp, i, entry.txBytes_mp);
        setLong(this.txPackets_mp, i, entry.txPackets_mp);
        if (this.rxBytes != null) {
            this.totalBytes += this.rxBytes[i];
        }
        if (this.txBytes != null) {
            this.totalBytes += this.txBytes[i];
        }
    }

    @Deprecated
    public void recordData(long start, long end, long rxBytes2, long txBytes2) {
        NetworkStats.Entry entry = new NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, rxBytes2, 0, txBytes2, 0, 0);
        recordData(start, end, entry);
    }

    public void recordData(long start, long end, NetworkStats.Entry entry) {
        int i;
        long overlap;
        long duration = start;
        long j = end;
        NetworkStats.Entry entry2 = entry;
        long rxBytes2 = entry2.rxBytes;
        long rxPackets2 = entry2.rxPackets;
        long txBytes2 = entry2.txBytes;
        long txPackets2 = entry2.txPackets;
        long operations2 = entry2.operations;
        long rxBytes3 = rxBytes2;
        long rxBytes_mp2 = entry2.rxBytes_mp;
        long rxPackets_mp2 = entry2.rxPackets_mp;
        long txBytes_mp2 = entry2.txBytes_mp;
        long txPackets_mp2 = entry2.txPackets_mp;
        if (entry.isNegative()) {
            long j2 = txPackets_mp2;
            Log.d("NetworkStatsHistory", "tried recording negative data.");
            return;
        }
        long txPackets_mp3 = txPackets_mp2;
        if (!entry.isEmpty()) {
            ensureBuckets(start, end);
            long txPackets_mp4 = txPackets_mp3;
            long txBytes_mp3 = txBytes_mp2;
            long rxPackets_mp3 = rxPackets_mp2;
            long rxBytes_mp3 = rxBytes_mp2;
            long operations3 = operations2;
            long fracTxBytes = txPackets2;
            long txBytes3 = txBytes2;
            long rxPackets3 = rxPackets2;
            long duration2 = j - duration;
            int i2 = getIndexAfter(j);
            while (true) {
                if (i2 < 0) {
                    long j3 = duration2;
                    long j4 = txBytes3;
                    long j5 = fracTxBytes;
                    break;
                }
                long txPackets3 = fracTxBytes;
                long txPackets4 = this.bucketStart[i2];
                int i3 = i2;
                long curEnd = this.bucketDuration + txPackets4;
                if (curEnd < duration) {
                    long j6 = duration2;
                    long j7 = txBytes3;
                    break;
                }
                if (txPackets4 <= j) {
                    long overlap2 = Math.min(curEnd, j) - Math.max(txPackets4, duration);
                    if (overlap2 > 0) {
                        long fracRxBytes = (rxBytes3 * overlap2) / duration2;
                        long j8 = curEnd;
                        long fracRxPackets = (rxPackets3 * overlap2) / duration2;
                        long j9 = txPackets4;
                        long curStart = (txBytes3 * overlap2) / duration2;
                        long txBytes4 = txBytes3;
                        long fracTxPackets = (txPackets3 * overlap2) / duration2;
                        long fracOperations = (operations3 * overlap2) / duration2;
                        long fracRxBytes_mp = (rxBytes_mp3 * overlap2) / duration2;
                        long fracRxPackets_mp = (rxPackets_mp3 * overlap2) / duration2;
                        long fracTxBytes_mp = (txBytes_mp3 * overlap2) / duration2;
                        long fracTxBytes_mp2 = (txPackets_mp4 * overlap2) / duration2;
                        long duration3 = duration2;
                        i = i3;
                        addLong(this.activeTime, i, overlap2);
                        addLong(this.rxBytes, i, fracRxBytes);
                        rxBytes3 -= fracRxBytes;
                        addLong(this.rxPackets, i, fracRxPackets);
                        rxPackets3 -= fracRxPackets;
                        addLong(this.txBytes, i, curStart);
                        long txBytes5 = txBytes4 - curStart;
                        long j10 = fracRxBytes;
                        long fracRxBytes2 = fracTxPackets;
                        addLong(this.txPackets, i, fracRxBytes2);
                        long txPackets5 = txPackets3 - fracRxBytes2;
                        long j11 = fracRxBytes2;
                        long fracOperations2 = fracOperations;
                        addLong(this.operations, i, fracOperations2);
                        operations3 -= fracOperations2;
                        long j12 = fracOperations2;
                        long fracRxBytes_mp2 = fracRxBytes_mp;
                        addLong(this.rxBytes_mp, i, fracRxBytes_mp2);
                        rxBytes_mp3 -= fracRxBytes_mp2;
                        long j13 = fracRxBytes_mp2;
                        long fracRxPackets_mp2 = fracRxPackets_mp;
                        addLong(this.rxPackets_mp, i, fracRxPackets_mp2);
                        rxPackets_mp3 -= fracRxPackets_mp2;
                        long j14 = fracRxPackets_mp2;
                        long fracTxBytes_mp3 = fracTxBytes_mp;
                        addLong(this.txBytes_mp, i, fracTxBytes_mp3);
                        txBytes_mp3 -= fracTxBytes_mp3;
                        addLong(this.txPackets_mp, i, fracTxBytes_mp2);
                        txPackets_mp4 -= fracTxBytes_mp2;
                        overlap = duration3 - overlap2;
                        fracTxBytes = txPackets5;
                        txBytes3 = txBytes5;
                        i2 = i - 1;
                        duration2 = overlap;
                        duration = start;
                        j = end;
                        NetworkStats.Entry entry3 = entry;
                    }
                }
                overlap = duration2;
                fracTxBytes = txPackets3;
                i = i3;
                i2 = i - 1;
                duration2 = overlap;
                duration = start;
                j = end;
                NetworkStats.Entry entry32 = entry;
            }
            NetworkStats.Entry entry4 = entry;
            this.totalBytes += entry4.rxBytes + entry4.txBytes;
            this.totalRxBytes += entry4.rxBytes;
            this.totalTxBytes += entry4.txBytes;
        }
    }

    public void recordEntireHistory(NetworkStatsHistory input) {
        recordHistory(input, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public void recordHistory(NetworkStatsHistory input, long start, long end) {
        NetworkStatsHistory networkStatsHistory = input;
        NetworkStats.Entry entry = new NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        for (int i = 0; i < networkStatsHistory.bucketCount; i++) {
            long bucketStart2 = networkStatsHistory.bucketStart[i];
            long bucketEnd = networkStatsHistory.bucketDuration + bucketStart2;
            if (bucketStart2 >= start && bucketEnd <= end) {
                entry.rxBytes = getLong(networkStatsHistory.rxBytes, i, 0);
                entry.rxPackets = getLong(networkStatsHistory.rxPackets, i, 0);
                entry.txBytes = getLong(networkStatsHistory.txBytes, i, 0);
                entry.txPackets = getLong(networkStatsHistory.txPackets, i, 0);
                entry.operations = getLong(networkStatsHistory.operations, i, 0);
                entry.rxBytes_mp = getLong(networkStatsHistory.rxBytes_mp, i, 0);
                entry.rxPackets_mp = getLong(networkStatsHistory.rxPackets_mp, i, 0);
                entry.txBytes_mp = getLong(networkStatsHistory.txBytes_mp, i, 0);
                entry.txPackets_mp = getLong(networkStatsHistory.txPackets_mp, i, 0);
                recordData(bucketStart2, bucketEnd, entry);
            }
        }
    }

    private void ensureBuckets(long start, long end) {
        long start2 = start - (start % this.bucketDuration);
        long end2 = end + ((this.bucketDuration - (end % this.bucketDuration)) % this.bucketDuration);
        long now = start2;
        while (now < end2) {
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
            if (this.rxBytes_mp != null) {
                this.rxBytes_mp = Arrays.copyOf(this.rxBytes_mp, newLength);
            }
            if (this.rxPackets_mp != null) {
                this.rxPackets_mp = Arrays.copyOf(this.rxPackets_mp, newLength);
            }
            if (this.txBytes_mp != null) {
                this.txBytes_mp = Arrays.copyOf(this.txBytes_mp, newLength);
            }
            if (this.txPackets_mp != null) {
                this.txPackets_mp = Arrays.copyOf(this.txPackets_mp, newLength);
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
            if (this.rxBytes_mp != null) {
                System.arraycopy(this.rxBytes_mp, index, this.rxBytes_mp, dstPos, length);
            }
            if (this.rxPackets_mp != null) {
                System.arraycopy(this.rxPackets_mp, index, this.rxPackets_mp, dstPos, length);
            }
            if (this.txBytes_mp != null) {
                System.arraycopy(this.txBytes_mp, index, this.txBytes_mp, dstPos, length);
            }
            if (this.txPackets_mp != null) {
                System.arraycopy(this.txPackets_mp, index, this.txPackets_mp, dstPos, length);
            }
        }
        this.bucketStart[index] = start;
        setLong(this.activeTime, index, 0);
        setLong(this.rxBytes, index, 0);
        setLong(this.rxPackets, index, 0);
        setLong(this.txBytes, index, 0);
        setLong(this.txPackets, index, 0);
        setLong(this.operations, index, 0);
        setLong(this.rxBytes_mp, index, 0);
        setLong(this.rxPackets_mp, index, 0);
        setLong(this.txBytes_mp, index, 0);
        setLong(this.txPackets_mp, index, 0);
        this.bucketCount++;
    }

    public void clear() {
        this.bucketStart = EmptyArray.LONG;
        if (this.activeTime != null) {
            this.activeTime = EmptyArray.LONG;
        }
        if (this.rxBytes != null) {
            this.rxBytes = EmptyArray.LONG;
        }
        if (this.rxPackets != null) {
            this.rxPackets = EmptyArray.LONG;
        }
        if (this.txBytes != null) {
            this.txBytes = EmptyArray.LONG;
        }
        if (this.txPackets != null) {
            this.txPackets = EmptyArray.LONG;
        }
        if (this.operations != null) {
            this.operations = EmptyArray.LONG;
        }
        if (this.rxBytes_mp != null) {
            this.rxBytes_mp = EmptyArray.LONG;
        }
        if (this.rxPackets_mp != null) {
            this.rxPackets_mp = EmptyArray.LONG;
        }
        if (this.txBytes_mp != null) {
            this.txBytes_mp = EmptyArray.LONG;
        }
        if (this.txPackets_mp != null) {
            this.txPackets_mp = EmptyArray.LONG;
        }
        this.bucketCount = 0;
        this.totalBytes = 0;
    }

    @Deprecated
    public void removeBucketsBefore(long cutoff) {
        int i = 0;
        while (i < this.bucketCount) {
            if (this.bucketDuration + this.bucketStart[i] > cutoff) {
                break;
            }
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
            if (this.rxBytes_mp != null) {
                this.rxBytes_mp = Arrays.copyOfRange(this.rxBytes_mp, i, length);
            }
            if (this.rxPackets_mp != null) {
                this.rxPackets_mp = Arrays.copyOfRange(this.rxPackets_mp, i, length);
            }
            if (this.txBytes_mp != null) {
                this.txBytes_mp = Arrays.copyOfRange(this.txBytes_mp, i, length);
            }
            if (this.txPackets_mp != null) {
                this.txPackets_mp = Arrays.copyOfRange(this.txPackets_mp, i, length);
            }
            this.bucketCount -= i;
        }
    }

    public Entry getValues(long start, long end, Entry recycle) {
        return getValues(start, end, Long.MAX_VALUE, recycle);
    }

    public Entry getValues(long start, long end, long now, Entry recycle) {
        long overlapEnd;
        long j = start;
        long j2 = end;
        Entry entry = recycle != null ? recycle : new Entry();
        entry.bucketDuration = j2 - j;
        entry.bucketStart = j;
        long j3 = -1;
        entry.activeTime = this.activeTime != null ? 0 : -1;
        entry.rxBytes = this.rxBytes != null ? 0 : -1;
        entry.rxPackets = this.rxPackets != null ? 0 : -1;
        entry.txBytes = this.txBytes != null ? 0 : -1;
        entry.txPackets = this.txPackets != null ? 0 : -1;
        entry.operations = this.operations != null ? 0 : -1;
        entry.rxBytes_mp = this.rxBytes_mp != null ? 0 : -1;
        entry.rxPackets_mp = this.rxPackets_mp != null ? 0 : -1;
        entry.txBytes_mp = this.txBytes_mp != null ? 0 : -1;
        if (this.txPackets_mp != null) {
            j3 = 0;
        }
        entry.txPackets_mp = j3;
        int i = getIndexAfter(j2);
        while (i >= 0) {
            long curStart = this.bucketStart[i];
            long curEnd = curStart + this.bucketDuration;
            if (curEnd <= j) {
                break;
            }
            if (curStart < j2) {
                if (curStart < now && curEnd > now) {
                    overlapEnd = this.bucketDuration;
                } else {
                    overlapEnd = (curEnd < j2 ? curEnd : j2) - (curStart > j ? curStart : j);
                }
                if (overlapEnd > 0) {
                    if (this.activeTime != null) {
                        long j4 = curEnd;
                        entry.activeTime += (this.activeTime[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.rxBytes != null) {
                        entry.rxBytes += (this.rxBytes[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.rxPackets != null) {
                        entry.rxPackets += (this.rxPackets[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.txBytes != null) {
                        entry.txBytes += (this.txBytes[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.txPackets != null) {
                        entry.txPackets += (this.txPackets[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.operations != null) {
                        entry.operations += (this.operations[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.rxBytes_mp != null) {
                        entry.rxBytes_mp += (this.rxBytes_mp[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.rxPackets_mp != null) {
                        entry.rxPackets_mp += (this.rxPackets_mp[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.txBytes_mp != null) {
                        entry.txBytes_mp += (this.txBytes_mp[i] * overlapEnd) / this.bucketDuration;
                    }
                    if (this.txPackets_mp != null) {
                        entry.txPackets_mp += (this.txPackets_mp[i] * overlapEnd) / this.bucketDuration;
                    }
                }
            }
            i--;
            j = start;
            j2 = end;
        }
        return entry;
    }

    @Deprecated
    public void generateRandom(long start, long end, long bytes) {
        long j = bytes;
        Random r = new Random();
        float fractionRx = r.nextFloat();
        long rxBytes2 = (long) (((float) j) * fractionRx);
        long txBytes2 = (long) (((float) j) * (1.0f - fractionRx));
        long j2 = txBytes2;
        long j3 = rxBytes2;
        generateRandom(start, end, rxBytes2, rxBytes2 / 1024, txBytes2, txBytes2 / 1024, rxBytes2 / 2048, r);
    }

    @Deprecated
    public void generateRandom(long start, long end, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2, Random r) {
        long j = end;
        Random random = r;
        ensureBuckets(start, end);
        NetworkStats.Entry entry = new NetworkStats.Entry(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        long rxBytes3 = rxBytes2;
        long rxPackets3 = rxPackets2;
        long txBytes3 = txBytes2;
        long txPackets3 = txPackets2;
        long operations3 = operations2;
        while (true) {
            NetworkStats.Entry entry2 = entry;
            if (rxBytes3 > 1024 || rxPackets3 > 128 || txBytes3 > 1024 || txPackets3 > 128 || operations3 > 32) {
                long curStart = randomLong(random, start, j);
                long curEnd = curStart + randomLong(random, 0, (j - curStart) / 2);
                entry2.rxBytes = randomLong(random, 0, rxBytes3);
                entry2.rxPackets = randomLong(random, 0, rxPackets3);
                entry2.txBytes = randomLong(random, 0, txBytes3);
                entry2.txPackets = randomLong(random, 0, txPackets3);
                entry2.operations = randomLong(random, 0, operations3);
                rxBytes3 -= entry2.rxBytes;
                rxPackets3 -= entry2.rxPackets;
                txBytes3 -= entry2.txBytes;
                txPackets3 -= entry2.txPackets;
                operations3 -= entry2.operations;
                recordData(curStart, curEnd, entry2);
                entry = entry2;
                j = end;
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
        int start = 0;
        if (!fullHistory) {
            start = Math.max(0, this.bucketCount - 32);
        }
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
            if (this.rxBytes_mp != null) {
                pw.print(" rb=");
                pw.print(this.rxBytes_mp[i]);
            }
            if (this.rxPackets_mp != null) {
                pw.print(" rp=");
                pw.print(this.rxPackets_mp[i]);
            }
            if (this.txBytes_mp != null) {
                pw.print(" tb=");
                pw.print(this.txBytes_mp[i]);
            }
            if (this.txPackets_mp != null) {
                pw.print(" tp=");
                pw.print(this.txPackets_mp[i]);
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
        proto.write(AlarmClockInfoProto.TRIGGER_TIME_MS, this.bucketDuration);
        for (int i = 0; i < this.bucketCount; i++) {
            long startBucket = proto.start(2246267895810L);
            proto.write(AlarmClockInfoProto.TRIGGER_TIME_MS, this.bucketStart[i]);
            writeToProto(proto, 1112396529666L, this.rxBytes, i);
            writeToProto(proto, 1112396529667L, this.rxPackets, i);
            writeToProto(proto, ClipDescriptionProto.TIMESTAMP_MS, this.txBytes, i);
            writeToProto(proto, 1112396529669L, this.txPackets, i);
            writeToProto(proto, 1112396529670L, this.operations, i);
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
