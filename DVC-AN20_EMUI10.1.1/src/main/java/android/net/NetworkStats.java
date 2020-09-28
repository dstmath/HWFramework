package android.net;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.ArrayUtils;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import libcore.util.EmptyArray;

public class NetworkStats implements Parcelable {
    private static final String CLATD_INTERFACE_PREFIX = "v4-";
    @UnsupportedAppUsage
    public static final Parcelable.Creator<NetworkStats> CREATOR = new Parcelable.Creator<NetworkStats>() {
        /* class android.net.NetworkStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkStats createFromParcel(Parcel in) {
            return new NetworkStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public NetworkStats[] newArray(int size) {
            return new NetworkStats[size];
        }
    };
    public static final int DEFAULT_NETWORK_ALL = -1;
    public static final int DEFAULT_NETWORK_NO = 0;
    public static final int DEFAULT_NETWORK_YES = 1;
    public static final String IFACE_ALL = null;
    public static final String[] INTERFACES_ALL = null;
    private static final int IPV4V6_HEADER_DELTA = 20;
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
    public static final int SET_STATIC = 9;
    public static final int STATS_PER_IFACE = 0;
    public static final int STATS_PER_UID = 1;
    private static final String TAG = "NetworkStats";
    public static final int TAG_ALL = -1;
    public static final int TAG_NONE = 0;
    public static final int UID_ALL = -1;
    private int[] actUid;
    @UnsupportedAppUsage
    private int capacity;
    @UnsupportedAppUsage
    private int[] defaultNetwork;
    private long elapsedRealtime;
    @UnsupportedAppUsage
    private String[] iface;
    @UnsupportedAppUsage
    private int[] metered;
    @UnsupportedAppUsage
    private long[] operations;
    private String[] proc;
    @UnsupportedAppUsage
    private int[] roaming;
    @UnsupportedAppUsage
    private long[] rxBytes;
    private long[] rxBytes_mp;
    @UnsupportedAppUsage
    private long[] rxPackets;
    private long[] rxPackets_mp;
    @UnsupportedAppUsage
    private int[] set;
    @UnsupportedAppUsage
    private int size;
    @UnsupportedAppUsage
    private int[] tag;
    @UnsupportedAppUsage
    private long[] txBytes;
    private long[] txBytes_mp;
    @UnsupportedAppUsage
    private long[] txPackets;
    private long[] txPackets_mp;
    @UnsupportedAppUsage
    private int[] uid;

    public interface NonMonotonicObserver<C> {
        void foundNonMonotonic(NetworkStats networkStats, int i, NetworkStats networkStats2, int i2, C c);

        void foundNonMonotonic(NetworkStats networkStats, int i, C c);

        void foundNonMonotonic(NetworkStats networkStats, int[] iArr, NetworkStats networkStats2, int[] iArr2, C c);
    }

    public static class Entry {
        public int actUid;
        public int defaultNetwork;
        @UnsupportedAppUsage
        public String iface;
        public int metered;
        public long operations;
        public String proc;
        public int roaming;
        @UnsupportedAppUsage
        public long rxBytes;
        public long rxBytes_mp;
        @UnsupportedAppUsage
        public long rxPackets;
        public long rxPackets_mp;
        @UnsupportedAppUsage
        public int set;
        @UnsupportedAppUsage
        public int tag;
        @UnsupportedAppUsage
        public long txBytes;
        public long txBytes_mp;
        @UnsupportedAppUsage
        public long txPackets;
        public long txPackets_mp;
        @UnsupportedAppUsage
        public int uid;

        @UnsupportedAppUsage
        public Entry() {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, 0, 0, 0, 0, 0);
        }

        public Entry(long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
            this(NetworkStats.IFACE_ALL, -1, 0, 0, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2);
        }

        public Entry(String iface2, int uid2, int set2, int tag2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
            this(iface2, uid2, set2, tag2, 0, 0, 0, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2);
        }

        public Entry(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
            this(iface2, uid2, set2, tag2, metered2, roaming2, defaultNetwork2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2, "");
        }

        public Entry(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2, String proc2) {
            this(iface2, uid2, set2, tag2, metered2, roaming2, defaultNetwork2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2, proc2, -1);
        }

        public Entry(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2, String proc2, int actUid2) {
            this(iface2, uid2, set2, tag2, metered2, roaming2, defaultNetwork2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2, proc2, -1, 0, 0, 0, 0);
        }

        public Entry(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2, long rxBytes_mp2, long rxPackets_mp2, long txBytes_mp2, long txPackets_mp2) {
            this(iface2, uid2, set2, tag2, metered2, roaming2, defaultNetwork2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2, "", -1, rxBytes_mp2, rxPackets_mp2, txBytes_mp2, txPackets_mp2);
        }

        public Entry(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2, String proc2, int actUid2, long rxBytes_mp2, long rxPackets_mp2, long txBytes_mp2, long txPackets_mp2) {
            this.proc = "";
            this.actUid = -1;
            this.iface = iface2;
            this.uid = uid2;
            this.set = set2;
            this.tag = tag2;
            this.metered = metered2;
            this.roaming = roaming2;
            this.defaultNetwork = defaultNetwork2;
            this.rxBytes = rxBytes2;
            this.rxPackets = rxPackets2;
            this.txBytes = txBytes2;
            this.txPackets = txPackets2;
            this.operations = operations2;
            this.proc = proc2;
            this.actUid = actUid2;
            this.rxBytes_mp = rxBytes_mp2;
            this.rxPackets_mp = rxPackets_mp2;
            this.txBytes_mp = txBytes_mp2;
            this.txPackets_mp = txPackets_mp2;
        }

        public boolean isNegative() {
            return this.rxBytes < 0 || this.rxPackets < 0 || this.txBytes < 0 || this.txPackets < 0 || this.operations < 0;
        }

        public boolean isEmpty() {
            return this.rxBytes == 0 && this.rxPackets == 0 && this.txBytes == 0 && this.txPackets == 0 && this.operations == 0;
        }

        public void add(Entry another) {
            this.rxBytes += another.rxBytes;
            this.rxPackets += another.rxPackets;
            this.txBytes += another.txBytes;
            this.txPackets += another.txPackets;
            this.operations += another.operations;
            this.rxBytes_mp += another.rxBytes_mp;
            this.rxPackets_mp += another.rxPackets_mp;
            this.txBytes_mp += another.txBytes_mp;
            this.txPackets_mp += another.txPackets_mp;
        }

        public String toString() {
            return "iface=" + this.iface + " uid=" + this.uid + " set=" + NetworkStats.setToString(this.set) + " tag=" + NetworkStats.tagToString(this.tag) + " metered=" + NetworkStats.meteredToString(this.metered) + " roaming=" + NetworkStats.roamingToString(this.roaming) + " defaultNetwork=" + NetworkStats.defaultNetworkToString(this.defaultNetwork) + " rxBytes=" + this.rxBytes + " rxPackets=" + this.rxPackets + " txBytes=" + this.txBytes + " txPackets=" + this.txPackets + " operations=" + this.operations + " proc=" + this.proc + " actUid=" + this.actUid + " rxBytes_mp=" + this.rxBytes_mp + " rxPackets_mp=" + this.rxPackets_mp + " txBytes_mp=" + this.txBytes_mp + " txPackets_mp=" + this.txPackets_mp;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (this.uid == e.uid && this.set == e.set && this.tag == e.tag && this.metered == e.metered && this.roaming == e.roaming && this.defaultNetwork == e.defaultNetwork && this.rxBytes == e.rxBytes && this.rxPackets == e.rxPackets && this.txBytes == e.txBytes && this.txPackets == e.txPackets && this.operations == e.operations && this.iface.equals(e.iface) && this.proc.equals(e.proc) && this.actUid == e.actUid) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.uid), Integer.valueOf(this.set), Integer.valueOf(this.tag), Integer.valueOf(this.metered), Integer.valueOf(this.roaming), Integer.valueOf(this.defaultNetwork), this.iface);
        }
    }

    @UnsupportedAppUsage
    public NetworkStats(long elapsedRealtime2, int initialSize) {
        this.elapsedRealtime = elapsedRealtime2;
        this.size = 0;
        if (initialSize > 0) {
            this.capacity = initialSize;
            this.iface = new String[initialSize];
            this.uid = new int[initialSize];
            this.set = new int[initialSize];
            this.tag = new int[initialSize];
            this.metered = new int[initialSize];
            this.roaming = new int[initialSize];
            this.defaultNetwork = new int[initialSize];
            this.rxBytes = new long[initialSize];
            this.rxPackets = new long[initialSize];
            this.txBytes = new long[initialSize];
            this.txPackets = new long[initialSize];
            this.operations = new long[initialSize];
            this.proc = new String[initialSize];
            this.actUid = new int[initialSize];
            this.rxBytes_mp = new long[initialSize];
            this.rxPackets_mp = new long[initialSize];
            this.txBytes_mp = new long[initialSize];
            this.txPackets_mp = new long[initialSize];
            return;
        }
        clear();
    }

    @UnsupportedAppUsage
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
        this.defaultNetwork = parcel.createIntArray();
        this.rxBytes = parcel.createLongArray();
        this.rxPackets = parcel.createLongArray();
        this.txBytes = parcel.createLongArray();
        this.txPackets = parcel.createLongArray();
        this.operations = parcel.createLongArray();
        this.proc = parcel.createStringArray();
        this.actUid = parcel.createIntArray();
        this.rxBytes_mp = parcel.createLongArray();
        this.rxPackets_mp = parcel.createLongArray();
        this.txBytes_mp = parcel.createLongArray();
        this.txPackets_mp = parcel.createLongArray();
    }

    @Override // android.os.Parcelable
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
        dest.writeIntArray(this.defaultNetwork);
        dest.writeLongArray(this.rxBytes);
        dest.writeLongArray(this.rxPackets);
        dest.writeLongArray(this.txBytes);
        dest.writeLongArray(this.txPackets);
        dest.writeLongArray(this.operations);
        dest.writeStringArray(this.proc);
        dest.writeIntArray(this.actUid);
        dest.writeLongArray(this.rxBytes_mp);
        dest.writeLongArray(this.rxPackets_mp);
        dest.writeLongArray(this.txBytes_mp);
        dest.writeLongArray(this.txPackets_mp);
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

    public void clear() {
        this.capacity = 0;
        this.iface = EmptyArray.STRING;
        this.uid = EmptyArray.INT;
        this.set = EmptyArray.INT;
        this.tag = EmptyArray.INT;
        this.metered = EmptyArray.INT;
        this.roaming = EmptyArray.INT;
        this.defaultNetwork = EmptyArray.INT;
        this.rxBytes = EmptyArray.LONG;
        this.rxPackets = EmptyArray.LONG;
        this.txBytes = EmptyArray.LONG;
        this.txPackets = EmptyArray.LONG;
        this.operations = EmptyArray.LONG;
        this.proc = EmptyArray.STRING;
        this.actUid = EmptyArray.INT;
        this.rxBytes_mp = EmptyArray.LONG;
        this.rxPackets_mp = EmptyArray.LONG;
        this.txBytes_mp = EmptyArray.LONG;
        this.txPackets_mp = EmptyArray.LONG;
    }

    @VisibleForTesting
    public NetworkStats addIfaceValues(String iface2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2) {
        return addValues(iface2, -1, 0, 0, rxBytes2, rxPackets2, txBytes2, txPackets2, 0);
    }

    @VisibleForTesting
    public NetworkStats addValues(String iface2, int uid2, int set2, int tag2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
        return addValues(new Entry(iface2, uid2, set2, tag2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2));
    }

    @VisibleForTesting
    public NetworkStats addValues(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
        return addValues(new Entry(iface2, uid2, set2, tag2, metered2, roaming2, defaultNetwork2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2));
    }

    public NetworkStats addValues(Entry entry) {
        int i = this.size;
        if (i >= this.capacity) {
            int newLength = (Math.max(i, 10) * 3) / 2;
            this.iface = (String[]) Arrays.copyOf(this.iface, newLength);
            this.uid = Arrays.copyOf(this.uid, newLength);
            this.set = Arrays.copyOf(this.set, newLength);
            this.tag = Arrays.copyOf(this.tag, newLength);
            this.metered = Arrays.copyOf(this.metered, newLength);
            this.roaming = Arrays.copyOf(this.roaming, newLength);
            this.defaultNetwork = Arrays.copyOf(this.defaultNetwork, newLength);
            this.rxBytes = Arrays.copyOf(this.rxBytes, newLength);
            this.rxPackets = Arrays.copyOf(this.rxPackets, newLength);
            this.txBytes = Arrays.copyOf(this.txBytes, newLength);
            this.txPackets = Arrays.copyOf(this.txPackets, newLength);
            this.operations = Arrays.copyOf(this.operations, newLength);
            this.proc = (String[]) Arrays.copyOf(this.proc, newLength);
            this.actUid = Arrays.copyOf(this.actUid, newLength);
            this.capacity = newLength;
            this.rxBytes_mp = Arrays.copyOf(this.rxBytes_mp, newLength);
            this.rxPackets_mp = Arrays.copyOf(this.rxPackets_mp, newLength);
            this.txBytes_mp = Arrays.copyOf(this.txBytes_mp, newLength);
            this.txPackets_mp = Arrays.copyOf(this.txPackets_mp, newLength);
        }
        setValues(this.size, entry);
        this.size++;
        return this;
    }

    private void setValues(int i, Entry entry) {
        this.iface[i] = entry.iface;
        this.uid[i] = entry.uid;
        this.set[i] = entry.set;
        this.tag[i] = entry.tag;
        this.metered[i] = entry.metered;
        this.roaming[i] = entry.roaming;
        this.defaultNetwork[i] = entry.defaultNetwork;
        this.rxBytes[i] = entry.rxBytes;
        this.rxPackets[i] = entry.rxPackets;
        this.txBytes[i] = entry.txBytes;
        this.txPackets[i] = entry.txPackets;
        this.operations[i] = entry.operations;
        this.proc[i] = entry.proc;
        this.actUid[i] = entry.actUid;
        this.rxBytes_mp[i] = entry.rxBytes_mp;
        this.rxPackets_mp[i] = entry.rxPackets_mp;
        this.txBytes_mp[i] = entry.txBytes_mp;
        this.txPackets_mp[i] = entry.txPackets_mp;
    }

    @UnsupportedAppUsage
    public Entry getValues(int i, Entry recycle) {
        Entry entry = recycle != null ? recycle : new Entry();
        entry.iface = this.iface[i];
        entry.uid = this.uid[i];
        entry.set = this.set[i];
        entry.tag = this.tag[i];
        entry.metered = this.metered[i];
        entry.roaming = this.roaming[i];
        entry.defaultNetwork = this.defaultNetwork[i];
        entry.rxBytes = this.rxBytes[i];
        entry.rxPackets = this.rxPackets[i];
        entry.txBytes = this.txBytes[i];
        entry.txPackets = this.txPackets[i];
        entry.operations = this.operations[i];
        entry.proc = this.proc[i];
        entry.actUid = this.actUid[i];
        entry.rxBytes_mp = this.rxBytes_mp[i];
        entry.rxPackets_mp = this.rxPackets_mp[i];
        entry.txBytes_mp = this.txBytes_mp[i];
        entry.txPackets_mp = this.txPackets_mp[i];
        return entry;
    }

    private void maybeCopyEntry(int dest, int src) {
        if (dest != src) {
            String[] strArr = this.iface;
            strArr[dest] = strArr[src];
            int[] iArr = this.uid;
            iArr[dest] = iArr[src];
            int[] iArr2 = this.set;
            iArr2[dest] = iArr2[src];
            int[] iArr3 = this.tag;
            iArr3[dest] = iArr3[src];
            int[] iArr4 = this.metered;
            iArr4[dest] = iArr4[src];
            int[] iArr5 = this.roaming;
            iArr5[dest] = iArr5[src];
            int[] iArr6 = this.defaultNetwork;
            iArr6[dest] = iArr6[src];
            long[] jArr = this.rxBytes;
            jArr[dest] = jArr[src];
            long[] jArr2 = this.rxPackets;
            jArr2[dest] = jArr2[src];
            long[] jArr3 = this.txBytes;
            jArr3[dest] = jArr3[src];
            long[] jArr4 = this.txPackets;
            jArr4[dest] = jArr4[src];
            long[] jArr5 = this.operations;
            jArr5[dest] = jArr5[src];
        }
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

    @UnsupportedAppUsage
    public int size() {
        return this.size;
    }

    @VisibleForTesting
    public int internalSize() {
        return this.capacity;
    }

    @Deprecated
    public NetworkStats combineValues(String iface2, int uid2, int tag2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
        return combineValues(iface2, uid2, 0, tag2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2);
    }

    public NetworkStats combineValues(String iface2, int uid2, int set2, int tag2, long rxBytes2, long rxPackets2, long txBytes2, long txPackets2, long operations2) {
        return combineValues(new Entry(iface2, uid2, set2, tag2, rxBytes2, rxPackets2, txBytes2, txPackets2, operations2));
    }

    @UnsupportedAppUsage
    public NetworkStats combineValues(Entry entry) {
        int i = findIndex(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.defaultNetwork, entry.proc, entry.actUid);
        if (i == -1) {
            addValues(entry);
        } else {
            long[] jArr = this.rxBytes;
            jArr[i] = jArr[i] + entry.rxBytes;
            long[] jArr2 = this.rxPackets;
            jArr2[i] = jArr2[i] + entry.rxPackets;
            long[] jArr3 = this.txBytes;
            jArr3[i] = jArr3[i] + entry.txBytes;
            long[] jArr4 = this.txPackets;
            jArr4[i] = jArr4[i] + entry.txPackets;
            long[] jArr5 = this.operations;
            jArr5[i] = jArr5[i] + entry.operations;
            long[] jArr6 = this.rxBytes_mp;
            jArr6[i] = jArr6[i] + entry.rxBytes_mp;
            long[] jArr7 = this.rxPackets_mp;
            jArr7[i] = jArr7[i] + entry.rxPackets_mp;
            long[] jArr8 = this.txBytes_mp;
            jArr8[i] = jArr8[i] + entry.txBytes_mp;
            long[] jArr9 = this.txPackets_mp;
            jArr9[i] = jArr9[i] + entry.txPackets_mp;
        }
        return this;
    }

    @UnsupportedAppUsage
    public void combineAllValues(NetworkStats another) {
        Entry entry = null;
        for (int i = 0; i < another.size; i++) {
            entry = another.getValues(i, entry);
            combineValues(entry);
        }
    }

    public int findIndex(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2) {
        for (int i = 0; i < this.size; i++) {
            if (uid2 == this.uid[i] && set2 == this.set[i] && tag2 == this.tag[i] && metered2 == this.metered[i] && roaming2 == this.roaming[i] && defaultNetwork2 == this.defaultNetwork[i] && Objects.equals(iface2, this.iface[i])) {
                return i;
            }
        }
        return -1;
    }

    private int findIndex(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, String proc2, int actUid2) {
        for (int i = 0; i < this.size; i++) {
            if (uid2 == this.uid[i] && set2 == this.set[i] && tag2 == this.tag[i] && metered2 == this.metered[i] && roaming2 == this.roaming[i] && Objects.equals(iface2, this.iface[i]) && defaultNetwork2 == this.defaultNetwork[i] && Objects.equals(proc2, this.proc[i]) && actUid2 == this.actUid[i]) {
                return i;
            }
        }
        return -1;
    }

    private int findIndexHinted(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, String proc2, int actUid2, int hintIndex) {
        int i;
        int offset = 0;
        while (true) {
            int i2 = this.size;
            if (offset >= i2) {
                return -1;
            }
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % i2;
            } else {
                i = (((i2 + hintIndex) - halfOffset) - 1) % i2;
            }
            if (uid2 == this.uid[i] && set2 == this.set[i] && tag2 == this.tag[i] && metered2 == this.metered[i] && roaming2 == this.roaming[i] && Objects.equals(iface2, this.iface[i]) && defaultNetwork2 == this.defaultNetwork[i] && Objects.equals(proc2, this.proc[i]) && actUid2 == this.actUid[i]) {
                return i;
            }
            offset++;
        }
    }

    @VisibleForTesting
    public int findIndexHinted(String iface2, int uid2, int set2, int tag2, int metered2, int roaming2, int defaultNetwork2, int hintIndex) {
        int i;
        int offset = 0;
        while (true) {
            int i2 = this.size;
            if (offset >= i2) {
                return -1;
            }
            int halfOffset = offset / 2;
            if (offset % 2 == 0) {
                i = (hintIndex + halfOffset) % i2;
            } else {
                i = (((i2 + hintIndex) - halfOffset) - 1) % i2;
            }
            if (uid2 == this.uid[i] && set2 == this.set[i] && tag2 == this.tag[i] && metered2 == this.metered[i] && roaming2 == this.roaming[i] && defaultNetwork2 == this.defaultNetwork[i] && Objects.equals(iface2, this.iface[i])) {
                return i;
            }
            offset++;
        }
    }

    public void spliceOperationsFrom(NetworkStats stats) {
        for (int i = 0; i < this.size; i++) {
            int j = stats.findIndex(this.iface[i], this.uid[i], this.set[i], this.tag[i], this.metered[i], this.roaming[i], this.defaultNetwork[i], this.proc[i], this.actUid[i]);
            if (j == -1) {
                this.operations[i] = 0;
            } else {
                this.operations[i] = stats.operations[j];
            }
        }
    }

    public String[] getUniqueIfaces() {
        HashSet<String> ifaces = new HashSet<>();
        String[] strArr = this.iface;
        for (String iface2 : strArr) {
            if (iface2 != IFACE_ALL) {
                ifaces.add(iface2);
            }
        }
        return (String[]) ifaces.toArray(new String[ifaces.size()]);
    }

    @UnsupportedAppUsage
    public int[] getUniqueUids() {
        SparseBooleanArray uids = new SparseBooleanArray();
        for (int uid2 : this.uid) {
            uids.put(uid2, true);
        }
        int size2 = uids.size();
        int[] result = new int[size2];
        for (int i = 0; i < size2; i++) {
            result[i] = uids.keyAt(i);
        }
        return result;
    }

    @UnsupportedAppUsage
    public long getTotalBytes() {
        Entry entry = getTotal(null);
        return entry.rxBytes + entry.txBytes;
    }

    @UnsupportedAppUsage
    public Entry getTotal(Entry recycle) {
        return getTotal(recycle, null, -1, false);
    }

    @UnsupportedAppUsage
    public Entry getTotal(Entry recycle, int limitUid) {
        return getTotal(recycle, null, limitUid, false);
    }

    public Entry getTotal(Entry recycle, HashSet<String> limitIface) {
        return getTotal(recycle, limitIface, -1, false);
    }

    @UnsupportedAppUsage
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
        entry.defaultNetwork = -1;
        entry.rxBytes = 0;
        entry.rxPackets = 0;
        entry.txBytes = 0;
        entry.txPackets = 0;
        entry.operations = 0;
        entry.rxBytes_mp = 0;
        entry.rxPackets_mp = 0;
        entry.txBytes_mp = 0;
        entry.txPackets_mp = 0;
        for (int i = 0; i < this.size; i++) {
            boolean matchesIface = true;
            boolean matchesUid = limitUid == -1 || limitUid == this.uid[i];
            if (limitIface != null && !limitIface.contains(this.iface[i])) {
                matchesIface = false;
            }
            if (matchesUid && matchesIface && (this.tag[i] == 0 || includeTags)) {
                entry.rxBytes += this.rxBytes[i];
                entry.rxPackets += this.rxPackets[i];
                entry.txBytes += this.txBytes[i];
                entry.txPackets += this.txPackets[i];
                entry.operations += this.operations[i];
                entry.rxBytes_mp += this.rxBytes_mp[i];
                entry.rxPackets_mp += this.rxPackets_mp[i];
                entry.txBytes_mp += this.txBytes_mp[i];
                entry.txPackets_mp += this.txPackets_mp[i];
            }
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
        long deltaRealtime;
        NetworkStats result;
        long j;
        NetworkStats result2 = left;
        NetworkStats networkStats = right;
        long deltaRealtime2 = result2.elapsedRealtime - networkStats.elapsedRealtime;
        if (deltaRealtime2 < 0) {
            if (observer != null) {
                observer.foundNonMonotonic(left, -1, right, -1, cookie);
            }
            deltaRealtime = 0;
        } else {
            deltaRealtime = deltaRealtime2;
        }
        Entry entry = new Entry();
        if (recycle == null || recycle.capacity < result2.size) {
            result = new NetworkStats(deltaRealtime, result2.size);
        } else {
            recycle.size = 0;
            recycle.elapsedRealtime = deltaRealtime;
            result = recycle;
        }
        int[] leftIndex = new int[0];
        int[] rightIndex = new int[0];
        int i = 0;
        while (i < result2.size) {
            entry.iface = result2.iface[i];
            entry.uid = result2.uid[i];
            entry.set = result2.set[i];
            entry.tag = result2.tag[i];
            entry.metered = result2.metered[i];
            entry.roaming = result2.roaming[i];
            entry.defaultNetwork = result2.defaultNetwork[i];
            entry.rxBytes = result2.rxBytes[i];
            entry.rxPackets = result2.rxPackets[i];
            entry.txBytes = result2.txBytes[i];
            entry.txPackets = result2.txPackets[i];
            entry.operations = result2.operations[i];
            entry.proc = result2.proc[i];
            entry.actUid = result2.actUid[i];
            entry.rxBytes_mp = result2.rxBytes_mp[i];
            entry.rxPackets_mp = result2.rxPackets_mp[i];
            entry.txBytes_mp = result2.txBytes_mp[i];
            entry.txPackets_mp = result2.txPackets_mp[i];
            int j2 = right.findIndexHinted(entry.iface, entry.uid, entry.set, entry.tag, entry.metered, entry.roaming, entry.defaultNetwork, entry.proc, entry.actUid, i);
            if (j2 != -1) {
                entry.rxBytes -= networkStats.rxBytes[j2];
                entry.rxPackets -= networkStats.rxPackets[j2];
                entry.txBytes -= networkStats.txBytes[j2];
                entry.txPackets -= networkStats.txPackets[j2];
                entry.operations -= networkStats.operations[j2];
                entry.rxBytes_mp -= networkStats.rxBytes_mp[j2];
                entry.rxPackets_mp -= networkStats.rxPackets_mp[j2];
                entry.txBytes_mp -= networkStats.txBytes_mp[j2];
                entry.txPackets_mp -= networkStats.txPackets_mp[j2];
            }
            if (entry.isNegative()) {
                leftIndex = ArrayUtils.appendInt(leftIndex, i, true);
                rightIndex = ArrayUtils.appendInt(rightIndex, j2, true);
                j = 0;
                entry.rxBytes = Math.max(entry.rxBytes, 0L);
                entry.rxPackets = Math.max(entry.rxPackets, 0L);
                entry.txBytes = Math.max(entry.txBytes, 0L);
                entry.txPackets = Math.max(entry.txPackets, 0L);
                entry.operations = Math.max(entry.operations, 0L);
                entry.rxBytes_mp = entry.rxBytes_mp < 0 ? 0 : entry.rxBytes_mp;
                entry.rxPackets_mp = entry.rxPackets_mp < 0 ? 0 : entry.rxPackets_mp;
                entry.txBytes_mp = entry.txBytes_mp < 0 ? 0 : entry.txBytes_mp;
                entry.txPackets_mp = entry.txPackets_mp < 0 ? 0 : entry.txPackets_mp;
            } else {
                j = 0;
                rightIndex = rightIndex;
            }
            result.addValues(entry);
            i++;
            networkStats = networkStats;
            result = result;
            deltaRealtime = deltaRealtime;
            result2 = left;
        }
        if (observer != null && leftIndex.length > 0) {
            observer.foundNonMonotonic(left, leftIndex, right, rightIndex, cookie);
        }
        return result;
    }

    public static void apply464xlatAdjustments(NetworkStats baseTraffic, NetworkStats stackedTraffic, Map<String, String> stackedIfaces, boolean useBpfStats) {
        NetworkStats adjustments = new NetworkStats(0, stackedIfaces.size());
        Entry entry = null;
        Entry adjust = new Entry(IFACE_ALL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        for (int i = 0; i < stackedTraffic.size; i++) {
            entry = stackedTraffic.getValues(i, entry);
            if (entry.iface != null) {
                if (entry.iface.startsWith(CLATD_INTERFACE_PREFIX)) {
                    String baseIface = stackedIfaces.get(entry.iface);
                    if (baseIface != null) {
                        adjust.iface = baseIface;
                        if (!useBpfStats) {
                            adjust.rxBytes = -(entry.rxBytes + (entry.rxPackets * 20));
                            adjust.rxPackets = -entry.rxPackets;
                        }
                        adjust.rxBytes_mp = -(entry.rxBytes_mp + (entry.rxPackets_mp * 20));
                        adjust.txBytes_mp = -(entry.txBytes_mp + (entry.txPackets_mp * 20));
                        adjust.rxPackets_mp = -entry.rxPackets_mp;
                        adjust.txPackets_mp = -entry.txPackets_mp;
                        adjustments.combineValues(adjust);
                        entry.rxBytes += entry.rxPackets * 20;
                        entry.txBytes += entry.txPackets * 20;
                        entry.rxBytes_mp += entry.rxPackets_mp * 20;
                        entry.txBytes_mp += entry.txPackets_mp * 20;
                        stackedTraffic.setValues(i, entry);
                    }
                }
            }
        }
        baseTraffic.removeUids(new int[]{1029});
        baseTraffic.combineAllValues(adjustments);
    }

    public void apply464xlatAdjustments(Map<String, String> stackedIfaces, boolean useBpfStats) {
        apply464xlatAdjustments(this, this, stackedIfaces, useBpfStats);
    }

    public NetworkStats groupedByIface() {
        NetworkStats stats = new NetworkStats(this.elapsedRealtime, 10);
        Entry entry = new Entry();
        entry.uid = -1;
        entry.set = -1;
        entry.tag = 0;
        entry.metered = -1;
        entry.roaming = -1;
        entry.defaultNetwork = -1;
        entry.operations = 0;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.iface = this.iface[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                entry.rxBytes_mp = this.rxBytes_mp[i];
                entry.rxPackets_mp = this.rxPackets_mp[i];
                entry.txBytes_mp = this.txBytes_mp[i];
                entry.txPackets_mp = this.txPackets_mp[i];
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
        entry.defaultNetwork = -1;
        for (int i = 0; i < this.size; i++) {
            if (this.tag[i] == 0) {
                entry.uid = this.uid[i];
                entry.rxBytes = this.rxBytes[i];
                entry.rxPackets = this.rxPackets[i];
                entry.txBytes = this.txBytes[i];
                entry.txPackets = this.txPackets[i];
                entry.operations = this.operations[i];
                entry.rxBytes_mp = this.rxBytes_mp[i];
                entry.rxPackets_mp = this.rxPackets_mp[i];
                entry.txBytes_mp = this.txBytes_mp[i];
                entry.txPackets_mp = this.txPackets_mp[i];
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    public void removeUids(int[] uids) {
        int nextOutputEntry = 0;
        for (int i = 0; i < this.size; i++) {
            if (!ArrayUtils.contains(uids, this.uid[i])) {
                maybeCopyEntry(nextOutputEntry, i);
                nextOutputEntry++;
            }
        }
        this.size = nextOutputEntry;
    }

    public void filter(int limitUid, String[] limitIfaces, int limitTag) {
        if (!(limitUid == -1 && limitTag == -1 && limitIfaces == INTERFACES_ALL)) {
            Entry entry = new Entry();
            int nextOutputEntry = 0;
            for (int i = 0; i < this.size; i++) {
                entry = getValues(i, entry);
                if ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag) && (limitIfaces == INTERFACES_ALL || ArrayUtils.contains(limitIfaces, entry.iface))) {
                    setValues(nextOutputEntry, entry);
                    nextOutputEntry++;
                }
            }
            this.size = nextOutputEntry;
        }
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
            pw.print(" defaultNetwork=");
            pw.print(defaultNetworkToString(this.defaultNetwork[i]));
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
            pw.print(" rxBytes_mp=");
            pw.print(this.rxBytes_mp[i]);
            pw.print(" rxPackets_mp=");
            pw.print(this.rxPackets_mp[i]);
            pw.print(" txBytes_mp=");
            pw.print(this.txBytes_mp[i]);
            pw.print(" txPackets_mp=");
            pw.print(this.txPackets_mp[i]);
        }
    }

    public static String setToString(int set2) {
        if (set2 == -1) {
            return "ALL";
        }
        if (set2 == 0) {
            return "DEFAULT";
        }
        if (set2 == 1) {
            return "FOREGROUND";
        }
        if (set2 == 1001) {
            return "DBG_VPN_IN";
        }
        if (set2 != 1002) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "DBG_VPN_OUT";
    }

    public static String setToCheckinString(int set2) {
        if (set2 == -1) {
            return "all";
        }
        if (set2 == 0) {
            return "def";
        }
        if (set2 == 1) {
            return "fg";
        }
        if (set2 == 1001) {
            return "vpnin";
        }
        if (set2 != 1002) {
            return "unk";
        }
        return "vpnout";
    }

    public static boolean setMatches(int querySet, int dataSet) {
        if (querySet == dataSet) {
            return true;
        }
        if (querySet != -1 || dataSet >= 1000) {
            return false;
        }
        return true;
    }

    public static String tagToString(int tag2) {
        return "0x" + Integer.toHexString(tag2);
    }

    public static String meteredToString(int metered2) {
        if (metered2 == -1) {
            return "ALL";
        }
        if (metered2 == 0) {
            return "NO";
        }
        if (metered2 != 1) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "YES";
    }

    public static String roamingToString(int roaming2) {
        if (roaming2 == -1) {
            return "ALL";
        }
        if (roaming2 == 0) {
            return "NO";
        }
        if (roaming2 != 1) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "YES";
    }

    public static String defaultNetworkToString(int defaultNetwork2) {
        if (defaultNetwork2 == -1) {
            return "ALL";
        }
        if (defaultNetwork2 == 0) {
            return "NO";
        }
        if (defaultNetwork2 != 1) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "YES";
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump("", new PrintWriter(writer));
        return writer.toString();
    }

    @Override // android.os.Parcelable
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
        for (int i = 0; i < this.size; i++) {
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
        for (int i = 0; i < this.size; i++) {
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
                if (tunIfaceTotal.rxBytes_mp > 0) {
                    tmpEntry.rxBytes_mp = (pool.rxBytes_mp * this.rxBytes_mp[i]) / tunIfaceTotal.rxBytes_mp;
                } else {
                    tmpEntry.rxBytes_mp = 0;
                }
                if (tunIfaceTotal.rxPackets_mp > 0) {
                    tmpEntry.rxPackets_mp = (pool.rxPackets_mp * this.rxPackets_mp[i]) / tunIfaceTotal.rxPackets_mp;
                } else {
                    tmpEntry.rxPackets_mp = 0;
                }
                if (tunIfaceTotal.txBytes_mp > 0) {
                    tmpEntry.txBytes_mp = (pool.txBytes_mp * this.txBytes_mp[i]) / tunIfaceTotal.txBytes_mp;
                } else {
                    tmpEntry.txBytes_mp = 0;
                }
                if (tunIfaceTotal.txPackets_mp > 0) {
                    tmpEntry.txPackets_mp = (pool.txPackets_mp * this.txPackets_mp[i]) / tunIfaceTotal.txPackets_mp;
                } else {
                    tmpEntry.txPackets_mp = 0;
                }
                tmpEntry.uid = this.uid[i];
                tmpEntry.tag = this.tag[i];
                tmpEntry.set = this.set[i];
                tmpEntry.metered = this.metered[i];
                tmpEntry.roaming = this.roaming[i];
                tmpEntry.defaultNetwork = this.defaultNetwork[i];
                combineValues(tmpEntry);
                if (this.tag[i] == 0) {
                    moved.add(tmpEntry);
                    tmpEntry.set = 1001;
                    combineValues(tmpEntry);
                }
            }
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
        moved.defaultNetwork = -1;
        combineValues(moved);
        int idxVpnBackground = findIndex(underlyingIface, tunUid, 0, 0, 0, 0, 0);
        if (idxVpnBackground != -1) {
            tunSubtract(idxVpnBackground, this, moved);
        }
        int idxVpnForeground = findIndex(underlyingIface, tunUid, 1, 0, 0, 0, 0);
        if (idxVpnForeground != -1) {
            tunSubtract(idxVpnForeground, this, moved);
        }
    }

    private static void tunSubtract(int i, NetworkStats left, Entry right) {
        long rxBytes2 = Math.min(left.rxBytes[i], right.rxBytes);
        long[] jArr = left.rxBytes;
        jArr[i] = jArr[i] - rxBytes2;
        right.rxBytes -= rxBytes2;
        long rxPackets2 = Math.min(left.rxPackets[i], right.rxPackets);
        long[] jArr2 = left.rxPackets;
        jArr2[i] = jArr2[i] - rxPackets2;
        right.rxPackets -= rxPackets2;
        long txBytes2 = Math.min(left.txBytes[i], right.txBytes);
        long[] jArr3 = left.txBytes;
        jArr3[i] = jArr3[i] - txBytes2;
        right.txBytes -= txBytes2;
        long txPackets2 = Math.min(left.txPackets[i], right.txPackets);
        long[] jArr4 = left.txPackets;
        jArr4[i] = jArr4[i] - txPackets2;
        right.txPackets -= txPackets2;
    }
}
