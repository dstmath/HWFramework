package com.android.server.net;

import android.net.NetworkIdentity;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.telephony.SubscriptionPlan;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.IntArray;
import android.util.MathUtils;
import android.util.Range;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FileRotator;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import libcore.io.IoUtils;

public class NetworkStatsCollection implements FileRotator.Reader {
    private static final int FILE_MAGIC = 1095648596;
    private static final String TAG = "NetworkStatsCollection";
    private static final int VERSION_NETWORK_INIT = 1;
    private static final int VERSION_UID_INIT = 1;
    private static final int VERSION_UID_PROC_UNIFIED_INIT = 17;
    private static final int VERSION_UID_WITH_IDENT = 2;
    private static final int VERSION_UID_WITH_SET = 4;
    private static final int VERSION_UID_WITH_TAG = 3;
    private static final int VERSION_UNIFIED_INIT = 16;
    private final long mBucketDuration;
    private boolean mDirty;
    private long mEndMillis;
    private long mStartMillis;
    private ArrayMap<Key, NetworkStatsHistory> mStats = new ArrayMap<>();
    private long mTotalBytes;

    private static class Key implements Comparable<Key> {
        public final int actUid;
        private final int hashCode;
        public final NetworkIdentitySet ident;
        public final String proc;
        public final int set;
        public final int tag;
        public final int uid;

        public Key(NetworkIdentitySet ident2, int uid2, int set2, int tag2) {
            this(ident2, uid2, set2, tag2, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }

        public Key(NetworkIdentitySet ident2, int uid2, int set2, int tag2, String proc2) {
            this(ident2, uid2, set2, tag2, proc2, -1);
        }

        public Key(NetworkIdentitySet ident2, int uid2, int set2, int tag2, String proc2, int actUid2) {
            this.ident = ident2;
            this.uid = uid2;
            this.set = set2;
            this.tag = tag2;
            this.proc = proc2;
            this.actUid = actUid2;
            this.hashCode = Objects.hash(new Object[]{ident2, Integer.valueOf(uid2), Integer.valueOf(set2), Integer.valueOf(tag2), proc2, Integer.valueOf(actUid2)});
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Key)) {
                return false;
            }
            Key key = (Key) obj;
            if (this.uid == key.uid && this.set == key.set && this.tag == key.tag && Objects.equals(this.ident, key.ident) && this.proc.equals(key.proc) && this.actUid == key.actUid) {
                z = true;
            }
            return z;
        }

        public int compareTo(Key another) {
            int res = 0;
            if (!(this.ident == null || another.ident == null)) {
                res = this.ident.compareTo(another.ident);
            }
            if (res == 0) {
                res = Integer.compare(this.uid, another.uid);
            }
            if (res == 0) {
                res = Integer.compare(this.set, another.set);
            }
            if (res == 0) {
                res = Integer.compare(this.tag, another.tag);
            }
            if (!(res != 0 || this.proc == null || another.proc == null)) {
                res = this.proc.compareTo(another.proc);
            }
            if (res == 0) {
                return Integer.compare(this.actUid, another.actUid);
            }
            return res;
        }
    }

    public NetworkStatsCollection(long bucketDuration) {
        this.mBucketDuration = bucketDuration;
        reset();
    }

    public void clear() {
        reset();
    }

    public void reset() {
        this.mStats.clear();
        this.mStartMillis = JobStatus.NO_LATEST_RUNTIME;
        this.mEndMillis = Long.MIN_VALUE;
        this.mTotalBytes = 0;
        this.mDirty = false;
    }

    public long getStartMillis() {
        return this.mStartMillis;
    }

    public long getFirstAtomicBucketMillis() {
        if (this.mStartMillis == JobStatus.NO_LATEST_RUNTIME) {
            return JobStatus.NO_LATEST_RUNTIME;
        }
        return this.mStartMillis + this.mBucketDuration;
    }

    public long getEndMillis() {
        return this.mEndMillis;
    }

    public long getTotalBytes() {
        return this.mTotalBytes;
    }

    public boolean isDirty() {
        return this.mDirty;
    }

    public void clearDirty() {
        this.mDirty = false;
    }

    public boolean isEmpty() {
        return this.mStartMillis == JobStatus.NO_LATEST_RUNTIME && this.mEndMillis == Long.MIN_VALUE;
    }

    @VisibleForTesting
    public long roundUp(long time) {
        if (time == Long.MIN_VALUE || time == JobStatus.NO_LATEST_RUNTIME || time == -1) {
            return time;
        }
        long mod = time % this.mBucketDuration;
        if (mod > 0) {
            time = (time - mod) + this.mBucketDuration;
        }
        return time;
    }

    @VisibleForTesting
    public long roundDown(long time) {
        if (time == Long.MIN_VALUE || time == JobStatus.NO_LATEST_RUNTIME || time == -1) {
            return time;
        }
        long mod = time % this.mBucketDuration;
        if (mod > 0) {
            time -= mod;
        }
        return time;
    }

    @VisibleForTesting
    public static long multiplySafe(long value, long num, long den) {
        long den2;
        if (den == 0) {
            den2 = 1;
        } else {
            den2 = den;
        }
        long x = value;
        long y = num;
        long r = x * y;
        if (((Math.abs(x) | Math.abs(y)) >>> 31) == 0 || ((y == 0 || r / y == x) && !(x == Long.MIN_VALUE && y == -1))) {
            long j = value;
            long j2 = x;
            return r / den2;
        }
        long j3 = x;
        return (long) ((((double) num) / ((double) den2)) * ((double) value));
    }

    public int[] getRelevantUids(int accessLevel) {
        return getRelevantUids(accessLevel, Binder.getCallingUid());
    }

    public int[] getRelevantUids(int accessLevel, int callerUid) {
        IntArray uids = new IntArray();
        for (int i = 0; i < this.mStats.size(); i++) {
            Key key = this.mStats.keyAt(i);
            if (NetworkStatsAccess.isAccessibleToUser(key.uid, callerUid, accessLevel)) {
                int j = uids.binarySearch(key.uid);
                if (j < 0) {
                    uids.add(~j, key.uid);
                }
            }
        }
        return uids.toArray();
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, SubscriptionPlan augmentPlan, int uid, int set, int tag, int fields, long start, long end, int accessLevel, int callerUid) {
        return getHistory(template, augmentPlan, uid, set, tag, fields, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, start, end, accessLevel, callerUid);
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, SubscriptionPlan augmentPlan, int uid, int set, int tag, int fields, String proc, long start, long end, int accessLevel, int callerUid) {
        return getHistory(template, augmentPlan, uid, set, tag, fields, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, -1, start, end, accessLevel, callerUid);
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, SubscriptionPlan augmentPlan, int uid, int set, int tag, int fields, String proc, int actUid, long start, long end, int accessLevel, int callerUid) {
        long augmentEnd;
        long collectEnd;
        long collectEnd2;
        long collectStart;
        long augmentStart;
        int i = uid;
        int i2 = fields;
        if (NetworkStatsAccess.isAccessibleToUser(i, callerUid, accessLevel)) {
            int bucketEstimate = (int) MathUtils.constrain((end - start) / this.mBucketDuration, 0, 15552000000L / this.mBucketDuration);
            NetworkStatsHistory combined = new NetworkStatsHistory(this.mBucketDuration, bucketEstimate, i2);
            if (start == end) {
                return combined;
            }
            if (augmentPlan != null) {
                augmentEnd = augmentPlan.getDataUsageTime();
            } else {
                augmentEnd = -1;
            }
            long collectStart2 = start;
            long collectEnd3 = end;
            long augmentEnd2 = augmentEnd;
            if (augmentEnd2 != -1) {
                Iterator<Range<ZonedDateTime>> it = augmentPlan.cycleIterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Range<ZonedDateTime> cycle = it.next();
                    long cycleStart = cycle.getLower().toInstant().toEpochMilli();
                    long cycleEnd = cycle.getUpper().toInstant().toEpochMilli();
                    if (cycleStart <= augmentEnd2 && augmentEnd2 < cycleEnd) {
                        Range<ZonedDateTime> range = cycle;
                        collectEnd = cycleStart;
                        collectStart = Long.min(collectStart2, collectEnd);
                        collectEnd2 = Long.max(collectEnd3, augmentEnd2);
                        break;
                    }
                    collectStart2 = collectStart2;
                    collectEnd3 = collectEnd3;
                    int i3 = callerUid;
                    int i4 = accessLevel;
                }
            }
            collectStart = collectStart2;
            collectEnd2 = collectEnd3;
            collectEnd = -1;
            if (collectEnd != -1) {
                collectEnd = roundUp(collectEnd);
                augmentEnd2 = roundDown(augmentEnd2);
                collectStart = roundDown(collectStart);
                collectEnd2 = roundUp(collectEnd2);
            }
            long collectStart3 = collectStart;
            long collectEnd4 = collectEnd2;
            long augmentEnd3 = augmentEnd2;
            int i5 = 0;
            while (i5 < this.mStats.size()) {
                Key key = this.mStats.keyAt(i5);
                if (key.uid == i) {
                    if (NetworkStats.setMatches(set, key.set) && key.tag == tag) {
                        if (templateMatches(template, key.ident) && Objects.equals(key.proc, proc)) {
                            if (key.actUid == actUid) {
                                combined.recordHistory(this.mStats.valueAt(i5), collectStart3, collectEnd4);
                            }
                            i5++;
                            i = uid;
                        }
                    }
                } else {
                    int i6 = set;
                }
                int i7 = actUid;
                i5++;
                i = uid;
            }
            int i8 = set;
            int i9 = actUid;
            if (collectEnd != -1) {
                NetworkStatsHistory.Entry entry = combined.getValues(collectEnd, augmentEnd3, null);
                if (entry.rxBytes == 0 || entry.txBytes == 0) {
                    NetworkStats.Entry entry2 = new NetworkStats.Entry(1, 0, 1, 0, 0);
                    NetworkStatsHistory networkStatsHistory = combined;
                    long j = collectEnd;
                    long j2 = augmentEnd3;
                    networkStatsHistory.recordData(j, j2, entry2);
                    networkStatsHistory.getValues(j, j2, entry);
                }
                long rawBytes = entry.rxBytes + entry.txBytes;
                long rawRxBytes = entry.rxBytes;
                long rawTxBytes = entry.txBytes;
                long dataUsageBytes = augmentPlan.getDataUsageBytes();
                long j3 = rawBytes;
                long targetRxBytes = multiplySafe(dataUsageBytes, rawRxBytes, j3);
                long targetTxBytes = multiplySafe(dataUsageBytes, rawTxBytes, j3);
                long beforeTotal = combined.getTotalBytes();
                int i10 = 0;
                while (true) {
                    int i11 = i10;
                    if (i11 >= combined.size()) {
                        break;
                    }
                    combined.getValues(i11, entry);
                    if (entry.bucketStart >= collectEnd) {
                        augmentStart = collectEnd;
                        if (entry.bucketStart + entry.bucketDuration <= augmentEnd3) {
                            entry.rxBytes = multiplySafe(targetRxBytes, entry.rxBytes, rawRxBytes);
                            entry.txBytes = multiplySafe(targetTxBytes, entry.txBytes, rawTxBytes);
                            entry.rxPackets = 0;
                            entry.txPackets = 0;
                            combined.setValues(i11, entry);
                        }
                    } else {
                        augmentStart = collectEnd;
                    }
                    i10 = i11 + 1;
                    collectEnd = augmentStart;
                    int i12 = actUid;
                    int i13 = fields;
                }
                long augmentStart2 = collectEnd;
                if (combined.getTotalBytes() - beforeTotal != 0) {
                    Slog.d(TAG, "Augmented network usage by " + deltaTotal + " bytes");
                }
                NetworkStatsHistory sliced = new NetworkStatsHistory(this.mBucketDuration, bucketEstimate, fields);
                long j4 = rawTxBytes;
                NetworkStatsHistory.Entry entry3 = entry;
                NetworkStatsHistory networkStatsHistory2 = combined;
                sliced.recordHistory(combined, start, end);
                return sliced;
            }
            long j5 = collectEnd;
            return combined;
        }
        throw new SecurityException("Network stats history of uid " + uid + " is forbidden for caller " + callerUid);
    }

    public NetworkStats getSummary(NetworkTemplate template, long start, long end, int accessLevel, int callerUid) {
        int i;
        NetworkStats.Entry entry;
        NetworkStatsCollection networkStatsCollection = this;
        long now = System.currentTimeMillis();
        NetworkStats stats = new NetworkStats(end - start, 24);
        if (start == end) {
            return stats;
        }
        NetworkStats.Entry entry2 = new NetworkStats.Entry();
        NetworkStatsHistory.Entry historyEntry = null;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < networkStatsCollection.mStats.size()) {
                Key key = networkStatsCollection.mStats.keyAt(i3);
                if (templateMatches(template, key.ident)) {
                    if (NetworkStatsAccess.isAccessibleToUser(key.uid, callerUid, accessLevel) && key.set < 1000) {
                        i = i3;
                        Key key2 = key;
                        entry = entry2;
                        NetworkStatsHistory.Entry historyEntry2 = networkStatsCollection.mStats.valueAt(i3).getValues(start, end, now, historyEntry);
                        entry.iface = NetworkStats.IFACE_ALL;
                        entry.uid = key2.uid;
                        entry.set = key2.set;
                        entry.tag = key2.tag;
                        entry.defaultNetwork = key2.ident.areAllMembersOnDefaultNetwork() ? 1 : 0;
                        entry.metered = key2.ident.isAnyMemberMetered() ? 1 : 0;
                        entry.proc = key2.proc;
                        entry.actUid = key2.actUid;
                        entry.roaming = key2.ident.isAnyMemberRoaming() ? 1 : 0;
                        entry.rxBytes = historyEntry2.rxBytes;
                        entry.rxPackets = historyEntry2.rxPackets;
                        entry.txBytes = historyEntry2.txBytes;
                        entry.txPackets = historyEntry2.txPackets;
                        entry.operations = historyEntry2.operations;
                        entry.rxBytes_mp = historyEntry2.rxBytes_mp;
                        entry.rxPackets_mp = historyEntry2.rxPackets_mp;
                        entry.txBytes_mp = historyEntry2.txBytes_mp;
                        entry.txPackets_mp = historyEntry2.txPackets_mp;
                        if (!entry.isEmpty()) {
                            stats.combineValues(entry);
                        }
                        historyEntry = historyEntry2;
                        i2 = i + 1;
                        entry2 = entry;
                        networkStatsCollection = this;
                    }
                }
                i = i3;
                entry = entry2;
                i2 = i + 1;
                entry2 = entry;
                networkStatsCollection = this;
            } else {
                return stats;
            }
        }
    }

    public void recordData(NetworkIdentitySet ident, int uid, int set, int tag, long start, long end, NetworkStats.Entry entry) {
        int i = uid;
        NetworkStats.Entry entry2 = entry;
        NetworkStatsHistory history = findOrCreateHistory(ident, (i < 19959 || i > 19999) ? i : 5508, set, tag, entry2.proc, entry2.actUid);
        history.recordData(start, end, entry2);
        noteRecordedHistory(history.getStart(), history.getEnd(), entry2.rxBytes + entry2.txBytes);
    }

    private void recordHistory(Key key, NetworkStatsHistory history) {
        if (history.size() != 0) {
            noteRecordedHistory(history.getStart(), history.getEnd(), history.getTotalBytes());
            NetworkStatsHistory target = this.mStats.get(key);
            if (target == null) {
                target = new NetworkStatsHistory(history.getBucketDuration());
                this.mStats.put(key, target);
            }
            target.recordEntireHistory(history);
        }
    }

    public void recordCollection(NetworkStatsCollection another) {
        for (int i = 0; i < another.mStats.size(); i++) {
            recordHistory(another.mStats.keyAt(i), another.mStats.valueAt(i));
        }
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag) {
        return findOrCreateHistory(ident, uid, set, tag, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag, String proc) {
        return findOrCreateHistory(ident, uid, set, tag, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, -1);
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag, String proc, int actUid) {
        Key key = new Key(ident, uid, set, tag, proc, actUid);
        NetworkStatsHistory existing = this.mStats.get(key);
        NetworkStatsHistory updated = null;
        if (existing == null) {
            updated = new NetworkStatsHistory(this.mBucketDuration, 10);
        } else if (existing.getBucketDuration() != this.mBucketDuration) {
            updated = new NetworkStatsHistory(existing, this.mBucketDuration);
        }
        if (updated == null) {
            return existing;
        }
        this.mStats.put(key, updated);
        return updated;
    }

    public void read(InputStream in) throws IOException {
        read(new DataInputStream(in));
    }

    public void read(DataInputStream in) throws IOException {
        DataInputStream dataInputStream = in;
        if (in.readInt() == FILE_MAGIC) {
            boolean uidAndProcFlag = true;
            switch (in.readInt()) {
                case 16:
                    uidAndProcFlag = false;
                    Slog.d(TAG, "read net data,  uidAndProcFlag= " + false);
                    break;
                case 17:
                    break;
                default:
                    throw new ProtocolException("unexpected version: " + version);
            }
            int identSize = in.readInt();
            int i = 0;
            while (i < identSize) {
                NetworkIdentitySet ident = new NetworkIdentitySet(dataInputStream);
                int size = in.readInt();
                int j = 0;
                while (true) {
                    int j2 = j;
                    if (j2 < size) {
                        int uid = in.readInt();
                        int set = in.readInt();
                        int tag = in.readInt();
                        String proc = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                        if (in.readByte() != 0) {
                            proc = in.readUTF();
                        }
                        String proc2 = proc;
                        int actUid = -1;
                        if (uidAndProcFlag) {
                            actUid = in.readInt();
                        }
                        Key key = new Key(ident, uid, set, tag, proc2, actUid);
                        recordHistory(key, new NetworkStatsHistory(dataInputStream));
                        j = j2 + 1;
                    } else {
                        i++;
                    }
                }
            }
            return;
        }
        throw new ProtocolException("unexpected magic: " + magic);
    }

    public void write(DataOutputStream out) throws IOException {
        HashMap<NetworkIdentitySet, ArrayList<Key>> keysByIdent = Maps.newHashMap();
        for (Key key : this.mStats.keySet()) {
            ArrayList<Key> keys = keysByIdent.get(key.ident);
            if (keys == null) {
                keys = Lists.newArrayList();
                keysByIdent.put(key.ident, keys);
            }
            keys.add(key);
        }
        out.writeInt(FILE_MAGIC);
        out.writeInt(17);
        out.writeInt(keysByIdent.size());
        for (NetworkIdentitySet ident : keysByIdent.keySet()) {
            ArrayList<Key> keys2 = keysByIdent.get(ident);
            ident.writeToStream(out);
            out.writeInt(keys2.size());
            Iterator<Key> it = keys2.iterator();
            while (it.hasNext()) {
                Key key2 = it.next();
                NetworkStatsHistory history = this.mStats.get(key2);
                out.writeInt(key2.uid);
                out.writeInt(key2.set);
                out.writeInt(key2.tag);
                String proc = key2.proc;
                if (proc == null || BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(proc)) {
                    out.writeByte(0);
                } else {
                    out.writeByte(1);
                    out.writeUTF(proc);
                }
                out.writeInt(key2.actUid);
                history.writeToStream(out);
            }
        }
        out.flush();
    }

    @Deprecated
    public void readLegacyNetwork(File file) throws IOException {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            if (in.readInt() == FILE_MAGIC) {
                if (in.readInt() == 1) {
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        recordHistory(new Key(new NetworkIdentitySet(in), -1, -1, 0), new NetworkStatsHistory(in));
                    }
                    IoUtils.closeQuietly(in);
                    return;
                }
                throw new ProtocolException("unexpected version: " + version);
            }
            throw new ProtocolException("unexpected magic: " + magic);
        } catch (FileNotFoundException e) {
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
    }

    @Deprecated
    public void readLegacyUid(File file, boolean onlyTags) throws IOException {
        int set;
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            if (in.readInt() == FILE_MAGIC) {
                int version = in.readInt();
                switch (version) {
                    case 1:
                        boolean z = onlyTags;
                        break;
                    case 2:
                        boolean z2 = onlyTags;
                        break;
                    case 3:
                    case 4:
                        int identSize = in.readInt();
                        int i = 0;
                        while (i < identSize) {
                            NetworkIdentitySet ident = new NetworkIdentitySet(in);
                            int size = in.readInt();
                            int j = 0;
                            while (j < size) {
                                int uid = in.readInt();
                                if (version >= 4) {
                                    set = in.readInt();
                                } else {
                                    set = 0;
                                }
                                int tag = in.readInt();
                                Key key = new Key(ident, uid, set, tag);
                                NetworkStatsHistory history = new NetworkStatsHistory(in);
                                if ((tag == 0) != onlyTags) {
                                    recordHistory(key, history);
                                }
                                j++;
                                File file2 = file;
                            }
                            boolean z3 = onlyTags;
                            i++;
                            File file3 = file;
                        }
                        boolean z4 = onlyTags;
                        break;
                    default:
                        boolean z5 = onlyTags;
                        try {
                            throw new ProtocolException("unexpected version: " + version);
                        } catch (FileNotFoundException e) {
                            break;
                        } catch (Throwable th) {
                            th = th;
                            IoUtils.closeQuietly(in);
                            throw th;
                        }
                }
            } else {
                boolean z6 = onlyTags;
                throw new ProtocolException("unexpected magic: " + magic);
            }
        } catch (FileNotFoundException e2) {
            boolean z7 = onlyTags;
        } catch (Throwable th2) {
            th = th2;
            boolean z8 = onlyTags;
            IoUtils.closeQuietly(in);
            throw th;
        }
        IoUtils.closeQuietly(in);
    }

    public void removeUids(int[] uids) {
        ArrayList<Key> knownKeys = Lists.newArrayList();
        knownKeys.addAll(this.mStats.keySet());
        Iterator<Key> it = knownKeys.iterator();
        while (it.hasNext()) {
            Key key = it.next();
            if (ArrayUtils.contains(uids, key.uid)) {
                if (key.tag == 0) {
                    findOrCreateHistory(key.ident, -4, 0, 0).recordEntireHistory(this.mStats.get(key));
                }
                this.mStats.remove(key);
                this.mDirty = true;
            }
        }
    }

    private void noteRecordedHistory(long startMillis, long endMillis, long totalBytes) {
        if (startMillis < this.mStartMillis) {
            this.mStartMillis = startMillis;
        }
        if (endMillis > this.mEndMillis) {
            this.mEndMillis = endMillis;
        }
        this.mTotalBytes += totalBytes;
        this.mDirty = true;
    }

    private int estimateBuckets() {
        return (int) (Math.min(this.mEndMillis - this.mStartMillis, 3024000000L) / this.mBucketDuration);
    }

    private ArrayList<Key> getSortedKeys() {
        ArrayList<Key> keys = Lists.newArrayList();
        keys.addAll(this.mStats.keySet());
        Collections.sort(keys);
        return keys;
    }

    public void dump(IndentingPrintWriter pw) {
        Iterator<Key> it = getSortedKeys().iterator();
        while (it.hasNext()) {
            Key key = it.next();
            pw.print("ident=");
            pw.print(key.ident.toString());
            pw.print(" uid=");
            pw.print(key.uid);
            pw.print(" set=");
            pw.print(NetworkStats.setToString(key.set));
            pw.print(" tag=");
            pw.println(NetworkStats.tagToString(key.tag));
            pw.print(" proc=");
            pw.println(key.proc);
            pw.print(" actUid=");
            pw.print(key.actUid);
            pw.increaseIndent();
            this.mStats.get(key).dump(pw, true);
            pw.decreaseIndent();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long tag) {
        long start = proto.start(tag);
        Iterator<Key> it = getSortedKeys().iterator();
        while (it.hasNext()) {
            Key key = it.next();
            long startStats = proto.start(2246267895809L);
            long startKey = proto.start(1146756268033L);
            key.ident.writeToProto(proto, 1146756268033L);
            proto.write(1120986464258L, key.uid);
            proto.write(1120986464259L, key.set);
            proto.write(1120986464260L, key.tag);
            proto.end(startKey);
            this.mStats.get(key).writeToProto(proto, 1146756268034L);
            proto.end(startStats);
        }
        proto.end(start);
    }

    public void dumpCheckin(PrintWriter pw, long start, long end) {
        PrintWriter printWriter = pw;
        long j = start;
        long j2 = end;
        dumpCheckin(printWriter, j, j2, NetworkTemplate.buildTemplateMobileWildcard(), "cell");
        PrintWriter printWriter2 = pw;
        long j3 = start;
        long j4 = end;
        dumpCheckin(printWriter2, j3, j4, NetworkTemplate.buildTemplateWifiWildcard(), "wifi");
        dumpCheckin(printWriter, j, j2, NetworkTemplate.buildTemplateEthernet(), "eth");
        dumpCheckin(printWriter2, j3, j4, NetworkTemplate.buildTemplateBluetooth(), "bt");
    }

    private void dumpCheckin(PrintWriter pw, long start, long end, NetworkTemplate groupTemplate, String groupPrefix) {
        PrintWriter printWriter = pw;
        ArrayMap<Key, NetworkStatsHistory> grouped = new ArrayMap<>();
        for (int i = 0; i < this.mStats.size(); i++) {
            Key key = this.mStats.keyAt(i);
            NetworkStatsHistory value = this.mStats.valueAt(i);
            if (templateMatches(groupTemplate, key.ident) && key.set < 1000) {
                Key groupKey = new Key(null, key.uid, key.set, key.tag);
                NetworkStatsHistory groupHistory = grouped.get(groupKey);
                if (groupHistory == null) {
                    groupHistory = new NetworkStatsHistory(value.getBucketDuration());
                    grouped.put(groupKey, groupHistory);
                }
                groupHistory.recordHistory(value, start, end);
            }
        }
        NetworkTemplate networkTemplate = groupTemplate;
        for (int i2 = 0; i2 < grouped.size(); i2++) {
            Key key2 = grouped.keyAt(i2);
            NetworkStatsHistory value2 = grouped.valueAt(i2);
            if (value2.size() == 0) {
                String str = groupPrefix;
            } else {
                printWriter.print("c,");
                printWriter.print(groupPrefix);
                printWriter.print(',');
                printWriter.print(key2.uid);
                printWriter.print(',');
                printWriter.print(NetworkStats.setToCheckinString(key2.set));
                printWriter.print(',');
                printWriter.print(key2.tag);
                pw.println();
                value2.dumpCheckin(printWriter);
            }
        }
        String str2 = groupPrefix;
    }

    private static boolean templateMatches(NetworkTemplate template, NetworkIdentitySet identSet) {
        Iterator it = identSet.iterator();
        while (it.hasNext()) {
            if (template.matches((NetworkIdentity) it.next())) {
                return true;
            }
        }
        return false;
    }
}
