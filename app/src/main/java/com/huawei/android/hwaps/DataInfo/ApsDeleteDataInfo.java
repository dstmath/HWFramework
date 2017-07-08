package com.huawei.android.hwaps.DataInfo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ApsDeleteDataInfo implements Parcelable {
    public static final Creator<ApsDeleteDataInfo> CREATOR = null;
    public String[] mSelectionArgs;
    public String mUri;
    public String mWhere;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.DataInfo.ApsDeleteDataInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.DataInfo.ApsDeleteDataInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.DataInfo.ApsDeleteDataInfo.<clinit>():void");
    }

    public ApsDeleteDataInfo(String strUri, String where, String[] selectionArgs) {
        this.mUri = null;
        this.mWhere = null;
        this.mSelectionArgs = null;
        this.mUri = strUri;
        this.mWhere = where;
        if (selectionArgs != null) {
            this.mSelectionArgs = (String[]) selectionArgs.clone();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        synchronized (this) {
            dest.writeString(this.mUri);
            dest.writeString(this.mWhere);
            dest.writeArray(this.mSelectionArgs);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.mUri != null) {
            sb.append("ApsUpdateDataInfo: Uri:").append(this.mUri);
        }
        if (this.mWhere != null) {
            sb.append(" where:").append(this.mWhere);
        }
        if (this.mSelectionArgs != null) {
            sb.append(" SelectionArgs:");
            for (String str : this.mSelectionArgs) {
                sb.append(str);
            }
        }
        return sb.toString();
    }
}
