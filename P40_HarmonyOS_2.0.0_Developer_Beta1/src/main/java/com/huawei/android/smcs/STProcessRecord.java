package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

public final class STProcessRecord implements Parcelable {
    public static final Parcelable.Creator<STProcessRecord> CREATOR = new Parcelable.Creator<STProcessRecord>() {
        /* class com.huawei.android.smcs.STProcessRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public STProcessRecord createFromParcel(Parcel source) {
            return new STProcessRecord(source);
        }

        @Override // android.os.Parcelable.Creator
        public STProcessRecord[] newArray(int size) {
            return new STProcessRecord[size];
        }
    };
    private static final boolean IS_DEDUG_LOCAL_CLASS = false;
    private static final String TAG = "STProcessRecord";
    public int curAdj;
    public int pid;
    public HashSet<String> pkgList = null;
    public String processName = null;
    public int uid;

    public STProcessRecord(String processName2, int uid2, int pid2, int curAdj2, HashSet<String> pkgList2) {
        this.processName = processName2;
        this.uid = uid2;
        this.pid = pid2;
        this.curAdj = curAdj2;
        this.pkgList = pkgList2;
    }

    STProcessRecord(Parcel source) {
        readFromParcel(source);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hashCode(this.processName);
    }

    @Override // java.lang.Object
    public boolean equals(Object target) {
        if (target != null && (target instanceof STProcessRecord) && Objects.equals(((STProcessRecord) target).processName, this.processName)) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("STProcessRecord: " + System.lineSeparator());
        sb.append(" processName: " + this.processName + System.lineSeparator());
        sb.append(" curAdj " + this.curAdj + System.lineSeparator());
        sb.append(" pkgs: " + this.pkgList + System.lineSeparator());
        sb.append(" uid " + this.uid + System.lineSeparator());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(" pid ");
        sb2.append(this.pid);
        sb.append(sb2.toString());
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.processName);
        dest.writeInt(this.uid);
        dest.writeInt(this.pid);
        dest.writeInt(this.curAdj);
        String[] pkgs = new String[this.pkgList.size()];
        Iterator<String> it = this.pkgList.iterator();
        int i = 0;
        while (it.hasNext()) {
            pkgs[i] = it.next();
            i++;
        }
        dest.writeStringArray(pkgs);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.processName = source.readString();
        this.uid = source.readInt();
        this.pid = source.readInt();
        this.curAdj = source.readInt();
        String[] pkgs = source.readStringArray();
        if (pkgs != null) {
            int length = pkgs.length;
            this.pkgList = new HashSet<>();
            for (String pkgName : pkgs) {
                this.pkgList.add(pkgName);
            }
        }
    }
}
