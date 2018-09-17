package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.StringTokenizer;

public final class SmartTrimProcessPkgResume extends SmartTrimProcessEvent {
    public static final Creator<SmartTrimProcessPkgResume> CREATOR = null;
    private static final String TAG = "SmartTrimProcessPkgResume";
    private static final boolean mDebugLocalClass = false;
    public String mPkgName;
    public String mProcessName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.smcs.SmartTrimProcessPkgResume.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.smcs.SmartTrimProcessPkgResume.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.smcs.SmartTrimProcessPkgResume.<clinit>():void");
    }

    SmartTrimProcessPkgResume(Parcel source) {
        super(source);
        this.mProcessName = null;
        this.mPkgName = null;
        readFromParcel(source);
    }

    SmartTrimProcessPkgResume(Parcel source, int event) {
        super(event);
        this.mProcessName = null;
        this.mPkgName = null;
        readFromParcel(source);
    }

    public SmartTrimProcessPkgResume(String sPkg, String processName) {
        super(1);
        this.mProcessName = null;
        this.mPkgName = null;
        this.mPkgName = sPkg;
        this.mProcessName = processName;
    }

    SmartTrimProcessPkgResume(StringTokenizer stzer) {
        super(1);
        this.mProcessName = null;
        this.mPkgName = null;
    }

    public int hashCode() {
        try {
            String sHashCode = this.mProcessName + "_" + this.mPkgName;
            if (sHashCode == null || sHashCode.length() <= 0) {
                return -1;
            }
            return sHashCode.hashCode();
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessPkgResume.hashCode: catch exception " + e.toString());
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof SmartTrimProcessPkgResume)) {
                return false;
            }
            SmartTrimProcessPkgResume input = (SmartTrimProcessPkgResume) o;
            if (input.mProcessName.equals(this.mProcessName) && input.mPkgName.equals(this.mPkgName)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessPkgResume.equals: catch exception " + e.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessPkgResume:\n");
        sb.append("process: " + this.mProcessName + "\n");
        sb.append("pkg: " + this.mPkgName + "\n");
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mPkgName);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mProcessName = source.readString();
        this.mPkgName = source.readString();
    }
}
