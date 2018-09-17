package android.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcelable.Creator;
import android.os.WorkSourceProto.WorkSourceContentProto;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import java.util.Arrays;

public class WorkSource implements Parcelable {
    public static final Creator<WorkSource> CREATOR = new Creator<WorkSource>() {
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
    String[] mNames;
    int mNum;
    int[] mUids;

    public WorkSource() {
        this.mNum = 0;
    }

    public WorkSource(WorkSource orig) {
        if (orig == null) {
            this.mNum = 0;
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
    }

    public WorkSource(int uid) {
        this.mNum = 1;
        this.mUids = new int[]{uid, 0};
        this.mNames = null;
    }

    public WorkSource(int uid, String name) {
        if (name == null) {
            throw new NullPointerException("Name can't be null");
        }
        this.mNum = 1;
        this.mUids = new int[]{uid, 0};
        this.mNames = new String[]{name, null};
    }

    WorkSource(Parcel in) {
        this.mNum = in.readInt();
        this.mUids = in.createIntArray();
        this.mNames = in.createStringArray();
    }

    public int size() {
        return this.mNum;
    }

    public int get(int index) {
        return this.mUids[index];
    }

    public String getName(int index) {
        return this.mNames != null ? this.mNames[index] : null;
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
    }

    public boolean equals(Object o) {
        return o instanceof WorkSource ? diff((WorkSource) o) ^ 1 : false;
    }

    public int hashCode() {
        int i;
        int result = 0;
        for (i = 0; i < this.mNum; i++) {
            result = ((result << 4) | (result >>> 28)) ^ this.mUids[i];
        }
        if (this.mNames != null) {
            for (i = 0; i < this.mNum; i++) {
                result = ((result << 4) | (result >>> 28)) ^ this.mNames[i].hashCode();
            }
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
        int i = 0;
        while (i < N) {
            if (uids1[i] != uids2[i]) {
                return true;
            }
            if (names1 != null && names2 != null && (names1[i].equals(names2[i]) ^ 1) != 0) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void set(WorkSource other) {
        if (other == null) {
            this.mNum = 0;
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
    }

    public void set(int uid) {
        this.mNum = 1;
        if (this.mUids == null) {
            this.mUids = new int[2];
        }
        this.mUids[0] = uid;
        this.mNames = null;
    }

    public void set(int uid, String name) {
        if (name == null) {
            throw new NullPointerException("Name can't be null");
        }
        this.mNum = 1;
        if (this.mUids == null) {
            this.mUids = new int[2];
            this.mNames = new String[2];
        }
        this.mUids[0] = uid;
        this.mNames[0] = name;
    }

    public WorkSource[] setReturningDiffs(WorkSource other) {
        synchronized (sTmpWorkSource) {
            sNewbWork = null;
            sGoneWork = null;
            updateLocked(other, true, true);
            if (sNewbWork == null && sGoneWork == null) {
                return null;
            }
            WorkSource[] diffs = new WorkSource[]{sNewbWork, sGoneWork};
            return diffs;
        }
    }

    public boolean add(WorkSource other) {
        boolean updateLocked;
        synchronized (sTmpWorkSource) {
            updateLocked = updateLocked(other, false, false);
        }
        return updateLocked;
    }

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
        } else if (this.mNames != null) {
            throw new IllegalArgumentException("Adding without name to named " + this);
        } else {
            int i = Arrays.binarySearch(this.mUids, 0, this.mNum, uid);
            if (i >= 0) {
                return false;
            }
            insert((-i) - 1, uid);
            return true;
        }
    }

    public boolean add(int uid, String name) {
        if (this.mNum <= 0) {
            insert(0, uid, name);
            return true;
        } else if (this.mNames == null) {
            throw new IllegalArgumentException("Adding name to unnamed " + this);
        } else {
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
        }
    }

    public WorkSource addReturningNewbs(int uid) {
        WorkSource workSource;
        synchronized (sTmpWorkSource) {
            sNewbWork = null;
            sTmpWorkSource.mUids[0] = uid;
            updateLocked(sTmpWorkSource, false, true);
            workSource = sNewbWork;
        }
        return workSource;
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean remove(WorkSource other) {
        synchronized (sTmpWorkSource) {
            boolean removeUids;
            if (this.mNum <= 0 || other.mNum <= 0) {
            } else if (this.mNames == null && other.mNames == null) {
                removeUids = removeUids(other);
                return removeUids;
            } else if (this.mNames == null) {
                throw new IllegalArgumentException("Other " + other + " has names, but target " + this + " does not");
            } else if (other.mNames == null) {
                throw new IllegalArgumentException("Target " + this + " has names, but other " + other + " does not");
            } else {
                removeUids = removeUidsAndNames(other);
                return removeUids;
            }
        }
    }

    public WorkSource stripNames() {
        if (this.mNum <= 0) {
            return new WorkSource();
        }
        WorkSource result = new WorkSource();
        for (int i = 0; i < this.mNum; i++) {
            int uid = this.mUids[i];
            if (i == 0 || -1 != uid) {
                result.add(uid);
            }
        }
        return result;
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
                    newuids[i1] = uids2[i2];
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
            } else if (set) {
                int start = i1;
                while (i1 < N1 && (i2 >= N2 || uids2[i2] > uids1[i1])) {
                    sGoneWork = addWork(sGoneWork, uids1[i1]);
                    i1++;
                }
                if (start < i1) {
                    System.arraycopy(uids1, i1, uids1, start, N1 - i1);
                    N1 -= i1 - start;
                    i1 = start;
                }
                if (i1 < N1 && i2 < N2 && uids2[i2] == uids1[i1]) {
                    i1++;
                    i2++;
                }
            } else {
                if (i2 < N2 && uids2[i2] == uids1[i1]) {
                    i2++;
                }
                i1++;
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

    /* JADX WARNING: Missing block: B:8:0x001a, code:
            if (r2 > 0) goto L_0x001c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    diff = compare(other, i1, i2);
                }
                if (set) {
                    int start = i1;
                    while (diff < 0) {
                        sGoneWork = addWork(sGoneWork, this.mUids[i1], this.mNames[i1]);
                        i1++;
                        if (i1 >= this.mNum) {
                            break;
                        }
                        diff = i2 < N2 ? compare(other, i1, i2) : -1;
                    }
                    if (start < i1) {
                        System.arraycopy(this.mUids, i1, this.mUids, start, this.mNum - i1);
                        System.arraycopy(this.mNames, i1, this.mNames, start, this.mNum - i1);
                        this.mNum -= i1 - start;
                        i1 = start;
                    }
                    if (i1 < this.mNum && diff == 0) {
                        i1++;
                        i2++;
                    }
                } else {
                    if (i2 < N2 && diff == 0) {
                        i2++;
                    }
                    i1++;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNum);
        dest.writeIntArray(this.mUids);
        dest.writeStringArray(this.mNames);
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
        result.append("}");
        return result.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long workSourceToken = proto.start(fieldId);
        for (int i = 0; i < this.mNum; i++) {
            long contentProto = proto.start(2272037699585L);
            proto.write(WorkSourceContentProto.UID, this.mUids[i]);
            if (this.mNames != null) {
                proto.write(1159641169922L, this.mNames[i]);
            }
            proto.end(contentProto);
        }
        proto.end(workSourceToken);
    }
}
