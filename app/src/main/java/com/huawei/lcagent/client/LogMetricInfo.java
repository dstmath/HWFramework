package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LogMetricInfo implements Parcelable {
    public static final Creator<LogMetricInfo> CREATOR = null;
    public String description;
    public String[] files;
    public long id;
    public String logDetailedInfo;
    public String path;
    public String zipTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.lcagent.client.LogMetricInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.lcagent.client.LogMetricInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.lcagent.client.LogMetricInfo.<clinit>():void");
    }

    public LogMetricInfo() {
        this.id = 0;
        this.description = null;
        this.files = null;
        this.path = null;
        this.zipTime = null;
        this.logDetailedInfo = null;
    }

    public LogMetricInfo(long id, String path, String description, String[] files, String zipTime, String logDetailedInfo) {
        this.id = id;
        this.path = path;
        this.description = description;
        this.zipTime = zipTime;
        this.logDetailedInfo = logDetailedInfo;
        if (files == null || files.length == 0) {
            this.files = null;
            return;
        }
        this.files = new String[files.length];
        int length = files.length;
        for (int i = 0; i < length; i++) {
            this.files[i] = files[i];
        }
    }

    private LogMetricInfo(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.description = in.readString();
        this.files = in.createStringArray();
        this.zipTime = in.readString();
        this.logDetailedInfo = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.description);
        dest.writeStringArray(this.files);
        dest.writeString(this.zipTime);
        dest.writeString(this.logDetailedInfo);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ").append(this.id).append("\n");
        sb.append("path = ").append(this.path).append("\n");
        sb.append("description = ").append(this.description).append("\n");
        if (this.files == null) {
            return sb.toString();
        }
        int length = this.files.length;
        for (int i = 0; i < length; i++) {
            sb.append("files[").append(i).append("]=").append(this.files[i]).append("\n");
        }
        sb.append("zipTime = ").append(this.zipTime).append("\n");
        sb.append("logDetailedInfo = ").append(this.logDetailedInfo).append("\n");
        return sb.toString();
    }
}
