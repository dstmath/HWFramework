package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.HashSet;
import java.util.StringTokenizer;

public final class SmartTrimProcessAddRelation extends SmartTrimProcessEvent {
    public static final Creator<SmartTrimProcessAddRelation> CREATOR = null;
    private static final String TAG = "SmartTrimProcessAddRelation";
    private static final boolean mDebugLocalClass = false;
    public HashSet<String> mClientPkgList;
    public String mClientProc;
    public HashSet<String> mServerPkgList;
    public String mServerProc;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.smcs.SmartTrimProcessAddRelation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.smcs.SmartTrimProcessAddRelation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.smcs.SmartTrimProcessAddRelation.<clinit>():void");
    }

    public SmartTrimProcessAddRelation(String clientProc, HashSet<String> clientPkgList, String serverProc, HashSet<String> serverPkgList) {
        super(0);
        this.mClientPkgList = null;
        this.mServerPkgList = null;
        this.mClientProc = clientProc;
        this.mClientPkgList = clientPkgList;
        this.mServerProc = serverProc;
        this.mServerPkgList = serverPkgList;
    }

    SmartTrimProcessAddRelation(Parcel source) {
        super(source);
        this.mClientPkgList = null;
        this.mServerPkgList = null;
        readFromParcel(source);
    }

    SmartTrimProcessAddRelation(Parcel source, int event) {
        super(event);
        this.mClientPkgList = null;
        this.mServerPkgList = null;
        readFromParcel(source);
    }

    SmartTrimProcessAddRelation(StringTokenizer stzer) {
        super(0);
        this.mClientPkgList = null;
        this.mServerPkgList = null;
    }

    public int hashCode() {
        try {
            return (this.mClientProc + this.mServerProc).hashCode();
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessAddRelation.hashCode: catch exception " + e.toString());
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof SmartTrimProcessAddRelation)) {
                return false;
            }
            SmartTrimProcessAddRelation input = (SmartTrimProcessAddRelation) o;
            boolean clientEqual = input.mClientProc.equals(this.mClientProc);
            boolean serverEqual = input.mServerProc.equals(this.mServerProc);
            if (!clientEqual) {
                serverEqual = false;
            }
            if (serverEqual) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessAddRelation.equals: catch exception " + e.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessAddRelation:\n");
        sb.append("client process: " + this.mClientProc + "\n");
        sb.append("client pkg list: " + this.mClientPkgList + "\n");
        sb.append("server process: " + this.mServerProc + "\n");
        sb.append("server pkg list: " + this.mServerPkgList + "\n");
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mClientProc);
        dest.writeStringArray(hashSet2strings(this.mClientPkgList));
        dest.writeString(this.mServerProc);
        dest.writeStringArray(hashSet2strings(this.mServerPkgList));
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mClientProc = source.readString();
        this.mClientPkgList = strings2hashSet(source.readStringArray());
        this.mServerProc = source.readString();
        this.mServerPkgList = strings2hashSet(source.readStringArray());
    }
}
