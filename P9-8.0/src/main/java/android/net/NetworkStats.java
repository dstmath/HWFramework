package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.util.ArrayUtils;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import libcore.util.EmptyArray;

public class NetworkStats implements Parcelable {
    public static final Creator<NetworkStats> CREATOR = new Creator<NetworkStats>() {
        public NetworkStats createFromParcel(Parcel in) {
            return new NetworkStats(in);
        }

        public NetworkStats[] newArray(int size) {
            return new NetworkStats[size];
        }
    };
    public static final String IFACE_ALL = null;
    public static final int METERED_ALL = -1;
    public static final int METERED_NO = 0;
    public static final int METERED_YES = 1;
    public static final int ROAMING_ALL = -1;
    public static final int ROAMING_NO = 0;
    public static final int ROAMING_YES = 1;
    public static final int SET_ALL = -1;
    public static final int SET_DBG_VPN_IN = 1001;
    public static final int SET_DBG_VPN_OUT = 1002;
    public static final int SET_DEBUG_START = 1000;
    public static final int SET_DEFAULT = 0;
    public static final int SET_FOREGROUND = 1;
    private static final String TAG = "NetworkStats";
    public static final int TAG_ALL = -1;
    public static final int TAG_NONE = 0;
    public static final int UID_ALL = -1;
    private int[] actUid;
    private int capacity;
    private long elapsedRealtime;
    private String[] iface;
    private int[] metered;
    private long[] operations;
    private String[] proc;
    private int[] roaming;
    private long[] rxBytes;
    private long[] rxPackets;
    private int[] set;
    private int size;
    private int[] tag;
    private long[] txBytes;
    private long[] txPackets;
    private int[] uid;

    public static class Entry {
        public int actUid;
        public String iface;
        public int metered;
        public long operations;
        public String proc;
        public int roaming;
        public long rxBytes;
        public long rxPackets;
        public int set;
        public int tag;
        public long txBytes;
        public long txPackets;
        public int uid;

        public Entry() {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        }

        public Entry(long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, rxBytes, rxPackets, txBytes, txPackets, operations);
        }

        public Entry(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(iface, uid, set, tag, 0, 0, rxBytes, rxPackets, txBytes, txPackets, operations);
        }

        public Entry(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(iface, uid, set, tag, metered, roaming, rxBytes, rxPackets, txBytes, txPackets, operations, ProxyInfo.LOCAL_EXCL_LIST);
        }

        public Entry(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations, String proc) {
            this(iface, uid, set, tag, metered, roaming, rxBytes, rxPackets, txBytes, txPackets, operations, proc, -1);
        }

        public Entry(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations, String proc, int actUid) {
            this.proc = ProxyInfo.LOCAL_EXCL_LIST;
            this.actUid = -1;
            this.iface = iface;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.metered = metered;
            this.roaming = roaming;
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.operations = operations;
            this.proc = proc;
            this.actUid = actUid;
        }

        public boolean isNegative() {
            return this.rxBytes < 0 || this.rxPackets < 0 || this.txBytes < 0 || this.txPackets < 0 || this.operations < 0;
        }

        public boolean isEmpty() {
            if (this.rxBytes == 0 && this.rxPackets == 0 && this.txBytes == 0 && this.txPackets == 0 && this.operations == 0) {
                return true;
            }
            return false;
        }

        public void add(Entry another) {
            this.rxBytes += another.rxBytes;
            this.rxPackets += another.rxPackets;
            this.txBytes += another.txBytes;
            this.txPackets += another.txPackets;
            this.operations += another.operations;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("iface=").append(this.iface);
            builder.append(" uid=").append(this.uid);
            builder.append(" set=").append(NetworkStats.setToString(this.set));
            builder.append(" tag=").append(NetworkStats.tagToString(this.tag));
            builder.append(" metered=").append(NetworkStats.meteredToString(this.metered));
            builder.append(" roaming=").append(NetworkStats.roamingToString(this.roaming));
            builder.append(" rxBytes=").append(this.rxBytes);
            builder.append(" rxPackets=").append(this.rxPackets);
            builder.append(" txBytes=").append(this.txBytes);
            builder.append(" txPackets=").append(this.txPackets);
            builder.append(" operations=").append(this.operations);
            builder.append(" proc=").append(this.proc);
            builder.append(" actUid=").append(this.actUid);
            return builder.toString();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (this.uid == e.uid && this.set == e.set && this.tag == e.tag && this.metered == e.metered && this.roaming == e.roaming && this.rxBytes == e.rxBytes && this.rxPackets == e.rxPackets && this.txBytes == e.txBytes && this.txPackets == e.txPackets && this.operations == e.operations && this.iface.equals(e.iface) && this.proc.equals(e.proc) && this.actUid == e.actUid) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.uid), Integer.valueOf(this.set), Integer.valueOf(this.tag), Integer.valueOf(this.metered), Integer.valueOf(this.roaming), this.iface});
        }
    }

    public interface NonMonotonicObserver<C> {
        void foundNonMonotonic(NetworkStats networkStats, int i, NetworkStats networkStats2, int i2, C c);
    }

    public NetworkStats(long elapsedRealtime, int initialSize) {
        this.elapsedRealtime = elapsedRealtime;
        this.size = 0;
        if (initialSize >= 0) {
            this.capacity = initialSize;
            this.iface = new String[initialSize];
            this.uid = new int[initialSize];
            this.set = new int[initialSize];
            this.tag = new int[initialSize];
            this.metered = new int[initialSize];
            this.roaming = new int[initialSize];
            this.rxBytes = new long[initialSize];
            this.rxPackets = new long[initialSize];
            this.txBytes = new long[initialSize];
            this.txPackets = new long[initialSize];
            this.operations = new long[initialSize];
            this.proc = new String[initialSize];
            this.actUid = new int[initialSize];
            return;
        }
        this.capacity = 0;
        this.iface = EmptyArray.STRING;
        this.uid = EmptyArray.INT;
        this.set = EmptyArray.INT;
        this.tag = EmptyArray.INT;
        this.metered = EmptyArray.INT;
        this.roaming = EmptyArray.INT;
        this.rxBytes = EmptyArray.LONG;
        this.rxPackets = EmptyArray.LONG;
        this.txBytes = EmptyArray.LONG;
        this.txPackets = EmptyArray.LONG;
        this.operations = EmptyArray.LONG;
        this.proc = EmptyArray.STRING;
        this.actUid = EmptyArray.INT;
    }

    public NetworkStats(Parcel parcel) {
        this.elapsedRealtime = parcel.readLong();
        this.size = parcel.readInt();
        this.capacity = parcel.readInt();
        this.iface = parcel.createStringArray();
        this.uid = parcel.createIntArray();
        this.set = parcel.createIntArray();
        this.tag = parcel.createIntArray();
        this.metered = parcel.createIntArray();
        this.roaming = parcel.createIntArray();
        this.rxBytes = parcel.createLongArray();
        this.rxPackets = parcel.createLongArray();
        this.txBytes = parcel.createLongArray();
        this.txPackets = parcel.createLongArray();
        this.operations = parcel.createLongArray();
        this.proc = parcel.createStringArray();
        this.actUid = parcel.createIntArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.elapsedRealtime);
        dest.writeInt(this.size);
        dest.writeInt(this.capacity);
        dest.writeStringArray(this.iface);
        dest.writeIntArray(this.uid);
        dest.writeIntArray(this.set);
        dest.writeIntArray(this.tag);
        dest.writeIntArray(this.metered);
        dest.writeIntArray(this.roaming);
        dest.writeLongArray(this.rxBytes);
        dest.writeLongArray(this.rxPackets);
        dest.writeLongArray(this.txBytes);
        dest.writeLongArray(this.txPackets);
        dest.writeLongArray(this.operations);
        dest.writeStringArray(this.proc);
        dest.writeIntArray(this.actUid);
    }

    public NetworkStats clone() {
        NetworkStats clone = new NetworkStats(this.elapsedRealtime, this.size);
        Entry entry = null;
        for (int i = 0; i < this.size; i++) {
            entry = getValues(i, entry);
            clone.addValues(entry);
        }
        return clone;
    }

    public NetworkStats addIfaceValues(String iface, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        return addValues(iface, -1, 0, 0, rxBytes, rxPackets, txBytes, txPackets, 0);
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, int metered, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, metered, roaming, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats addValues(Entry entry) {
        if (this.size >= this.capacity) {
            int newLength = (Math.max(this.size, 10) * 3) / 2;
            this.iface = (String[]) Arrays.copyOf(this.iface, newLength);
            this.uid = Arrays.copyOf(this.uid, newLength);
            this.set = Arrays.copyOf(this.set, newLength);
            this.tag = Arrays.copyOf(this.tag, newLength);
            this.metered = Arrays.copyOf(this.metered, newLength);
            this.roaming = Arrays.copyOf(this.roaming, newLength);
            this.rxBytes = Arrays.copyOf(this.rxBytes, newLength);
            this.rxPackets = Arrays.copyOf(this.rxPackets, newLength);
            this.txBytes = Arrays.copyOf(this.txBytes, newLength);
            this.txPackets = Arrays.copyOf(this.txPackets, newLength);
            this.operations = Arrays.copyOf(this.operations, newLength);
            this.proc = (String[]) Arrays.copyOf(this.proc, newLength);
            this.actUid = Arrays.copyOf(this.actUid, newLength);
            this.capacity = newLength;
        }
        this.iface[this.size] = entry.iface;
        this.uid[this.size] = entry.uid;
        this.set[this.size] = entry.set;
        this.tag[this.size] = entry.tag;
        this.metered[this.size] = entry.metered;
        this.roaming[this.size] = entry.roaming;
        this.rxBytes[this.size] = entry.rxBytes;
        this.rxPackets[this.size] = entry.rxPackets;
        this.txBytes[this.size] = entry.txBytes;
        this.txPackets[this.size] = entry.txPackets;
        this.operations[this.size] = entry.operations;
        this.proc[this.size] = entry.proc;
        this.actUid[this.size] = entry.actUid;
        this.size++;
        return this;
    }

    public Entry getValues(int i, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = this.iface[i];
        entry.uid = this.uid[i];
        entry.set = this.set[i];
        entry.tag = this.tag[i];
        entry.metered = this.metered[i];
        entry.roaming = this.roaming[i];
        entry.rxBytes = this.rxBytes[i];
        entry.rxPackets = this.rxPackets[i];
        entry.txBytes = this.txBytes[i];
        entry.txPackets = this.txPackets[i];
        entry.operations = this.operations[i];
        entry.proc = this.proc[i];
        entry.actUid = this.actUid[i];
        return entry;
    }

    public long getElapsedRealtime() {
        return this.elapsedRealtime;
    }

    public void setElapsedRealtime(long time) {
        this.elapsedRealtime = time;
    }

    public long getElapsedRealtimeAge() {
        return SystemClock.elapsedRealtime() - this.elapsedRealtime;
    }

    public int size() {
        return this.size;
    }

    public int internalSize() {
        return this.capacity;
    }

    @Deprecated
    public NetworkStats combineValues(String iface, int uid, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(iface, uid, 0, tag, rxBytes, rxPackets, txBytes, txPackets, operations);
    }

    public NetworkStats combineValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats combineValues(Entry entry) {
        int i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.proc, entry.actUid);
        if (i == -1) {
            addValues(entry);
        } else {
            long[] jArr = this.rxBytes;
            jArr[i] = jArr[i] + entry.rxBytes;
            jArr = this.rxPackets;
            jArr[i] = jArr[i] + entry.rxPackets;
            jArr = this.txBytes;
            jArr[i] = jArr[i] + entry.txBytes;
            jArr = this.txPackets;
            jArr[i] = jArr[i] + entry.txPackets;
            jArr = this.operations;
            jArr[i] = jArr[i] + entry.operations;
        }
        return this;
    }

    public void combineAllValues(NetworkStats another) {
        Entry entry = null;
        for (int i = 0; i < another.size; i++) {
            entry = another.getValues(i, entry);
            combineValues(entry);
        }
    }

    public int findIndex(String iface, int uid, int set, int tag, int metered, int roaming) {
        int i = 0;
        while (i < this.size) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int findIndex(String iface, int uid, int set, int tag, int metered, int roaming, String proc, int actUid) {
        int i = 0;
        while (i < this.size) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i]) && Objects.equals(proc, this.proc[i]) && actUid == this.actUid[i]) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int findIndexHinted(String iface, int uid, int set, int tag, int metered, int roaming, String proc, int actUid, int hintIndex) {
        for (int offset = 0; offset < this.size; offset++) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) - 1) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i]) && Objects.equals(proc, this.proc[i]) && actUid == this.actUid[i]) {
                return i;
            }
        }
        return -1;
    }

    public int findIndexHinted(String iface, int uid, int set, int tag, int metered, int roaming, int hintIndex) {
        for (int offset = 0; offset < this.size; offset++) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) - 1) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && metered == this.metered[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
        }
        return -1;
    }

    public void spliceOperationsFrom(NetworkStats stats) {
        for (int i = 0; i < this.size; i++) {
            int j = stats.findIndex(this.iface[i], this.uid[i], this.set[i], this.tag[i], this.metered[i], this.roaming[i], this.proc[i], this.actUid[i]);
            if (j == -1) {
                this.operations[i] = 0;
            } else {
                this.operations[i] = stats.operations[j];
            }
        }
    }

    public String[] getUniqueIfaces() {
        HashSet<String> ifaces = new HashSet();
        for (String iface : this.iface) {
            if (iface != IFACE_ALL) {
                ifaces.add(iface);
            }
        }
        return (String[]) ifaces.toArray(new String[ifaces.size()]);
    }

    public int[] getUniqueUids() {
        SparseBooleanArray uids = new SparseBooleanArray();
        for (int uid : this.uid) {
            uids.put(uid, true);
        }
        int size = uids.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = uids.keyAt(i);
        }
        return result;
    }

    public long getTotalBytes() {
        Entry entry = getTotal(null);
        return entry.rxBytes + entry.txBytes;
    }

    public Entry getTotal(Entry recycle) {
        return getTotal(recycle, null, -1, false);
    }

    public Entry getTotal(Entry recycle, int limitUid) {
        return getTotal(recycle, null, limitUid, false);
    }

    public Entry getTotal(Entry recycle, HashSet<String> limitIface) {
        return getTotal(recycle, limitIface, -1, false);
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        return getTotal(recycle, null, -1, true);
    }

    private Entry getTotal(Entry recycle, HashSet<String> limitIface, int limitUid, boolean includeTags) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = IFACE_ALL;
        entry.uid = limitUid;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        entry.rxBytes = 0;
        entry.rxPackets = 0;
        entry.txBytes = 0;
        entry.txPackets = 0;
        entry.operations = 0;
        int i = 0;
        while (i < this.size) {
            boolean matchesUid = limitUid == -1 || limitUid == this.uid[i];
            boolean matchesIface = limitIface != null ? limitIface.contains(this.iface[i]) : true;
            if (matchesUid && matchesIface && (this.tag[i] == 0 || (includeTags ^ 1) == 0)) {
                entry.rxBytes += this.rxBytes[i];
                entry.rxPackets += this.rxPackets[i];
                entry.txBytes += this.txBytes[i];
                entry.txPackets += this.txPackets[i];
                entry.operations += this.operations[i];
            }
            i++;
        }
        return entry;
    }

    public long getTotalPackets() {
        long total = 0;
        for (int i = this.size - 1; i >= 0; i--) {
            total += this.rxPackets[i] + this.txPackets[i];
        }
        return total;
    }

    public NetworkStats subtract(NetworkStats right) {
        return subtract(this, right, null, null);
    }

    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right, NonMonotonicObserver<C> observer, C cookie) {
        return subtract(left, right, observer, cookie, null);
    }

    public static <C> NetworkStats subtract(NetworkStats left, NetworkStats right, NonMonotonicObserver<C> observer, C cookie, NetworkStats recycle) {
        NetworkStats result;
        long deltaRealtime = left.elapsedRealtime - right.elapsedRealtime;
        if (deltaRealtime < 0) {
            if (observer != null) {
                observer.foundNonMonotonic(left, -1, right, -1, cookie);
            }
            deltaRealtime = 0;
        }
        Entry entry = new Entry();
        if (recycle == null || recycle.capacity < left.size) {
            NetworkStats networkStats = new NetworkStats(deltaRealtime, left.size);
        } else {
            result = recycle;
            recycle.size = 0;
            recycle.elapsedRealtime = deltaRealtime;
        }
        for (int i = 0; i < left.size; i++) {
            entry.iface = left.iface[i];
            entry.uid = left.uid[i];
            entry.set = left.set[i];
            entry.tag = left.tag[i];
            entry.metered = left.metered[i];
            entry.roaming = left.roaming[i];
            entry.proc = left.proc[i];
            entry.actUid = left.actUid[i];
            int j = right.findIndexHinted(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.proc, entry.actUid, i);
            if (j == -1) {
                entry.rxBytes = left.rxBytes[i];
                entry.rxPackets = left.rxPackets[i];
                entry.txBytes = left.txBytes[i];
                entry.txPackets = left.txPackets[i];
                entry.operations = left.operations[i];
            } else {
                entry.rxBytes = left.rxBytes[i] - right.rxBytes[j];
                entry.rxPackets = left.rxPackets[i] - right.rxPackets[j];
                entry.txBytes = left.txBytes[i] - right.txBytes[j];
                entry.txPackets = left.txPackets[i] - right.txPackets[j];
                entry.operations = left.operations[i] - right.operations[j];
                if (entry.rxBytes < 0 || entry.rxPackets < 0 || entry.txBytes < 0 || entry.txPackets < 0 || entry.operations < 0) {
                    if (observer != null) {
                        observer.foundNonMonotonic(left, i, right, j, cookie);
                    }
                    entry.rxBytes = Math.max(entry.rxBytes, 0);
                    entry.rxPackets = Math.max(entry.rxPackets, 0);
                    entry.txBytes = Math.max(entry.txBytes, 0);
                    entry.txPackets = Math.max(entry.txPackets, 0);
                    entry.operations = Math.max(entry.operations, 0);
                }
            }
            result.addValues(entry);
        }
        return result;
    }

    public NetworkStats groupedByIface() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        Entry entry = new Entry();
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        entry.operations = 0;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.iface = this.iface[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    public NetworkStats groupedByUid() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        Entry entry = new Entry();
        entry.iface = IFACE_ALL;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.uid = this.uid[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                entry.operations = this.operations[i];
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    public NetworkStats withoutUids(int[] uids) {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        Entry entry = new Entry();
        for (int i = 0; i < this.size; i++) {
            entry = getValues(i, entry);
            if (!ArrayUtils.contains(uids, entry.uid)) {
                stats.addValues(entry);
            }
        }
        return stats;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("NetworkStats: elapsedRealtime=");
        pw.println(this.elapsedRealtime);
        for (int i = 0; i < this.size; i++) {
            pw.print(prefix);
            pw.print("  [");
            pw.print(i);
            pw.print("]");
            pw.print(" iface=");
            pw.print(this.iface[i]);
            pw.print(" uid=");
            pw.print(this.uid[i]);
            pw.print(" set=");
            pw.print(setToString(this.set[i]));
            pw.print(" tag=");
            pw.print(tagToString(this.tag[i]));
            pw.print(" metered=");
            pw.print(meteredToString(this.metered[i]));
            pw.print(" roaming=");
            pw.print(roamingToString(this.roaming[i]));
            pw.print(" rxBytes=");
            pw.print(this.rxBytes[i]);
            pw.print(" rxPackets=");
            pw.print(this.rxPackets[i]);
            pw.print(" txBytes=");
            pw.print(this.txBytes[i]);
            pw.print(" txPackets=");
            pw.print(this.txPackets[i]);
            pw.print(" operations=");
            pw.println(this.operations[i]);
            pw.print(" proc=");
            pw.println(this.proc[i]);
            pw.print(" actuid=");
            pw.println(this.actUid[i]);
        }
    }

    public static String setToString(int set) {
        switch (set) {
            case -1:
                return "ALL";
            case 0:
                return "DEFAULT";
            case 1:
                return "FOREGROUND";
            case 1001:
                return "DBG_VPN_IN";
            case 1002:
                return "DBG_VPN_OUT";
            default:
                return "UNKNOWN";
        }
    }

    public static String setToCheckinString(int set) {
        switch (set) {
            case -1:
                return "all";
            case 0:
                return "def";
            case 1:
                return "fg";
            case 1001:
                return "vpnin";
            case 1002:
                return "vpnout";
            default:
                return "unk";
        }
    }

    public static boolean setMatches(int querySet, int dataSet) {
        boolean z = true;
        if (querySet == dataSet) {
            return true;
        }
        if (querySet != -1 || dataSet >= 1000) {
            z = false;
        }
        return z;
    }

    public static String tagToString(int tag) {
        return "0x" + Integer.toHexString(tag);
    }

    public static String meteredToString(int metered) {
        switch (metered) {
            case -1:
                return "ALL";
            case 0:
                return "NO";
            case 1:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    public static String roamingToString(int roaming) {
        switch (roaming) {
            case -1:
                return "ALL";
            case 0:
                return "NO";
            case 1:
                return "YES";
            default:
                return "UNKNOWN";
        }
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(ProxyInfo.LOCAL_EXCL_LIST, new PrintWriter(writer));
        return writer.toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean migrateTun(int tunUid, String tunIface, String underlyingIface) {
        Entry tunIfaceTotal = new Entry();
        Entry underlyingIfaceTotal = new Entry();
        tunAdjustmentInit(tunUid, tunIface, underlyingIface, tunIfaceTotal, underlyingIfaceTotal);
        Entry pool = tunGetPool(tunIfaceTotal, underlyingIfaceTotal);
        if (pool.isEmpty()) {
            return true;
        }
        Entry moved = addTrafficToApplications(tunUid, tunIface, underlyingIface, tunIfaceTotal, pool);
        deductTrafficFromVpnApp(tunUid, underlyingIface, moved);
        if (moved.isEmpty()) {
            return true;
        }
        Slog.wtf(TAG, "Failed to deduct underlying network traffic from VPN package. Moved=" + moved);
        return false;
    }

    private void tunAdjustmentInit(int tunUid, String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry underlyingIfaceTotal) {
        Entry recycle = new Entry();
        int i = 0;
        while (i < this.size) {
            getValues(i, recycle);
            if (recycle.uid == -1) {
                throw new IllegalStateException("Cannot adjust VPN accounting on an iface aggregated NetworkStats.");
            } else if (recycle.set == 1001 || recycle.set == 1002) {
                throw new IllegalStateException("Cannot adjust VPN accounting on a NetworkStats containing SET_DBG_VPN_*");
            } else {
                if (recycle.uid == tunUid && recycle.tag == 0 && Objects.equals(underlyingIface, recycle.iface)) {
                    underlyingIfaceTotal.add(recycle);
                }
                if (recycle.uid != tunUid && recycle.tag == 0 && Objects.equals(tunIface, recycle.iface)) {
                    tunIfaceTotal.add(recycle);
                }
                i++;
            }
        }
    }

    private static Entry tunGetPool(Entry tunIfaceTotal, Entry underlyingIfaceTotal) {
        Entry pool = new Entry();
        pool.rxBytes = Math.min(tunIfaceTotal.rxBytes, underlyingIfaceTotal.rxBytes);
        pool.rxPackets = Math.min(tunIfaceTotal.rxPackets, underlyingIfaceTotal.rxPackets);
        pool.txBytes = Math.min(tunIfaceTotal.txBytes, underlyingIfaceTotal.txBytes);
        pool.txPackets = Math.min(tunIfaceTotal.txPackets, underlyingIfaceTotal.txPackets);
        pool.operations = Math.min(tunIfaceTotal.operations, underlyingIfaceTotal.operations);
        return pool;
    }

    private Entry addTrafficToApplications(int tunUid, String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry pool) {
        Entry moved = new Entry();
        Entry tmpEntry = new Entry();
        tmpEntry.iface = underlyingIface;
        int i = 0;
        while (i < this.size) {
            if (Objects.equals(this.iface[i], tunIface) && this.uid[i] != tunUid) {
                if (tunIfaceTotal.rxBytes > 0) {
                    tmpEntry.rxBytes = (pool.rxBytes * this.rxBytes[i]) / tunIfaceTotal.rxBytes;
                } else {
                    tmpEntry.rxBytes = 0;
                }
                if (tunIfaceTotal.rxPackets > 0) {
                    tmpEntry.rxPackets = (pool.rxPackets * this.rxPackets[i]) / tunIfaceTotal.rxPackets;
                } else {
                    tmpEntry.rxPackets = 0;
                }
                if (tunIfaceTotal.txBytes > 0) {
                    tmpEntry.txBytes = (pool.txBytes * this.txBytes[i]) / tunIfaceTotal.txBytes;
                } else {
                    tmpEntry.txBytes = 0;
                }
                if (tunIfaceTotal.txPackets > 0) {
                    tmpEntry.txPackets = (pool.txPackets * this.txPackets[i]) / tunIfaceTotal.txPackets;
                } else {
                    tmpEntry.txPackets = 0;
                }
                if (tunIfaceTotal.operations > 0) {
                    tmpEntry.operations = (pool.operations * this.operations[i]) / tunIfaceTotal.operations;
                } else {
                    tmpEntry.operations = 0;
                }
                tmpEntry.uid = this.uid[i];
                tmpEntry.tag = this.tag[i];
                tmpEntry.set = this.set[i];
                tmpEntry.metered = this.metered[i];
                tmpEntry.roaming = this.roaming[i];
                combineValues(tmpEntry);
                if (this.tag[i] == 0) {
                    moved.add(tmpEntry);
                    tmpEntry.set = 1001;
                    combineValues(tmpEntry);
                }
            }
            i++;
        }
        return moved;
    }

    private void deductTrafficFromVpnApp(int tunUid, String underlyingIface, Entry moved) {
        moved.uid = tunUid;
        moved.set = 1002;
        moved.tag = 0;
        moved.iface = underlyingIface;
        moved.metered = -1;
        moved.roaming = -1;
        combineValues(moved);
        int idxVpnBackground = findIndex(underlyingIface, tunUid, 0, 0, 0, 0);
        if (idxVpnBackground != -1) {
            tunSubtract(idxVpnBackground, this, moved);
        }
        int idxVpnForeground = findIndex(underlyingIface, tunUid, 1, 0, 0, 0);
        if (idxVpnForeground != -1) {
            tunSubtract(idxVpnForeground, this, moved);
        }
    }

    private static void tunSubtract(int i, NetworkStats left, Entry right) {
        long rxBytes = Math.min(left.rxBytes[i], right.rxBytes);
        long[] jArr = left.rxBytes;
        jArr[i] = jArr[i] - rxBytes;
        right.rxBytes -= rxBytes;
        long rxPackets = Math.min(left.rxPackets[i], right.rxPackets);
        jArr = left.rxPackets;
        jArr[i] = jArr[i] - rxPackets;
        right.rxPackets -= rxPackets;
        long txBytes = Math.min(left.txBytes[i], right.txBytes);
        jArr = left.txBytes;
        jArr[i] = jArr[i] - txBytes;
        right.txBytes -= txBytes;
        long txPackets = Math.min(left.txPackets[i], right.txPackets);
        jArr = left.txPackets;
        jArr[i] = jArr[i] - txPackets;
        right.txPackets -= txPackets;
    }
}
