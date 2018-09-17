package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PresServiceInfo implements Parcelable {
    public static final Creator<PresServiceInfo> CREATOR = null;
    public static final int UCE_PRES_MEDIA_CAP_FULL_AUDIO_AND_VIDEO = 2;
    public static final int UCE_PRES_MEDIA_CAP_FULL_AUDIO_ONLY = 1;
    public static final int UCE_PRES_MEDIA_CAP_NONE = 0;
    public static final int UCE_PRES_MEDIA_CAP_UNKNOWN = 3;
    private int mMediaCap;
    private String mServiceDesc;
    private String mServiceID;
    private String mServiceVer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.presence.PresServiceInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.presence.PresServiceInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.presence.PresServiceInfo.<clinit>():void");
    }

    public int getMediaType() {
        return this.mMediaCap;
    }

    public void setMediaType(int nMediaCap) {
        this.mMediaCap = nMediaCap;
    }

    public String getServiceId() {
        return this.mServiceID;
    }

    public void setServiceId(String serviceID) {
        this.mServiceID = serviceID;
    }

    public String getServiceDesc() {
        return this.mServiceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.mServiceDesc = serviceDesc;
    }

    public String getServiceVer() {
        return this.mServiceVer;
    }

    public void setServiceVer(String serviceVer) {
        this.mServiceVer = serviceVer;
    }

    public PresServiceInfo() {
        this.mMediaCap = UCE_PRES_MEDIA_CAP_NONE;
        this.mServiceID = "";
        this.mServiceDesc = "";
        this.mServiceVer = "";
    }

    public int describeContents() {
        return UCE_PRES_MEDIA_CAP_NONE;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceID);
        dest.writeString(this.mServiceDesc);
        dest.writeString(this.mServiceVer);
        dest.writeInt(this.mMediaCap);
    }

    private PresServiceInfo(Parcel source) {
        this.mMediaCap = UCE_PRES_MEDIA_CAP_NONE;
        this.mServiceID = "";
        this.mServiceDesc = "";
        this.mServiceVer = "";
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mServiceID = source.readString();
        this.mServiceDesc = source.readString();
        this.mServiceVer = source.readString();
        this.mMediaCap = source.readInt();
    }
}
