package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.MSimTelephonyConstants;

public class CompressInfo implements Parcelable {
    public static final int COLLECT_LOG = 0;
    public static final int COMPRESS_LOG = 1;
    public static final Creator<CompressInfo> CREATOR = null;
    public static final int FINISHED = 2;
    public String description;
    public String path;
    public int progress;
    public int status;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.lcagent.client.CompressInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.lcagent.client.CompressInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.lcagent.client.CompressInfo.<clinit>():void");
    }

    public CompressInfo() {
        this.status = COLLECT_LOG;
        this.progress = COLLECT_LOG;
        this.path = MSimTelephonyConstants.MY_RADIO_PLATFORM;
        this.description = MSimTelephonyConstants.MY_RADIO_PLATFORM;
    }

    private CompressInfo(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }

    public void setCompressInfo(int status, int progress, String path, String description) {
        this.status = status;
        this.progress = progress;
        this.path = path;
        this.description = description;
    }

    public int describeContents() {
        return COLLECT_LOG;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.progress);
        dest.writeString(this.path);
        dest.writeString(this.description);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("status = ").append(this.status).append("\n");
        sb.append("progress = ").append(this.progress).append("%").append("\n");
        sb.append("path = ").append(this.path).append("\n");
        sb.append("description = ").append(this.description).append("\n");
        return sb.toString();
    }

    public void readFromParcel(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }
}
