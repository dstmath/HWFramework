package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PresResInfo implements Parcelable {
    public static final Creator<PresResInfo> CREATOR = null;
    private String mDisplayName;
    private PresResInstanceInfo mInstanceInfo;
    private String mResUri;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.presence.PresResInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.presence.PresResInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.presence.PresResInfo.<clinit>():void");
    }

    public PresResInstanceInfo getInstanceInfo() {
        return this.mInstanceInfo;
    }

    public void setInstanceInfo(PresResInstanceInfo instanceInfo) {
        this.mInstanceInfo = instanceInfo;
    }

    public String getResUri() {
        return this.mResUri;
    }

    public void setResUri(String resUri) {
        this.mResUri = resUri;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public PresResInfo() {
        this.mResUri = "";
        this.mDisplayName = "";
        this.mInstanceInfo = new PresResInstanceInfo();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mResUri);
        dest.writeString(this.mDisplayName);
        dest.writeParcelable(this.mInstanceInfo, flags);
    }

    private PresResInfo(Parcel source) {
        this.mResUri = "";
        this.mDisplayName = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mResUri = source.readString();
        this.mDisplayName = source.readString();
        this.mInstanceInfo = (PresResInstanceInfo) source.readParcelable(PresResInstanceInfo.class.getClassLoader());
    }
}
