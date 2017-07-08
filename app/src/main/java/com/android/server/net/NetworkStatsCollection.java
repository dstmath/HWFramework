package com.android.server.net;

import android.net.NetworkIdentity;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.util.ArrayMap;
import android.util.AtomicFile;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FileRotator.Reader;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.ProcessList;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import libcore.io.IoUtils;

public class NetworkStatsCollection implements Reader {
    private static final int FILE_MAGIC = 1095648596;
    private static final String TAG = "NetworkStatsCollection";
    private static final int VERSION_NETWORK_INIT = 1;
    private static final int VERSION_UID_INIT = 1;
    private static final int VERSION_UID_WITH_IDENT = 2;
    private static final int VERSION_UID_WITH_SET = 4;
    private static final int VERSION_UID_WITH_TAG = 3;
    private static final int VERSION_UNIFIED_INIT = 16;
    private final long mBucketDuration;
    private boolean mDirty;
    private long mEndMillis;
    private long mStartMillis;
    private ArrayMap<Key, NetworkStatsHistory> mStats;
    private long mTotalBytes;

    private static class Key implements Comparable<Key> {
        private final int hashCode;
        public final NetworkIdentitySet ident;
        public final String proc;
        public final int set;
        public final int tag;
        public final int uid;

        public Key(NetworkIdentitySet ident, int uid, int set, int tag) {
            this(ident, uid, set, tag, "");
        }

        public Key(NetworkIdentitySet ident, int uid, int set, int tag, String proc) {
            this.ident = ident;
            this.uid = uid;
            this.set = set;
            this.tag = tag;
            this.proc = proc;
            this.hashCode = Objects.hash(new Object[]{ident, Integer.valueOf(uid), Integer.valueOf(set), Integer.valueOf(tag), proc});
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
            if (this.uid == key.uid && this.set == key.set && this.tag == key.tag && Objects.equals(this.ident, key.ident)) {
                z = this.proc.equals(key.proc);
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
            if (res != 0 || this.proc == null || another.proc == null) {
                return res;
            }
            return this.proc.compareTo(another.proc);
        }
    }

    public int[] getRelevantUids(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.NetworkStatsCollection.getRelevantUids(int, int):int[]
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkStatsCollection.getRelevantUids(int, int):int[]");
    }

    public NetworkStatsCollection(long bucketDuration) {
        this.mStats = new ArrayMap();
        this.mBucketDuration = bucketDuration;
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

    public int[] getRelevantUids(int accessLevel) {
        return getRelevantUids(accessLevel, Binder.getCallingUid());
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, int accessLevel) {
        return getHistory(template, uid, set, tag, fields, Long.MIN_VALUE, JobStatus.NO_LATEST_RUNTIME, accessLevel);
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end, int accessLevel) {
        return getHistory(template, uid, set, tag, fields, start, end, accessLevel, Binder.getCallingUid());
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end, int accessLevel, int callerUid) {
        return getHistory(template, uid, set, tag, fields, "", start, end, accessLevel, callerUid);
    }

    public NetworkStatsHistory getHistory(NetworkTemplate template, int uid, int set, int tag, int fields, String proc, long start, long end, int accessLevel, int callerUid) {
        if (NetworkStatsAccess.isAccessibleToUser(uid, callerUid, accessLevel)) {
            NetworkStatsHistory combined = new NetworkStatsHistory(this.mBucketDuration, start == end ? VERSION_UID_INIT : estimateBuckets(), fields);
            if (start == end) {
                return combined;
            }
            for (int i = 0; i < this.mStats.size(); i += VERSION_UID_INIT) {
                Key key = (Key) this.mStats.keyAt(i);
                if (key.uid == uid && NetworkStats.setMatches(set, key.set) && key.tag == tag && templateMatches(template, key.ident) && Objects.equals(key.proc, proc)) {
                    combined.recordHistory((NetworkStatsHistory) this.mStats.valueAt(i), start, end);
                }
            }
            return combined;
        }
        throw new SecurityException("Network stats history of uid " + uid + " is forbidden for caller " + callerUid);
    }

    public NetworkStats getSummary(NetworkTemplate template, long start, long end, int accessLevel) {
        return getSummary(template, start, end, accessLevel, Binder.getCallingUid());
    }

    public NetworkStats getSummary(NetworkTemplate template, long start, long end, int accessLevel, int callerUid) {
        long now = System.currentTimeMillis();
        NetworkStats stats = new NetworkStats(end - start, 24);
        if (start == end) {
            return stats;
        }
        Entry entry = new Entry();
        NetworkStatsHistory.Entry historyEntry = null;
        for (int i = 0; i < this.mStats.size(); i += VERSION_UID_INIT) {
            Key key = (Key) this.mStats.keyAt(i);
            if (templateMatches(template, key.ident) && NetworkStatsAccess.isAccessibleToUser(key.uid, callerUid, accessLevel) && key.set < ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                historyEntry = ((NetworkStatsHistory) this.mStats.valueAt(i)).getValues(start, end, now, historyEntry);
                entry.iface = NetworkStats.IFACE_ALL;
                entry.uid = key.uid;
                entry.set = key.set;
                entry.tag = key.tag;
                entry.roaming = key.ident.isAnyMemberRoaming() ? VERSION_UID_INIT : 0;
                entry.rxBytes = historyEntry.rxBytes;
                entry.rxPackets = historyEntry.rxPackets;
                entry.txBytes = historyEntry.txBytes;
                entry.txPackets = historyEntry.txPackets;
                entry.operations = historyEntry.operations;
                if (!entry.isEmpty()) {
                    stats.combineValues(entry);
                }
            }
        }
        return stats;
    }

    public void recordData(NetworkIdentitySet ident, int uid, int set, int tag, long start, long end, Entry entry) {
        NetworkStatsHistory history = findOrCreateHistory(ident, uid, set, tag, entry.proc);
        history.recordData(start, end, entry);
        noteRecordedHistory(history.getStart(), history.getEnd(), entry.txBytes + entry.rxBytes);
    }

    private void recordHistory(Key key, NetworkStatsHistory history) {
        if (history.size() != 0) {
            noteRecordedHistory(history.getStart(), history.getEnd(), history.getTotalBytes());
            NetworkStatsHistory target = (NetworkStatsHistory) this.mStats.get(key);
            if (target == null) {
                target = new NetworkStatsHistory(history.getBucketDuration());
                this.mStats.put(key, target);
            }
            target.recordEntireHistory(history);
        }
    }

    public void recordCollection(NetworkStatsCollection another) {
        for (int i = 0; i < another.mStats.size(); i += VERSION_UID_INIT) {
            recordHistory((Key) another.mStats.keyAt(i), (NetworkStatsHistory) another.mStats.valueAt(i));
        }
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag) {
        return findOrCreateHistory(ident, uid, set, tag, "");
    }

    private NetworkStatsHistory findOrCreateHistory(NetworkIdentitySet ident, int uid, int set, int tag, String proc) {
        Key key = new Key(ident, uid, set, tag, proc);
        NetworkStatsHistory existing = (NetworkStatsHistory) this.mStats.get(key);
        NetworkStatsHistory networkStatsHistory = null;
        if (existing == null) {
            networkStatsHistory = new NetworkStatsHistory(this.mBucketDuration, 10);
        } else if (existing.getBucketDuration() != this.mBucketDuration) {
            networkStatsHistory = new NetworkStatsHistory(existing, this.mBucketDuration);
        }
        if (networkStatsHistory == null) {
            return existing;
        }
        this.mStats.put(key, networkStatsHistory);
        return networkStatsHistory;
    }

    public void read(InputStream in) throws IOException {
        read(new DataInputStream(in));
    }

    public void read(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != FILE_MAGIC) {
            throw new ProtocolException("unexpected magic: " + magic);
        }
        int version = in.readInt();
        switch (version) {
            case VERSION_UNIFIED_INIT /*16*/:
                int identSize = in.readInt();
                for (int i = 0; i < identSize; i += VERSION_UID_INIT) {
                    NetworkIdentitySet ident = new NetworkIdentitySet(in);
                    int size = in.readInt();
                    for (int j = 0; j < size; j += VERSION_UID_INIT) {
                        int uid = in.readInt();
                        int set = in.readInt();
                        int tag = in.readInt();
                        String proc = "";
                        if (in.readByte() != null) {
                            proc = in.readUTF();
                        }
                        recordHistory(new Key(ident, uid, set, tag, proc), new NetworkStatsHistory(in));
                    }
                }
            default:
                throw new ProtocolException("unexpected version: " + version);
        }
    }

    public void write(DataOutputStream out) throws IOException {
        HashMap<NetworkIdentitySet, ArrayList<Key>> keysByIdent = Maps.newHashMap();
        for (Key key : this.mStats.keySet()) {
            ArrayList<Key> keys = (ArrayList) keysByIdent.get(key.ident);
            if (keys == null) {
                keys = Lists.newArrayList();
                keysByIdent.put(key.ident, keys);
            }
            keys.add(key);
        }
        out.writeInt(FILE_MAGIC);
        out.writeInt(VERSION_UNIFIED_INIT);
        out.writeInt(keysByIdent.size());
        for (NetworkIdentitySet ident : keysByIdent.keySet()) {
            keys = (ArrayList) keysByIdent.get(ident);
            ident.writeToStream(out);
            out.writeInt(keys.size());
            for (Key key2 : keys) {
                NetworkStatsHistory history = (NetworkStatsHistory) this.mStats.get(key2);
                out.writeInt(key2.uid);
                out.writeInt(key2.set);
                out.writeInt(key2.tag);
                String proc = key2.proc;
                if (proc == null || "".equals(proc)) {
                    out.writeByte(0);
                } else {
                    out.writeByte(VERSION_UID_INIT);
                    out.writeUTF(proc);
                }
                history.writeToStream(out);
            }
        }
        out.flush();
    }

    @Deprecated
    public void readLegacyNetwork(File file) throws IOException {
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            try {
                int magic = in.readInt();
                if (magic != FILE_MAGIC) {
                    throw new ProtocolException("unexpected magic: " + magic);
                }
                int version = in.readInt();
                switch (version) {
                    case VERSION_UID_INIT /*1*/:
                        int size = in.readInt();
                        for (int i = 0; i < size; i += VERSION_UID_INIT) {
                            NetworkIdentitySet ident = new NetworkIdentitySet(in);
                            recordHistory(new Key(ident, -1, -1, 0), new NetworkStatsHistory(in));
                        }
                        IoUtils.closeQuietly(in);
                    default:
                        throw new ProtocolException("unexpected version: " + version);
                }
            } catch (FileNotFoundException e) {
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    @Deprecated
    public void readLegacyUid(File file, boolean onlyTags) throws IOException {
        Throwable th;
        AutoCloseable autoCloseable = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new AtomicFile(file).openRead()));
            try {
                int magic = in.readInt();
                if (magic != FILE_MAGIC) {
                    throw new ProtocolException("unexpected magic: " + magic);
                }
                int version = in.readInt();
                switch (version) {
                    case VERSION_UID_INIT /*1*/:
                    case VERSION_UID_WITH_IDENT /*2*/:
                        break;
                    case VERSION_UID_WITH_TAG /*3*/:
                    case VERSION_UID_WITH_SET /*4*/:
                        int identSize = in.readInt();
                        for (int i = 0; i < identSize; i += VERSION_UID_INIT) {
                            NetworkIdentitySet ident = new NetworkIdentitySet(in);
                            int size = in.readInt();
                            for (int j = 0; j < size; j += VERSION_UID_INIT) {
                                int set;
                                int uid = in.readInt();
                                if (version >= VERSION_UID_WITH_SET) {
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
                            }
                        }
                        break;
                    default:
                        throw new ProtocolException("unexpected version: " + version);
                }
                IoUtils.closeQuietly(in);
            } catch (FileNotFoundException e) {
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th2) {
                th = th2;
                autoCloseable = in;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(autoCloseable);
            throw th;
        }
    }

    public void removeUids(int[] uids) {
        ArrayList<Key> knownKeys = Lists.newArrayList();
        knownKeys.addAll(this.mStats.keySet());
        for (Key key : knownKeys) {
            if (ArrayUtils.contains(uids, key.uid)) {
                if (key.tag == 0) {
                    findOrCreateHistory(key.ident, -4, 0, 0).recordEntireHistory((NetworkStatsHistory) this.mStats.get(key));
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

    public void dump(IndentingPrintWriter pw) {
        ArrayList<Key> keys = Lists.newArrayList();
        keys.addAll(this.mStats.keySet());
        Collections.sort(keys);
        for (Key key : keys) {
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
            NetworkStatsHistory history = (NetworkStatsHistory) this.mStats.get(key);
            pw.increaseIndent();
            history.dump(pw, true);
            pw.decreaseIndent();
        }
    }

    public void dumpCheckin(PrintWriter pw, long start, long end) {
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateMobileWildcard(), "cell");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateWifiWildcard(), "wifi");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateEthernet(), "eth");
        dumpCheckin(pw, start, end, NetworkTemplate.buildTemplateBluetooth(), "bt");
    }

    private void dumpCheckin(PrintWriter pw, long start, long end, NetworkTemplate groupTemplate, String groupPrefix) {
        int i;
        ArrayMap<Key, NetworkStatsHistory> grouped = new ArrayMap();
        for (i = 0; i < this.mStats.size(); i += VERSION_UID_INIT) {
            Key key = (Key) this.mStats.keyAt(i);
            NetworkStatsHistory value = (NetworkStatsHistory) this.mStats.valueAt(i);
            if (templateMatches(groupTemplate, key.ident) && key.set < ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                Key groupKey = new Key(null, key.uid, key.set, key.tag);
                NetworkStatsHistory groupHistory = (NetworkStatsHistory) grouped.get(groupKey);
                if (groupHistory == null) {
                    groupHistory = new NetworkStatsHistory(value.getBucketDuration());
                    grouped.put(groupKey, groupHistory);
                }
                groupHistory.recordHistory(value, start, end);
            }
        }
        for (i = 0; i < grouped.size(); i += VERSION_UID_INIT) {
            key = (Key) grouped.keyAt(i);
            value = (NetworkStatsHistory) grouped.valueAt(i);
            if (value.size() != 0) {
                pw.print("c,");
                pw.print(groupPrefix);
                pw.print(',');
                pw.print(key.uid);
                pw.print(',');
                pw.print(NetworkStats.setToCheckinString(key.set));
                pw.print(',');
                pw.print(key.tag);
                pw.println();
                value.dumpCheckin(pw);
            }
        }
    }

    private static boolean templateMatches(NetworkTemplate template, NetworkIdentitySet identSet) {
        for (NetworkIdentity ident : identSet) {
            if (template.matches(ident)) {
                return true;
            }
        }
        return false;
    }
}
