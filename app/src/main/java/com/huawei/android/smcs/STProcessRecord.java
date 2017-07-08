package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;

public final class STProcessRecord implements Parcelable {
    public static final Creator<STProcessRecord> CREATOR = null;
    private static final String TAG = "STProcessRecord";
    private static final boolean mDebugLocalClass = false;
    public int curAdj;
    public int pid;
    public HashSet<String> pkgList;
    public String processName;
    public int uid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.smcs.STProcessRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.smcs.STProcessRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.smcs.STProcessRecord.<clinit>():void");
    }

    public STProcessRecord(String processName, int uid, int pid, int curAdj, HashSet<String> pkgList) {
        this.processName = null;
        this.pkgList = null;
        this.processName = processName;
        this.uid = uid;
        this.pid = pid;
        this.curAdj = curAdj;
        this.pkgList = pkgList;
    }

    STProcessRecord(Parcel source) {
        this.processName = null;
        this.pkgList = null;
        readFromParcel(source);
    }

    public int hashCode() {
        return this.processName.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof STProcessRecord)) {
                return false;
            }
            if (((STProcessRecord) o).processName.equals(this.processName)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "STProcessRecord.equals: catch exception " + e.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("STProcessRecord: \n");
        sb.append("    processName: " + this.processName);
        sb.append("\n    curAdj " + this.curAdj);
        sb.append("\n    pkgs: " + this.pkgList);
        sb.append("\n    uid " + this.uid);
        sb.append("\n    pid " + this.pid);
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        dest.writeString(this.processName);
        dest.writeInt(this.uid);
        dest.writeInt(this.pid);
        dest.writeInt(this.curAdj);
        String[] pkgs = new String[this.pkgList.size()];
        Iterator<String> it = this.pkgList.iterator();
        while (it.hasNext()) {
            pkgs[i] = (String) it.next();
            i++;
        }
        dest.writeStringArray(pkgs);
    }

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
            this.pkgList = new HashSet();
            for (Object add : pkgs) {
                this.pkgList.add(add);
            }
        }
    }
}
