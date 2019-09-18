package android.os;

import android.annotation.SystemApi;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class WorkSource implements Parcelable {
    public static final Parcelable.Creator<WorkSource> CREATOR = new Parcelable.Creator<WorkSource>() {
        public WorkSource createFromParcel(Parcel in) {
            return new WorkSource(in);
        }

        public WorkSource[] newArray(int size) {
            return new WorkSource[size];
        }
    };
    static final boolean DEBUG = false;
    static final boolean DEBUG_I = true;
    static final String TAG = "WorkSource";
    static WorkSource sGoneWork;
    static WorkSource sNewbWork;
    static final WorkSource sTmpWorkSource = new WorkSource(0);
    private ArrayList<WorkChain> mChains;
    String[] mNames;
    int mNum;
    int[] mUids;

    @SystemApi
    public static final class WorkChain implements Parcelable {
        public static final Parcelable.Creator<WorkChain> CREATOR = new Parcelable.Creator<WorkChain>() {
            public WorkChain createFromParcel(Parcel in) {
                return new WorkChain(in);
            }

            public WorkChain[] newArray(int size) {
                return new WorkChain[size];
            }
        };
        private int mSize;
        private String[] mTags;
        private int[] mUids;

        public WorkChain() {
            this.mSize = 0;
            this.mUids = new int[4];
            this.mTags = new String[4];
        }

        @VisibleForTesting
        public WorkChain(WorkChain other) {
            this.mSize = other.mSize;
            this.mUids = (int[]) other.mUids.clone();
            this.mTags = (String[]) other.mTags.clone();
        }

        private WorkChain(Parcel in) {
            this.mSize = in.readInt();
            this.mUids = in.createIntArray();
            this.mTags = in.createStringArray();
        }

        public WorkChain addNode(int uid, String tag) {
            if (this.mSize == this.mUids.length) {
                resizeArrays();
            }
            this.mUids[this.mSize] = uid;
            this.mTags[this.mSize] = tag;
            this.mSize++;
            return this;
        }

        public int getAttributionUid() {
            return this.mUids[0];
        }

        public String getAttributionTag() {
            return this.mTags[0];
        }

        @VisibleForTesting
        public int[] getUids() {
            int[] uids = new int[this.mSize];
            System.arraycopy(this.mUids, 0, uids, 0, this.mSize);
            return uids;
        }

        @VisibleForTesting
        public String[] getTags() {
            String[] tags = new String[this.mSize];
            System.arraycopy(this.mTags, 0, tags, 0, this.mSize);
            return tags;
        }

        @VisibleForTesting
        public int getSize() {
            return this.mSize;
        }

        private void resizeArrays() {
            int newSize = this.mSize * 2;
            int[] uids = new int[newSize];
            String[] tags = new String[newSize];
            System.arraycopy(this.mUids, 0, uids, 0, this.mSize);
            System.arraycopy(this.mTags, 0, tags, 0, this.mSize);
            this.mUids = uids;
            this.mTags = tags;
        }

        public String toString() {
            StringBuilder result = new StringBuilder("WorkChain{");
            for (int i = 0; i < this.mSize; i++) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append("(");
                result.append(this.mUids[i]);
                if (this.mTags[i] != null) {
                    result.append(", ");
                    result.append(this.mTags[i]);
                }
                result.append(")");
            }
            result.append("}");
            return result.toString();
        }

        public int hashCode() {
            return ((this.mSize + (Arrays.hashCode(this.mUids) * 31)) * 31) + Arrays.hashCode(this.mTags);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof WorkChain)) {
                return false;
            }
            WorkChain other = (WorkChain) o;
            if (this.mSize == other.mSize && Arrays.equals(this.mUids, other.mUids) && Arrays.equals(this.mTags, other.mTags)) {
                z = true;
            }
            return z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mSize);
            dest.writeIntArray(this.mUids);
            dest.writeStringArray(this.mTags);
        }
    }

    public WorkSource() {
        this.mNum = 0;
        this.mChains = null;
    }

    public WorkSource(WorkSource orig) {
        if (orig == null) {
            this.mNum = 0;
            this.mChains = null;
            return;
        }
        this.mNum = orig.mNum;
        if (orig.mUids != null) {
            this.mUids = (int[]) orig.mUids.clone();
            this.mNames = orig.mNames != null ? (String[]) orig.mNames.clone() : null;
        } else {
            this.mUids = null;
            this.mNames = null;
        }
        if (orig.mChains != null) {
            this.mChains = new ArrayList<>(orig.mChains.size());
            Iterator<WorkChain> it = orig.mChains.iterator();
            while (it.hasNext()) {
                this.mChains.add(new WorkChain(it.next()));
            }
        } else {
            this.mChains = null;
        }
    }

    public WorkSource(int uid) {
        this.mNum = 1;
        this.mUids = new int[]{uid, 0};
        this.mNames = null;
        this.mChains = null;
    }

    public WorkSource(int uid, String name) {
        if (name != null) {
            this.mNum = 1;
            this.mUids = new int[]{uid, 0};
            this.mNames = new String[]{name, null};
            this.mChains = null;
            return;
        }
        throw new NullPointerException("Name can't be null");
    }

    WorkSource(Parcel in) {
        this.mNum = in.readInt();
        this.mUids = in.createIntArray();
        this.mNames = in.createStringArray();
        int numChains = in.readInt();
        if (numChains > 0) {
            this.mChains = new ArrayList<>(numChains);
            in.readParcelableList(this.mChains, WorkChain.class.getClassLoader());
            return;
        }
        this.mChains = null;
    }

    public static boolean isChainedBatteryAttributionEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.CHAINED_BATTERY_ATTRIBUTION_ENABLED, 0) == 1;
    }

    public int size() {
        return this.mNum;
    }

    public int get(int index) {
        return this.mUids[index];
    }

    public String getName(int index) {
        if (this.mNames != null) {
            return this.mNames[index];
        }
        return null;
    }

    public void clearNames() {
        if (this.mNames != null) {
            this.mNames = null;
            int destIndex = 1;
            int newNum = this.mNum;
            for (int sourceIndex = 1; sourceIndex < this.mNum; sourceIndex++) {
                if (this.mUids[sourceIndex] == this.mUids[sourceIndex - 1]) {
                    newNum--;
                } else {
                    this.mUids[destIndex] = this.mUids[sourceIndex];
                    destIndex++;
                }
            }
            this.mNum = newNum;
        }
    }

    public void clear() {
        this.mNum = 0;
        if (this.mChains != null) {
            this.mChains.clear();
        }
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof WorkSource)) {
            return false;
        }
        WorkSource other = (WorkSource) o;
        if (diff(other)) {
            return false;
        }
        if (this.mChains != null && !this.mChains.isEmpty()) {
            return this.mChains.equals(other.mChains);
        }
        if (other.mChains == null || other.mChains.isEmpty()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int result = 0;
        for (int i2 = 0; i2 < this.mNum; i2++) {
            result = ((result << 4) | (result >>> 28)) ^ this.mUids[i2];
        }
        if (this.mNames != null) {
            while (true) {
                int i3 = i;
                if (i3 >= this.mNum) {
                    break;
                }
                result = ((result << 4) | (result >>> 28)) ^ this.mNames[i3].hashCode();
                i = i3 + 1;
            }
        }
        if (this.mChains != null) {
            return ((result << 4) | (result >>> 28)) ^ this.mChains.hashCode();
        }
        return result;
    }

    public boolean diff(WorkSource other) {
        int N = this.mNum;
        if (N != other.mNum) {
            return true;
        }
        int[] uids1 = this.mUids;
        int[] uids2 = other.mUids;
        String[] names1 = this.mNames;
        String[] names2 = other.mNames;
        for (int i = 0; i < N; i++) {
            if (uids1[i] != uids2[i]) {
                return true;
            }
            if (names1 != null && names2 != null && !names1[i].equals(names2[i])) {
                return true;
            }
        }
        return false;
    }

    public void set(WorkSource other) {
        if (other == null) {
            this.mNum = 0;
            if (this.mChains != null) {
                this.mChains.clear();
            }
            return;
        }
        this.mNum = other.mNum;
        if (other.mUids != null) {
            if (this.mUids == null || this.mUids.length < this.mNum) {
                this.mUids = (int[]) other.mUids.clone();
            } else {
                System.arraycopy(other.mUids, 0, this.mUids, 0, this.mNum);
            }
            if (other.mNames == null) {
                this.mNames = null;
            } else if (this.mNames == null || this.mNames.length < this.mNum) {
                this.mNames = (String[]) other.mNames.clone();
            } else {
                System.arraycopy(other.mNames, 0, this.mNames, 0, this.mNum);
            }
        } else {
            this.mUids = null;
            this.mNames = null;
        }
        if (other.mChains != null) {
            if (this.mChains != null) {
                this.mChains.clear();
            } else {
                this.mChains = new ArrayList<>(other.mChains.size());
            }
            Iterator<WorkChain> it = other.mChains.iterator();
            while (it.hasNext()) {
                this.mChains.add(new WorkChain(it.next()));
            }
        }
    }

    public void set(int uid) {
        this.mNum = 1;
        if (this.mUids == null) {
            this.mUids = new int[2];
        }
        this.mUids[0] = uid;
        this.mNames = null;
        if (this.mChains != null) {
            this.mChains.clear();
        }
    }

    public void set(int uid, String name) {
        if (name != null) {
            this.mNum = 1;
            if (this.mUids == null) {
                this.mUids = new int[2];
                this.mNames = new String[2];
            }
            this.mUids[0] = uid;
            this.mNames[0] = name;
            if (this.mChains != null) {
                this.mChains.clear();
                return;
            }
            return;
        }
        throw new NullPointerException("Name can't be null");
    }

    @Deprecated
    public WorkSource[] setReturningDiffs(WorkSource other) {
        synchronized (sTmpWorkSource) {
            sNewbWork = null;
            sGoneWork = null;
            updateLocked(other, true, true);
            if (sNewbWork == null) {
                if (sGoneWork == null) {
                    return null;
                }
            }
            WorkSource[] diffs = {sNewbWork, sGoneWork};
            return diffs;
        }
    }

    public boolean add(WorkSource other) {
        boolean z;
        synchronized (sTmpWorkSource) {
            z = false;
            boolean uidAdded = updateLocked(other, false, false);
            if (other.mChains != null) {
                if (this.mChains == null) {
                    this.mChains = new ArrayList<>(other.mChains.size());
                }
                Iterator<WorkChain> it = other.mChains.iterator();
                while (it.hasNext()) {
                    WorkChain wc = it.next();
                    if (!this.mChains.contains(wc)) {
                        this.mChains.add(new WorkChain(wc));
                    }
                }
            }
            if (!uidAdded) {
                if (0 != 0) {
                }
            }
            z = true;
        }
        return z;
    }

    @Deprecated
    public WorkSource addReturningNewbs(WorkSource other) {
        WorkSource workSource;
        synchronized (sTmpWorkSource) {
            sNewbWork = null;
            updateLocked(other, false, true);
            workSource = sNewbWork;
        }
        return workSource;
    }

    public boolean add(int uid) {
        if (this.mNum <= 0) {
            this.mNames = null;
            insert(0, uid);
            return true;
        } else if (this.mNames == null) {
            int i = Arrays.binarySearch(this.mUids, 0, this.mNum, uid);
            if (i >= 0) {
                return false;
            }
            insert((-i) - 1, uid);
            return true;
        } else {
            throw new IllegalArgumentException("Adding without name to named " + this);
        }
    }

    public boolean add(int uid, String name) {
        if (this.mNum <= 0) {
            insert(0, uid, name);
            return true;
        } else if (this.mNames != null) {
            int i = 0;
            while (i < this.mNum && this.mUids[i] <= uid) {
                if (this.mUids[i] == uid) {
                    int diff = this.mNames[i].compareTo(name);
                    if (diff > 0) {
                        break;
                    } else if (diff == 0) {
                        return false;
                    }
                }
                i++;
            }
            insert(i, uid, name);
            return true;
        } else {
            throw new IllegalArgumentException("Adding name to unnamed " + this);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0045, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008f, code lost:
        return false;
     */
    public boolean remove(WorkSource other) {
        boolean uidRemoved;
        synchronized (sTmpWorkSource) {
            boolean z = false;
            if (!isEmpty()) {
                if (!other.isEmpty()) {
                    if (this.mNames == null && other.mNames == null) {
                        uidRemoved = removeUids(other);
                    } else if (this.mNames == null) {
                        throw new IllegalArgumentException("Other " + other + " has names, but target " + this + " does not");
                    } else if (other.mNames != null) {
                        uidRemoved = removeUidsAndNames(other);
                    } else {
                        throw new IllegalArgumentException("Target " + this + " has names, but other " + other + " does not");
                    }
                    boolean chainRemoved = false;
                    if (!(other.mChains == null || this.mChains == null)) {
                        chainRemoved = this.mChains.removeAll(other.mChains);
                    }
                    if (!uidRemoved) {
                        if (chainRemoved) {
                        }
                    }
                    z = true;
                }
            }
        }
    }

    @SystemApi
    public WorkChain createWorkChain() {
        if (this.mChains == null) {
            this.mChains = new ArrayList<>(4);
        }
        WorkChain wc = new WorkChain();
        this.mChains.add(wc);
        return wc;
    }

    public boolean isEmpty() {
        return this.mNum == 0 && (this.mChains == null || this.mChains.isEmpty());
    }

    public ArrayList<WorkChain> getWorkChains() {
        return this.mChains;
    }

    public void transferWorkChains(WorkSource other) {
        if (this.mChains != null) {
            this.mChains.clear();
        }
        if (other.mChains != null && !other.mChains.isEmpty()) {
            if (this.mChains == null) {
                this.mChains = new ArrayList<>(4);
            }
            this.mChains.addAll(other.mChains);
            other.mChains.clear();
        }
    }

    private boolean removeUids(WorkSource other) {
        int N1 = this.mNum;
        int[] uids1 = this.mUids;
        int N2 = other.mNum;
        int[] uids2 = other.mUids;
        boolean changed = false;
        int i1 = 0;
        int i2 = 0;
        while (i1 < N1 && i2 < N2) {
            if (uids2[i2] == uids1[i1]) {
                N1--;
                changed = true;
                if (i1 < N1) {
                    System.arraycopy(uids1, i1 + 1, uids1, i1, N1 - i1);
                }
                i2++;
            } else if (uids2[i2] > uids1[i1]) {
                i1++;
            } else {
                i2++;
            }
        }
        this.mNum = N1;
        return changed;
    }

    private boolean removeUidsAndNames(WorkSource other) {
        int N1 = this.mNum;
        int[] uids1 = this.mUids;
        String[] names1 = this.mNames;
        int N2 = other.mNum;
        int[] uids2 = other.mUids;
        String[] names2 = other.mNames;
        boolean changed = false;
        int i1 = 0;
        int i2 = 0;
        while (i1 < N1 && i2 < N2) {
            if (uids2[i2] == uids1[i1] && names2[i2].equals(names1[i1])) {
                N1--;
                changed = true;
                if (i1 < N1) {
                    System.arraycopy(uids1, i1 + 1, uids1, i1, N1 - i1);
                    System.arraycopy(names1, i1 + 1, names1, i1, N1 - i1);
                }
                i2++;
            } else if (uids2[i2] > uids1[i1] || (uids2[i2] == uids1[i1] && names2[i2].compareTo(names1[i1]) > 0)) {
                i1++;
            } else {
                i2++;
            }
        }
        this.mNum = N1;
        return changed;
    }

    private boolean updateLocked(WorkSource other, boolean set, boolean returnNewbs) {
        if (this.mNames == null && other.mNames == null) {
            return updateUidsLocked(other, set, returnNewbs);
        }
        if (this.mNum > 0 && this.mNames == null) {
            throw new IllegalArgumentException("Other " + other + " has names, but target " + this + " does not");
        } else if (other.mNum <= 0 || other.mNames != null) {
            return updateUidsAndNamesLocked(other, set, returnNewbs);
        } else {
            throw new IllegalArgumentException("Target " + this + " has names, but other " + other + " does not");
        }
    }

    private static WorkSource addWork(WorkSource cur, int newUid) {
        if (cur == null) {
            return new WorkSource(newUid);
        }
        cur.insert(cur.mNum, newUid);
        return cur;
    }

    private boolean updateUidsLocked(WorkSource other, boolean set, boolean returnNewbs) {
        int N1 = this.mNum;
        int[] uids1 = this.mUids;
        int N2 = other.mNum;
        int[] uids2 = other.mUids;
        int i1 = 0;
        boolean changed = false;
        int[] uids12 = uids1;
        int N12 = N1;
        int i2 = 0;
        while (true) {
            if (i1 >= N12 && i2 >= N2) {
                this.mNum = N12;
                this.mUids = uids12;
                return changed;
            } else if (i1 >= N12 || (i2 < N2 && uids2[i2] < uids12[i1])) {
                changed = true;
                if (uids12 == null) {
                    uids12 = new int[4];
                    uids12[0] = uids2[i2];
                } else if (N12 >= uids12.length) {
                    int[] newuids = new int[((uids12.length * 3) / 2)];
                    if (i1 > 0) {
                        System.arraycopy(uids12, 0, newuids, 0, i1);
                    }
                    if (i1 < N12) {
                        System.arraycopy(uids12, i1, newuids, i1 + 1, N12 - i1);
                    }
                    uids12 = newuids;
                    uids12[i1] = uids2[i2];
                } else {
                    if (i1 < N12) {
                        System.arraycopy(uids12, i1, uids12, i1 + 1, N12 - i1);
                    }
                    uids12[i1] = uids2[i2];
                }
                if (returnNewbs) {
                    sNewbWork = addWork(sNewbWork, uids2[i2]);
                }
                N12++;
                i1++;
                i2++;
            } else if (!set) {
                if (i2 < N2 && uids2[i2] == uids12[i1]) {
                    i2++;
                }
                i1++;
            } else {
                int i12 = i1;
                while (i12 < N12 && (i2 >= N2 || uids2[i2] > uids12[i12])) {
                    sGoneWork = addWork(sGoneWork, uids12[i12]);
                    i12++;
                }
                if (i1 < i12) {
                    System.arraycopy(uids12, i12, uids12, i1, N12 - i12);
                    N12 -= i12 - i1;
                    i12 = i1;
                }
                if (i12 < N12 && i2 < N2 && uids2[i2] == uids12[i12]) {
                    i12++;
                    i2++;
                }
                i1 = i12;
            }
        }
    }

    private int compare(WorkSource other, int i1, int i2) {
        int diff = this.mUids[i1] - other.mUids[i2];
        if (diff != 0) {
            return diff;
        }
        return this.mNames[i1].compareTo(other.mNames[i2]);
    }

    private static WorkSource addWork(WorkSource cur, int newUid, String newName) {
        if (cur == null) {
            return new WorkSource(newUid, newName);
        }
        cur.insert(cur.mNum, newUid, newName);
        return cur;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001d, code lost:
        if (r7 > 0) goto L_0x007a;
     */
    private boolean updateUidsAndNamesLocked(WorkSource other, boolean set, boolean returnNewbs) {
        int N2 = other.mNum;
        int[] uids2 = other.mUids;
        String[] names2 = other.mNames;
        boolean changed = false;
        int i1 = 0;
        int i2 = 0;
        while (true) {
            if (i1 >= this.mNum && i2 >= N2) {
                return changed;
            }
            int diff = -1;
            if (i1 < this.mNum) {
                if (i2 < N2) {
                    int compare = compare(other, i1, i2);
                    diff = compare;
                }
                if (!set) {
                    if (i2 < N2 && diff == 0) {
                        i2++;
                    }
                    i1++;
                } else {
                    int i12 = i1;
                    while (diff < 0) {
                        sGoneWork = addWork(sGoneWork, this.mUids[i12], this.mNames[i12]);
                        i12++;
                        if (i12 >= this.mNum) {
                            break;
                        }
                        diff = i2 < N2 ? compare(other, i12, i2) : -1;
                    }
                    if (i1 < i12) {
                        System.arraycopy(this.mUids, i12, this.mUids, i1, this.mNum - i12);
                        System.arraycopy(this.mNames, i12, this.mNames, i1, this.mNum - i12);
                        this.mNum -= i12 - i1;
                        i12 = i1;
                    }
                    if (i12 < this.mNum && diff == 0) {
                        i12++;
                        i2++;
                    }
                    i1 = i12;
                }
            }
            changed = true;
            Log.d("WorkSource", "i1=" + i1 + " i2=" + i2 + " N1=" + this.mNum + ": insert " + uids2[i2] + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + names2[i2]);
            insert(i1, uids2[i2], names2[i2]);
            if (returnNewbs) {
                sNewbWork = addWork(sNewbWork, uids2[i2], names2[i2]);
            }
            i1++;
            i2++;
        }
    }

    private void insert(int index, int uid) {
        if (this.mUids == null) {
            this.mUids = new int[4];
            this.mUids[0] = uid;
            this.mNum = 1;
        } else if (this.mNum >= this.mUids.length) {
            int[] newuids = new int[((this.mNum * 3) / 2)];
            if (index > 0) {
                System.arraycopy(this.mUids, 0, newuids, 0, index);
            }
            if (index < this.mNum) {
                System.arraycopy(this.mUids, index, newuids, index + 1, this.mNum - index);
            }
            this.mUids = newuids;
            this.mUids[index] = uid;
            this.mNum++;
        } else {
            if (index < this.mNum) {
                System.arraycopy(this.mUids, index, this.mUids, index + 1, this.mNum - index);
            }
            this.mUids[index] = uid;
            this.mNum++;
        }
    }

    private void insert(int index, int uid, String name) {
        Log.d("WorkSource", "Insert in " + this + " @ " + index + " uid " + uid + " name " + name);
        if (this.mUids == null) {
            this.mUids = new int[4];
            this.mUids[0] = uid;
            this.mNames = new String[4];
            this.mNames[0] = name;
            this.mNum = 1;
        } else if (this.mNum >= this.mUids.length) {
            int[] newuids = new int[((this.mNum * 3) / 2)];
            String[] newnames = new String[((this.mNum * 3) / 2)];
            if (index > 0) {
                System.arraycopy(this.mUids, 0, newuids, 0, index);
                System.arraycopy(this.mNames, 0, newnames, 0, index);
            }
            if (index < this.mNum) {
                System.arraycopy(this.mUids, index, newuids, index + 1, this.mNum - index);
                System.arraycopy(this.mNames, index, newnames, index + 1, this.mNum - index);
            }
            this.mUids = newuids;
            this.mNames = newnames;
            this.mUids[index] = uid;
            this.mNames[index] = name;
            this.mNum++;
        } else {
            if (index < this.mNum) {
                System.arraycopy(this.mUids, index, this.mUids, index + 1, this.mNum - index);
                System.arraycopy(this.mNames, index, this.mNames, index + 1, this.mNum - index);
            }
            this.mUids[index] = uid;
            this.mNames[index] = name;
            this.mNum++;
        }
    }

    public static ArrayList<WorkChain>[] diffChains(WorkSource oldWs, WorkSource newWs) {
        ArrayList<WorkChain> newChains = null;
        ArrayList<WorkChain> goneChains = null;
        if (oldWs.mChains != null) {
            ArrayList<WorkChain> goneChains2 = null;
            for (int i = 0; i < oldWs.mChains.size(); i++) {
                WorkChain wc = oldWs.mChains.get(i);
                if (newWs.mChains == null || !newWs.mChains.contains(wc)) {
                    if (goneChains2 == null) {
                        goneChains2 = new ArrayList<>(oldWs.mChains.size());
                    }
                    goneChains2.add(wc);
                }
            }
            goneChains = goneChains2;
        }
        if (newWs.mChains != null) {
            ArrayList<WorkChain> newChains2 = null;
            for (int i2 = 0; i2 < newWs.mChains.size(); i2++) {
                WorkChain wc2 = newWs.mChains.get(i2);
                if (oldWs.mChains == null || !oldWs.mChains.contains(wc2)) {
                    if (newChains2 == null) {
                        newChains2 = new ArrayList<>(newWs.mChains.size());
                    }
                    newChains2.add(wc2);
                }
            }
            newChains = newChains2;
        }
        if (newChains == null && goneChains == null) {
            return null;
        }
        return new ArrayList[]{newChains, goneChains};
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNum);
        dest.writeIntArray(this.mUids);
        dest.writeStringArray(this.mNames);
        if (this.mChains == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(this.mChains.size());
        dest.writeParcelableList(this.mChains, flags);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("WorkSource{");
        for (int i = 0; i < this.mNum; i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(this.mUids[i]);
            if (this.mNames != null) {
                result.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                result.append(this.mNames[i]);
            }
        }
        if (this.mChains != null) {
            result.append(" chains=");
            for (int i2 = 0; i2 < this.mChains.size(); i2++) {
                if (i2 != 0) {
                    result.append(", ");
                }
                result.append(this.mChains.get(i2));
            }
        }
        result.append("}");
        return result.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long j;
        ProtoOutputStream protoOutputStream = proto;
        long workSourceToken = proto.start(fieldId);
        int i = 0;
        while (true) {
            j = 2246267895809L;
            if (i >= this.mNum) {
                break;
            }
            long contentProto = protoOutputStream.start(2246267895809L);
            protoOutputStream.write(1120986464257L, this.mUids[i]);
            if (this.mNames != null) {
                protoOutputStream.write(1138166333442L, this.mNames[i]);
            }
            protoOutputStream.end(contentProto);
            i++;
        }
        if (this.mChains != null) {
            int i2 = 0;
            while (i2 < this.mChains.size()) {
                WorkChain wc = this.mChains.get(i2);
                long workChain = protoOutputStream.start(2246267895810L);
                String[] tags = wc.getTags();
                int[] uids = wc.getUids();
                int j2 = 0;
                while (true) {
                    int j3 = j2;
                    if (j3 >= tags.length) {
                        break;
                    }
                    long contentProto2 = protoOutputStream.start(j);
                    protoOutputStream.write(1120986464257L, uids[j3]);
                    protoOutputStream.write(1138166333442L, tags[j3]);
                    protoOutputStream.end(contentProto2);
                    j2 = j3 + 1;
                    j = 2246267895809L;
                }
                protoOutputStream.end(workChain);
                i2++;
                j = 2246267895809L;
            }
        }
        protoOutputStream.end(workSourceToken);
    }
}
