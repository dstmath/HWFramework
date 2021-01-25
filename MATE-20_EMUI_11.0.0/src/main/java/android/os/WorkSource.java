package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class WorkSource implements Parcelable {
    public static final Parcelable.Creator<WorkSource> CREATOR = new Parcelable.Creator<WorkSource>() {
        /* class android.os.WorkSource.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WorkSource createFromParcel(Parcel in) {
            return new WorkSource(in);
        }

        @Override // android.os.Parcelable.Creator
        public WorkSource[] newArray(int size) {
            return new WorkSource[size];
        }
    };
    static final boolean DEBUG = false;
    static final String TAG = "WorkSource";
    static WorkSource sGoneWork;
    static WorkSource sNewbWork;
    static final WorkSource sTmpWorkSource = new WorkSource(0);
    private ArrayList<WorkChain> mChains;
    @UnsupportedAppUsage
    String[] mNames;
    @UnsupportedAppUsage
    int mNum;
    @UnsupportedAppUsage
    int[] mUids;

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
        int[] iArr = orig.mUids;
        if (iArr != null) {
            this.mUids = (int[]) iArr.clone();
            String[] strArr = orig.mNames;
            this.mNames = strArr != null ? (String[]) strArr.clone() : null;
        } else {
            this.mUids = null;
            this.mNames = null;
        }
        ArrayList<WorkChain> arrayList = orig.mChains;
        if (arrayList != null) {
            this.mChains = new ArrayList<>(arrayList.size());
            Iterator<WorkChain> it = orig.mChains.iterator();
            while (it.hasNext()) {
                this.mChains.add(new WorkChain(it.next()));
            }
            return;
        }
        this.mChains = null;
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

    @UnsupportedAppUsage
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

    public int getAttributionUid() {
        if (isEmpty()) {
            return -1;
        }
        return this.mNum > 0 ? this.mUids[0] : this.mChains.get(0).getAttributionUid();
    }

    public String getName(int index) {
        String[] strArr = this.mNames;
        if (strArr != null) {
            return strArr[index];
        }
        return null;
    }

    public void clearNames() {
        if (this.mNames != null) {
            this.mNames = null;
            int destIndex = 1;
            int newNum = this.mNum;
            for (int sourceIndex = 1; sourceIndex < this.mNum; sourceIndex++) {
                int[] iArr = this.mUids;
                if (iArr[sourceIndex] == iArr[sourceIndex - 1]) {
                    newNum--;
                } else {
                    iArr[destIndex] = iArr[sourceIndex];
                    destIndex++;
                }
            }
            this.mNum = newNum;
        }
    }

    public void clear() {
        this.mNum = 0;
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList != null) {
            arrayList.clear();
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof WorkSource)) {
            return false;
        }
        WorkSource other = (WorkSource) o;
        if (diff(other)) {
            return false;
        }
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList != null && !arrayList.isEmpty()) {
            return this.mChains.equals(other.mChains);
        }
        ArrayList<WorkChain> arrayList2 = other.mChains;
        if (arrayList2 == null || arrayList2.isEmpty()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 0;
        for (int i = 0; i < this.mNum; i++) {
            result = ((result << 4) | (result >>> 28)) ^ this.mUids[i];
        }
        if (this.mNames != null) {
            for (int i2 = 0; i2 < this.mNum; i2++) {
                result = ((result << 4) | (result >>> 28)) ^ this.mNames[i2].hashCode();
            }
        }
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList != null) {
            return ((result << 4) | (result >>> 28)) ^ arrayList.hashCode();
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
            if (!(names1 == null || names2 == null || names1[i].equals(names2[i]))) {
                return true;
            }
        }
        return false;
    }

    public void set(WorkSource other) {
        int i;
        int i2;
        if (other == null) {
            this.mNum = 0;
            ArrayList<WorkChain> arrayList = this.mChains;
            if (arrayList != null) {
                arrayList.clear();
                return;
            }
            return;
        }
        this.mNum = other.mNum;
        int[] iArr = other.mUids;
        if (iArr != null) {
            int[] iArr2 = this.mUids;
            if (iArr2 == null || iArr2.length < (i2 = this.mNum)) {
                this.mUids = (int[]) other.mUids.clone();
            } else {
                System.arraycopy(iArr, 0, iArr2, 0, i2);
            }
            String[] strArr = other.mNames;
            if (strArr != null) {
                String[] strArr2 = this.mNames;
                if (strArr2 == null || strArr2.length < (i = this.mNum)) {
                    this.mNames = (String[]) other.mNames.clone();
                } else {
                    System.arraycopy(strArr, 0, strArr2, 0, i);
                }
            } else {
                this.mNames = null;
            }
        } else {
            this.mUids = null;
            this.mNames = null;
        }
        ArrayList<WorkChain> arrayList2 = other.mChains;
        if (arrayList2 != null) {
            ArrayList<WorkChain> arrayList3 = this.mChains;
            if (arrayList3 != null) {
                arrayList3.clear();
            } else {
                this.mChains = new ArrayList<>(arrayList2.size());
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
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList != null) {
            arrayList.clear();
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
            ArrayList<WorkChain> arrayList = this.mChains;
            if (arrayList != null) {
                arrayList.clear();
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
            return new WorkSource[]{sNewbWork, sGoneWork};
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
            if (uidAdded || 0 != 0) {
                z = true;
            }
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
        int i = this.mNum;
        if (i <= 0) {
            this.mNames = null;
            insert(0, uid);
            return true;
        } else if (this.mNames == null) {
            int i2 = Arrays.binarySearch(this.mUids, 0, i, uid);
            if (i2 >= 0) {
                return false;
            }
            insert((-i2) - 1, uid);
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
            while (i < this.mNum) {
                int[] iArr = this.mUids;
                if (iArr[i] > uid) {
                    break;
                }
                if (iArr[i] == uid) {
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

    public boolean remove(WorkSource other) {
        boolean uidRemoved;
        ArrayList<WorkChain> arrayList;
        if (isEmpty() || other.isEmpty()) {
            return false;
        }
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
        ArrayList<WorkChain> arrayList2 = other.mChains;
        if (!(arrayList2 == null || (arrayList = this.mChains) == null)) {
            chainRemoved = arrayList.removeAll(arrayList2);
        }
        if (uidRemoved || chainRemoved) {
            return true;
        }
        return false;
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
        ArrayList<WorkChain> arrayList;
        return this.mNum == 0 && ((arrayList = this.mChains) == null || arrayList.isEmpty());
    }

    public ArrayList<WorkChain> getWorkChains() {
        return this.mChains;
    }

    public void transferWorkChains(WorkSource other) {
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList != null) {
            arrayList.clear();
        }
        ArrayList<WorkChain> arrayList2 = other.mChains;
        if (arrayList2 != null && !arrayList2.isEmpty()) {
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
        boolean changed = false;
        int i1 = 0;
        int i2 = 0;
        while (true) {
            if (i1 >= N1 && i2 >= N2) {
                this.mNum = N1;
                this.mUids = uids1;
                return changed;
            } else if (i1 >= N1 || (i2 < N2 && uids2[i2] < uids1[i1])) {
                changed = true;
                if (uids1 == null) {
                    uids1 = new int[4];
                    uids1[0] = uids2[i2];
                } else if (N1 >= uids1.length) {
                    int[] newuids = new int[((uids1.length * 3) / 2)];
                    if (i1 > 0) {
                        System.arraycopy(uids1, 0, newuids, 0, i1);
                    }
                    if (i1 < N1) {
                        System.arraycopy(uids1, i1, newuids, i1 + 1, N1 - i1);
                    }
                    uids1 = newuids;
                    uids1[i1] = uids2[i2];
                } else {
                    if (i1 < N1) {
                        System.arraycopy(uids1, i1, uids1, i1 + 1, N1 - i1);
                    }
                    uids1[i1] = uids2[i2];
                }
                if (returnNewbs) {
                    sNewbWork = addWork(sNewbWork, uids2[i2]);
                }
                N1++;
                i1++;
                i2++;
            } else if (!set) {
                if (i2 < N2 && uids2[i2] == uids1[i1]) {
                    i2++;
                }
                i1++;
            } else {
                while (i1 < N1 && (i2 >= N2 || uids2[i2] > uids1[i1])) {
                    sGoneWork = addWork(sGoneWork, uids1[i1]);
                    i1++;
                }
                if (i1 < i1) {
                    System.arraycopy(uids1, i1, uids1, i1, N1 - i1);
                    N1 -= i1 - i1;
                    i1 = i1;
                }
                if (i1 < N1 && i2 < N2 && uids2[i2] == uids1[i1]) {
                    i1++;
                    i2++;
                }
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
        if (r7 > 0) goto L_0x0074;
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
                    while (diff < 0) {
                        sGoneWork = addWork(sGoneWork, this.mUids[i1], this.mNames[i1]);
                        i1++;
                        if (i1 >= this.mNum) {
                            break;
                        }
                        diff = i2 < N2 ? compare(other, i1, i2) : -1;
                    }
                    if (i1 < i1) {
                        int[] iArr = this.mUids;
                        System.arraycopy(iArr, i1, iArr, i1, this.mNum - i1);
                        String[] strArr = this.mNames;
                        System.arraycopy(strArr, i1, strArr, i1, this.mNum - i1);
                        this.mNum -= i1 - i1;
                        i1 = i1;
                    }
                    if (i1 < this.mNum && diff == 0) {
                        i1++;
                        i2++;
                    }
                }
            }
            changed = true;
            insert(i1, uids2[i2], names2[i2]);
            if (returnNewbs) {
                sNewbWork = addWork(sNewbWork, uids2[i2], names2[i2]);
            }
            i1++;
            i2++;
        }
    }

    private void insert(int index, int uid) {
        int[] iArr = this.mUids;
        if (iArr == null) {
            this.mUids = new int[4];
            this.mUids[0] = uid;
            this.mNum = 1;
            return;
        }
        int i = this.mNum;
        if (i >= iArr.length) {
            int[] newuids = new int[((i * 3) / 2)];
            if (index > 0) {
                System.arraycopy(iArr, 0, newuids, 0, index);
            }
            int i2 = this.mNum;
            if (index < i2) {
                System.arraycopy(this.mUids, index, newuids, index + 1, i2 - index);
            }
            this.mUids = newuids;
            this.mUids[index] = uid;
            this.mNum++;
            return;
        }
        if (index < i) {
            System.arraycopy(iArr, index, iArr, index + 1, i - index);
        }
        this.mUids[index] = uid;
        this.mNum++;
    }

    private void insert(int index, int uid, String name) {
        int[] iArr = this.mUids;
        if (iArr == null) {
            this.mUids = new int[4];
            this.mUids[0] = uid;
            this.mNames = new String[4];
            this.mNames[0] = name;
            this.mNum = 1;
            return;
        }
        int i = this.mNum;
        if (i >= iArr.length) {
            int[] newuids = new int[((i * 3) / 2)];
            String[] newnames = new String[((i * 3) / 2)];
            if (index > 0) {
                System.arraycopy(iArr, 0, newuids, 0, index);
                System.arraycopy(this.mNames, 0, newnames, 0, index);
            }
            int i2 = this.mNum;
            if (index < i2) {
                System.arraycopy(this.mUids, index, newuids, index + 1, i2 - index);
                System.arraycopy(this.mNames, index, newnames, index + 1, this.mNum - index);
            }
            this.mUids = newuids;
            this.mNames = newnames;
            this.mUids[index] = uid;
            this.mNames[index] = name;
            this.mNum++;
            return;
        }
        if (index < i) {
            System.arraycopy(iArr, index, iArr, index + 1, i - index);
            String[] strArr = this.mNames;
            System.arraycopy(strArr, index, strArr, index + 1, this.mNum - index);
        }
        this.mUids[index] = uid;
        this.mNames[index] = name;
        this.mNum++;
    }

    @SystemApi
    public static final class WorkChain implements Parcelable {
        public static final Parcelable.Creator<WorkChain> CREATOR = new Parcelable.Creator<WorkChain>() {
            /* class android.os.WorkSource.WorkChain.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public WorkChain createFromParcel(Parcel in) {
                return new WorkChain(in);
            }

            @Override // android.os.Parcelable.Creator
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
            int[] iArr = this.mUids;
            int i = this.mSize;
            iArr[i] = uid;
            this.mTags[i] = tag;
            this.mSize = i + 1;
            return this;
        }

        public int getAttributionUid() {
            if (this.mSize > 0) {
                return this.mUids[0];
            }
            return -1;
        }

        public String getAttributionTag() {
            String[] strArr = this.mTags;
            if (strArr.length > 0) {
                return strArr[0];
            }
            return null;
        }

        @VisibleForTesting
        public int[] getUids() {
            int i = this.mSize;
            int[] uids = new int[i];
            System.arraycopy(this.mUids, 0, uids, 0, i);
            return uids;
        }

        @VisibleForTesting
        public String[] getTags() {
            int i = this.mSize;
            String[] tags = new String[i];
            System.arraycopy(this.mTags, 0, tags, 0, i);
            return tags;
        }

        @VisibleForTesting
        public int getSize() {
            return this.mSize;
        }

        private void resizeArrays() {
            int i = this.mSize;
            int newSize = i * 2;
            int[] uids = new int[newSize];
            String[] tags = new String[newSize];
            System.arraycopy(this.mUids, 0, uids, 0, i);
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
            if (!(o instanceof WorkChain)) {
                return false;
            }
            WorkChain other = (WorkChain) o;
            if (this.mSize != other.mSize || !Arrays.equals(this.mUids, other.mUids) || !Arrays.equals(this.mTags, other.mTags)) {
                return false;
            }
            return true;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mSize);
            dest.writeIntArray(this.mUids);
            dest.writeStringArray(this.mTags);
        }
    }

    public static ArrayList<WorkChain>[] diffChains(WorkSource oldWs, WorkSource newWs) {
        ArrayList<WorkChain> newChains = null;
        ArrayList<WorkChain> goneChains = null;
        if (oldWs.mChains != null) {
            for (int i = 0; i < oldWs.mChains.size(); i++) {
                WorkChain wc = oldWs.mChains.get(i);
                ArrayList<WorkChain> arrayList = newWs.mChains;
                if (arrayList == null || !arrayList.contains(wc)) {
                    if (goneChains == null) {
                        goneChains = new ArrayList<>(oldWs.mChains.size());
                    }
                    goneChains.add(wc);
                }
            }
        }
        if (newWs.mChains != null) {
            for (int i2 = 0; i2 < newWs.mChains.size(); i2++) {
                WorkChain wc2 = newWs.mChains.get(i2);
                ArrayList<WorkChain> arrayList2 = oldWs.mChains;
                if (arrayList2 == null || !arrayList2.contains(wc2)) {
                    if (newChains == null) {
                        newChains = new ArrayList<>(newWs.mChains.size());
                    }
                    newChains.add(wc2);
                }
            }
        }
        if (newChains == null && goneChains == null) {
            return null;
        }
        return new ArrayList[]{newChains, goneChains};
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNum);
        dest.writeIntArray(this.mUids);
        dest.writeStringArray(this.mNames);
        ArrayList<WorkChain> arrayList = this.mChains;
        if (arrayList == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(arrayList.size());
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
        long workSourceToken = proto.start(fieldId);
        int i = 0;
        while (true) {
            j = 2246267895809L;
            if (i >= this.mNum) {
                break;
            }
            long contentProto = proto.start(2246267895809L);
            proto.write(1120986464257L, this.mUids[i]);
            String[] strArr = this.mNames;
            if (strArr != null) {
                proto.write(1138166333442L, strArr[i]);
            }
            proto.end(contentProto);
            i++;
        }
        if (this.mChains != null) {
            int i2 = 0;
            while (i2 < this.mChains.size()) {
                WorkChain wc = this.mChains.get(i2);
                long workChain = proto.start(2246267895810L);
                String[] tags = wc.getTags();
                int[] uids = wc.getUids();
                int j2 = 0;
                while (j2 < tags.length) {
                    long contentProto2 = proto.start(j);
                    proto.write(1120986464257L, uids[j2]);
                    proto.write(1138166333442L, tags[j2]);
                    proto.end(contentProto2);
                    j2++;
                    j = 2246267895809L;
                }
                proto.end(workChain);
                i2++;
                j = 2246267895809L;
            }
        }
        proto.end(workSourceToken);
    }
}
