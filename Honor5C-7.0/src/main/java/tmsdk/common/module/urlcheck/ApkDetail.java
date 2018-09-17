package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

/* compiled from: Unknown */
public class ApkDetail implements Parcelable {
    public static Creator<ApkDetail> CREATOR;
    public String apkName;
    public String apkPackage;
    public String certMD5;
    public String description;
    public String developer;
    public int downloadCount;
    public String iconUrl;
    public ArrayList<String> imageUrls;
    public boolean isInSoftwareDB;
    public int official;
    public ArrayList<String> sensitivePermissions;
    public long size;
    public String source;
    public int versionCode;
    public String versionName;
    public String virsusDescription;
    public String virsusName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.urlcheck.ApkDetail.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.urlcheck.ApkDetail.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.urlcheck.ApkDetail.<clinit>():void");
    }

    public ApkDetail() {
        this.apkPackage = "";
        this.apkName = "";
        this.iconUrl = "";
        this.versionCode = 0;
        this.versionName = "";
        this.size = 0;
        this.official = 0;
        this.developer = "";
        this.certMD5 = "";
        this.isInSoftwareDB = false;
        this.description = "";
        this.imageUrls = null;
        this.downloadCount = 0;
        this.source = "";
        this.sensitivePermissions = null;
        this.virsusName = "";
        this.virsusDescription = "";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeString(this.apkPackage);
        parcel.writeString(this.apkName);
        parcel.writeString(this.iconUrl);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.versionName);
        parcel.writeLong(this.size);
        parcel.writeInt(this.official);
        parcel.writeString(this.developer);
        parcel.writeString(this.certMD5);
        if (this.isInSoftwareDB) {
            i2 = 1;
        }
        parcel.writeInt(i2);
        parcel.writeString(this.description);
        parcel.writeStringList(this.imageUrls);
        parcel.writeInt(this.downloadCount);
        parcel.writeString(this.source);
        parcel.writeStringList(this.sensitivePermissions);
        parcel.writeString(this.virsusName);
        parcel.writeString(this.virsusDescription);
    }
}
