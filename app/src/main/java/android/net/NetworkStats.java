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
    public static final Creator<NetworkStats> CREATOR = null;
    public static final String IFACE_ALL = null;
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
    private int capacity;
    private long elapsedRealtime;
    private String[] iface;
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
        public String iface;
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
            this(NetworkStats.IFACE_ALL, NetworkStats.UID_ALL, NetworkStats.TAG_NONE, NetworkStats.TAG_NONE, 0, 0, 0, 0, 0);
        }

        public Entry(long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(NetworkStats.IFACE_ALL, NetworkStats.UID_ALL, NetworkStats.TAG_NONE, NetworkStats.TAG_NONE, rxBytes, rxPackets, txBytes, txPackets, operations);
        }

        public Entry(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(iface, uid, set, tag, NetworkStats.TAG_NONE, rxBytes, rxPackets, txBytes, txPackets, operations);
        }

        public Entry(String iface, int uid, int set, int tag, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
            this(iface, uid, set, tag, roaming, rxBytes, rxPackets, txBytes, txPackets, operations, ProxyInfo.LOCAL_EXCL_LIST);
        }

        public Entry(String iface, int uid, int set, int tag, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations, String proc) {
            this.proc = ProxyInfo.LOCAL_EXCL_LIST;
            this.iface = iface;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.roaming = roaming;
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.operations = operations;
            this.proc = proc;
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
            builder.append(" roaming=").append(NetworkStats.roamingToString(this.roaming));
            builder.append(" rxBytes=").append(this.rxBytes);
            builder.append(" rxPackets=").append(this.rxPackets);
            builder.append(" txBytes=").append(this.txBytes);
            builder.append(" txPackets=").append(this.txPackets);
            builder.append(" operations=").append(this.operations);
            builder.append(" proc=").append(this.proc);
            return builder.toString();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (this.uid == e.uid && this.set == e.set && this.tag == e.tag && this.roaming == e.roaming && this.rxBytes == e.rxBytes && this.rxPackets == e.rxPackets && this.txBytes == e.txBytes && this.txPackets == e.txPackets && this.operations == e.operations && this.iface.equals(e.iface)) {
                z = this.proc.equals(e.proc);
            }
            return z;
        }
    }

    public interface NonMonotonicObserver<C> {
        void foundNonMonotonic(NetworkStats networkStats, int i, NetworkStats networkStats2, int i2, C c);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkStats.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkStats.<clinit>():void");
    }

    public NetworkStats(long elapsedRealtime, int initialSize) {
        this.elapsedRealtime = elapsedRealtime;
        this.size = TAG_NONE;
        if (initialSize >= 0) {
            this.capacity = initialSize;
            this.iface = new String[initialSize];
            this.uid = new int[initialSize];
            this.set = new int[initialSize];
            this.tag = new int[initialSize];
            this.roaming = new int[initialSize];
            this.rxBytes = new long[initialSize];
            this.rxPackets = new long[initialSize];
            this.txBytes = new long[initialSize];
            this.txPackets = new long[initialSize];
            this.operations = new long[initialSize];
            this.proc = new String[initialSize];
            return;
        }
        this.capacity = TAG_NONE;
        this.iface = EmptyArray.STRING;
        this.uid = EmptyArray.INT;
        this.set = EmptyArray.INT;
        this.tag = EmptyArray.INT;
        this.roaming = EmptyArray.INT;
        this.rxBytes = EmptyArray.LONG;
        this.rxPackets = EmptyArray.LONG;
        this.txBytes = EmptyArray.LONG;
        this.txPackets = EmptyArray.LONG;
        this.operations = EmptyArray.LONG;
        this.proc = EmptyArray.STRING;
    }

    public NetworkStats(Parcel parcel) {
        this.elapsedRealtime = parcel.readLong();
        this.size = parcel.readInt();
        this.capacity = parcel.readInt();
        this.iface = parcel.createStringArray();
        this.uid = parcel.createIntArray();
        this.set = parcel.createIntArray();
        this.tag = parcel.createIntArray();
        this.roaming = parcel.createIntArray();
        this.rxBytes = parcel.createLongArray();
        this.rxPackets = parcel.createLongArray();
        this.txBytes = parcel.createLongArray();
        this.txPackets = parcel.createLongArray();
        this.operations = parcel.createLongArray();
        this.proc = parcel.createStringArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.elapsedRealtime);
        dest.writeInt(this.size);
        dest.writeInt(this.capacity);
        dest.writeStringArray(this.iface);
        dest.writeIntArray(this.uid);
        dest.writeIntArray(this.set);
        dest.writeIntArray(this.tag);
        dest.writeIntArray(this.roaming);
        dest.writeLongArray(this.rxBytes);
        dest.writeLongArray(this.rxPackets);
        dest.writeLongArray(this.txBytes);
        dest.writeLongArray(this.txPackets);
        dest.writeLongArray(this.operations);
        dest.writeStringArray(this.proc);
    }

    public NetworkStats clone() {
        NetworkStats clone = new NetworkStats(this.elapsedRealtime, this.size);
        Entry entry = null;
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
            entry = getValues(i, entry);
            clone.addValues(entry);
        }
        return clone;
    }

    public NetworkStats addIfaceValues(String iface, long rxBytes, long rxPackets, long txBytes, long txPackets) {
        return addValues(iface, UID_ALL, TAG_NONE, TAG_NONE, rxBytes, rxPackets, txBytes, txPackets, 0);
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats addValues(String iface, int uid, int set, int tag, int roaming, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return addValues(new Entry(iface, uid, set, tag, roaming, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats addValues(Entry entry) {
        if (this.size >= this.capacity) {
            int newLength = (Math.max(this.size, 10) * 3) / 2;
            this.iface = (String[]) Arrays.copyOf(this.iface, newLength);
            this.uid = Arrays.copyOf(this.uid, newLength);
            this.set = Arrays.copyOf(this.set, newLength);
            this.tag = Arrays.copyOf(this.tag, newLength);
            this.roaming = Arrays.copyOf(this.roaming, newLength);
            this.rxBytes = Arrays.copyOf(this.rxBytes, newLength);
            this.rxPackets = Arrays.copyOf(this.rxPackets, newLength);
            this.txBytes = Arrays.copyOf(this.txBytes, newLength);
            this.txPackets = Arrays.copyOf(this.txPackets, newLength);
            this.operations = Arrays.copyOf(this.operations, newLength);
            this.proc = (String[]) Arrays.copyOf(this.proc, newLength);
            this.capacity = newLength;
        }
        this.iface[this.size] = entry.iface;
        this.uid[this.size] = entry.uid;
        this.set[this.size] = entry.set;
        this.tag[this.size] = entry.tag;
        this.roaming[this.size] = entry.roaming;
        this.rxBytes[this.size] = entry.rxBytes;
        this.rxPackets[this.size] = entry.rxPackets;
        this.txBytes[this.size] = entry.txBytes;
        this.txPackets[this.size] = entry.txPackets;
        this.operations[this.size] = entry.operations;
        this.proc[this.size] = entry.proc;
        this.size += SET_FOREGROUND;
        return this;
    }

    public Entry getValues(int i, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = this.iface[i];
        entry.uid = this.uid[i];
        entry.set = this.set[i];
        entry.tag = this.tag[i];
        entry.roaming = this.roaming[i];
        entry.rxBytes = this.rxBytes[i];
        entry.rxPackets = this.rxPackets[i];
        entry.txBytes = this.txBytes[i];
        entry.txPackets = this.txPackets[i];
        entry.operations = this.operations[i];
        entry.proc = this.proc[i];
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
        return combineValues(iface, uid, TAG_NONE, tag, rxBytes, rxPackets, txBytes, txPackets, operations);
    }

    public NetworkStats combineValues(String iface, int uid, int set, int tag, long rxBytes, long rxPackets, long txBytes, long txPackets, long operations) {
        return combineValues(new Entry(iface, uid, set, tag, rxBytes, rxPackets, txBytes, txPackets, operations));
    }

    public NetworkStats combineValues(Entry entry) {
        int i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.roaming, entry.proc);
        if (i == UID_ALL) {
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
        for (int i = TAG_NONE; i < another.size; i += SET_FOREGROUND) {
            entry = another.getValues(i, entry);
            combineValues(entry);
        }
    }

    public int findIndex(String iface, int uid, int set, int tag, int roaming) {
        int i = TAG_NONE;
        while (i < this.size) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
            i += SET_FOREGROUND;
        }
        return UID_ALL;
    }

    private int findIndex(String iface, int uid, int set, int tag, int roaming, String proc) {
        int i = TAG_NONE;
        while (i < this.size) {
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i]) && Objects.equals(proc, this.proc[i])) {
                return i;
            }
            i += SET_FOREGROUND;
        }
        return UID_ALL;
    }

    private int findIndexHinted(String iface, int uid, int set, int tag, int roaming, String proc, int hintIndex) {
        for (int offset = TAG_NONE; offset < this.size; offset += SET_FOREGROUND) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) + UID_ALL) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i]) && Objects.equals(proc, this.proc[i])) {
                return i;
            }
        }
        return UID_ALL;
    }

    public int findIndexHinted(String iface, int uid, int set, int tag, int roaming, int hintIndex) {
        for (int offset = TAG_NONE; offset < this.size; offset += SET_FOREGROUND) {
            int i;
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % this.size;
            } else {
                i = (((this.size + hintIndex) - halfOffset) + UID_ALL) % this.size;
            }
            if (uid == this.uid[i] && set == this.set[i] && tag == this.tag[i] && roaming == this.roaming[i] && Objects.equals(iface, this.iface[i])) {
                return i;
            }
        }
        return UID_ALL;
    }

    public void spliceOperationsFrom(NetworkStats stats) {
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
            int j = stats.findIndex(this.iface[i], this.uid[i], this.set[i], this.tag[i], this.roaming[i], this.proc[i]);
            if (j == UID_ALL) {
                this.operations[i] = 0;
            } else {
                this.operations[i] = stats.operations[j];
            }
        }
    }

    public String[] getUniqueIfaces() {
        HashSet<String> ifaces = new HashSet();
        String[] strArr = this.iface;
        int length = strArr.length;
        for (int i = TAG_NONE; i < length; i += SET_FOREGROUND) {
            String iface = strArr[i];
            if (iface != IFACE_ALL) {
                ifaces.add(iface);
            }
        }
        return (String[]) ifaces.toArray(new String[ifaces.size()]);
    }

    public int[] getUniqueUids() {
        SparseBooleanArray uids = new SparseBooleanArray();
        int[] iArr = this.uid;
        int length = iArr.length;
        for (int i = TAG_NONE; i < length; i += SET_FOREGROUND) {
            uids.put(iArr[i], true);
        }
        int size = uids.size();
        int[] result = new int[size];
        for (int i2 = TAG_NONE; i2 < size; i2 += SET_FOREGROUND) {
            result[i2] = uids.keyAt(i2);
        }
        return result;
    }

    public long getTotalBytes() {
        Entry entry = getTotal(null);
        return entry.rxBytes + entry.txBytes;
    }

    public Entry getTotal(Entry recycle) {
        return getTotal(recycle, null, UID_ALL, false);
    }

    public Entry getTotal(Entry recycle, int limitUid) {
        return getTotal(recycle, null, limitUid, false);
    }

    public Entry getTotal(Entry recycle, HashSet<String> limitIface) {
        return getTotal(recycle, limitIface, UID_ALL, false);
    }

    public Entry getTotalIncludingTags(Entry recycle) {
        return getTotal(recycle, null, UID_ALL, true);
    }

    private Entry getTotal(Entry recycle, HashSet<String> limitIface, int limitUid, boolean includeTags) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = IFACE_ALL;
        entry.uid = limitUid;
        entry.set = UID_ALL;
        entry.tag = TAG_NONE;
        entry.roaming = UID_ALL;
        entry.rxBytes = 0;
        entry.rxPackets = 0;
        entry.txBytes = 0;
        entry.txPackets = 0;
        entry.operations = 0;
        int i = TAG_NONE;
        while (i < this.size) {
            boolean matchesUid = limitUid == UID_ALL || limitUid == this.uid[i];
            boolean contains = limitIface != null ? limitIface.contains(this.iface[i]) : true;
            if (matchesUid && contains && (this.tag[i] == 0 || includeTags)) {
                entry.rxBytes += this.rxBytes[i];
                entry.rxPackets += this.rxPackets[i];
                entry.txBytes += this.txBytes[i];
                entry.txPackets += this.txPackets[i];
                entry.operations += this.operations[i];
            }
            i += SET_FOREGROUND;
        }
        return entry;
    }

    public long getTotalPackets() {
        long total = 0;
        for (int i = this.size + UID_ALL; i >= 0; i += UID_ALL) {
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
                observer.foundNonMonotonic(left, UID_ALL, right, UID_ALL, cookie);
            }
            deltaRealtime = 0;
        }
        Entry entry = new Entry();
        if (recycle == null || recycle.capacity < left.size) {
            NetworkStats networkStats = new NetworkStats(deltaRealtime, left.size);
        } else {
            result = recycle;
            recycle.size = TAG_NONE;
            recycle.elapsedRealtime = deltaRealtime;
        }
        for (int i = TAG_NONE; i < left.size; i += SET_FOREGROUND) {
            entry.iface = left.iface[i];
            entry.uid = left.uid[i];
            entry.set = left.set[i];
            entry.tag = left.tag[i];
            entry.roaming = left.roaming[i];
            entry.proc = left.proc[i];
            int j = right.findIndexHinted(entry.iface, entry.uid, entry.set, entry.tag, entry.roaming, entry.proc, i);
            if (j == UID_ALL) {
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
                if (entry.rxBytes >= 0 && entry.rxPackets >= 0 && entry.txBytes >= 0 && entry.txPackets >= 0) {
                    if (entry.operations < 0) {
                    }
                }
                if (observer != null) {
                    observer.foundNonMonotonic(left, i, right, j, cookie);
                }
                entry.rxBytes = Math.max(entry.rxBytes, 0);
                entry.rxPackets = Math.max(entry.rxPackets, 0);
                entry.txBytes = Math.max(entry.txBytes, 0);
                entry.txPackets = Math.max(entry.txPackets, 0);
                entry.operations = Math.max(entry.operations, 0);
            }
            result.addValues(entry);
        }
        return result;
    }

    public NetworkStats groupedByIface() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        Entry entry = new Entry();
        entry.uid = UID_ALL;
        entry.set = UID_ALL;
        entry.tag = TAG_NONE;
        entry.roaming = UID_ALL;
        entry.operations = 0;
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
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
        entry.set = UID_ALL;
        entry.tag = TAG_NONE;
        entry.roaming = UID_ALL;
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
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
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
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
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
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
        }
    }

    public static String setToString(int set) {
        switch (set) {
            case UID_ALL /*-1*/:
                return "ALL";
            case TAG_NONE /*0*/:
                return "DEFAULT";
            case SET_FOREGROUND /*1*/:
                return "FOREGROUND";
            case SET_DBG_VPN_IN /*1001*/:
                return "DBG_VPN_IN";
            case SET_DBG_VPN_OUT /*1002*/:
                return "DBG_VPN_OUT";
            default:
                return "UNKNOWN";
        }
    }

    public static String setToCheckinString(int set) {
        switch (set) {
            case UID_ALL /*-1*/:
                return "all";
            case TAG_NONE /*0*/:
                return "def";
            case SET_FOREGROUND /*1*/:
                return "fg";
            case SET_DBG_VPN_IN /*1001*/:
                return "vpnin";
            case SET_DBG_VPN_OUT /*1002*/:
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
        if (querySet != UID_ALL || dataSet >= SET_DEBUG_START) {
            z = false;
        }
        return z;
    }

    public static String tagToString(int tag) {
        return "0x" + Integer.toHexString(tag);
    }

    public static String roamingToString(int roaming) {
        switch (roaming) {
            case UID_ALL /*-1*/:
                return "ALL";
            case TAG_NONE /*0*/:
                return "NO";
            case SET_FOREGROUND /*1*/:
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
        return TAG_NONE;
    }

    public boolean migrateTun(int tunUid, String tunIface, String underlyingIface) {
        Entry tunIfaceTotal = new Entry();
        Entry underlyingIfaceTotal = new Entry();
        tunAdjustmentInit(tunUid, tunIface, underlyingIface, tunIfaceTotal, underlyingIfaceTotal);
        Entry pool = tunGetPool(tunIfaceTotal, underlyingIfaceTotal);
        if (pool.isEmpty()) {
            return true;
        }
        Entry moved = addTrafficToApplications(tunIface, underlyingIface, tunIfaceTotal, pool);
        deductTrafficFromVpnApp(tunUid, underlyingIface, moved);
        if (moved.isEmpty()) {
            return true;
        }
        Slog.wtf(TAG, "Failed to deduct underlying network traffic from VPN package. Moved=" + moved);
        return false;
    }

    private void tunAdjustmentInit(int tunUid, String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry underlyingIfaceTotal) {
        Entry recycle = new Entry();
        int i = TAG_NONE;
        while (i < this.size) {
            getValues(i, recycle);
            if (recycle.uid == UID_ALL) {
                throw new IllegalStateException("Cannot adjust VPN accounting on an iface aggregated NetworkStats.");
            } else if (recycle.set == SET_DBG_VPN_IN || recycle.set == SET_DBG_VPN_OUT) {
                throw new IllegalStateException("Cannot adjust VPN accounting on a NetworkStats containing SET_DBG_VPN_*");
            } else {
                if (recycle.uid == tunUid && recycle.tag == 0 && Objects.equals(underlyingIface, recycle.iface)) {
                    underlyingIfaceTotal.add(recycle);
                }
                if (recycle.tag == 0 && Objects.equals(tunIface, recycle.iface)) {
                    tunIfaceTotal.add(recycle);
                }
                i += SET_FOREGROUND;
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

    private Entry addTrafficToApplications(String tunIface, String underlyingIface, Entry tunIfaceTotal, Entry pool) {
        Entry moved = new Entry();
        Entry tmpEntry = new Entry();
        tmpEntry.iface = underlyingIface;
        for (int i = TAG_NONE; i < this.size; i += SET_FOREGROUND) {
            if (Objects.equals(this.iface[i], tunIface)) {
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
                tmpEntry.roaming = this.roaming[i];
                combineValues(tmpEntry);
                if (this.tag[i] == 0) {
                    moved.add(tmpEntry);
                    tmpEntry.set = SET_DBG_VPN_IN;
                    combineValues(tmpEntry);
                }
            }
        }
        return moved;
    }

    private void deductTrafficFromVpnApp(int tunUid, String underlyingIface, Entry moved) {
        moved.uid = tunUid;
        moved.set = SET_DBG_VPN_OUT;
        moved.tag = TAG_NONE;
        moved.iface = underlyingIface;
        moved.roaming = UID_ALL;
        combineValues(moved);
        int idxVpnBackground = findIndex(underlyingIface, tunUid, TAG_NONE, TAG_NONE, TAG_NONE);
        if (idxVpnBackground != UID_ALL) {
            tunSubtract(idxVpnBackground, this, moved);
        }
        int idxVpnForeground = findIndex(underlyingIface, tunUid, SET_FOREGROUND, TAG_NONE, TAG_NONE);
        if (idxVpnForeground != UID_ALL) {
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
