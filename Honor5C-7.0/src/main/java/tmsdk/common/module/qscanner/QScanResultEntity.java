package tmsdk.common.module.qscanner;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class QScanResultEntity implements Parcelable, Serializable {
    public static final Creator<QScanResultEntity> CREATOR = null;
    public int advice;
    public int apkType;
    public int category;
    public String certMd5;
    public String dexSha1;
    public List<String> dirtyDataPathes;
    public String discription;
    public boolean isInPayList;
    public boolean isInStealAccountList;
    public String label;
    public int malwareid;
    public String name;
    public boolean needOpenAppMonitorToHandle;
    public boolean needRootToHandle;
    public int official;
    public String officialCertMd5;
    public String officialPackName;
    public String packageName;
    public String path;
    public ArrayList<QScanAdPluginEntity> plugins;
    public int product;
    public int safeLevel;
    public String shortDesc;
    public int size;
    public String softName;
    public int special;
    public int systemFlaw;
    public int type;
    public String url;
    public String version;
    public int versionCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.qscanner.QScanResultEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.qscanner.QScanResultEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.qscanner.QScanResultEntity.<clinit>():void");
    }

    public QScanResultEntity() {
        this.special = -1;
        this.systemFlaw = -1;
        this.isInPayList = false;
        this.isInStealAccountList = false;
        this.needRootToHandle = false;
        this.needOpenAppMonitorToHandle = false;
        this.product = 0;
        this.official = 0;
        this.category = 0;
    }

    public int describeContents() {
        return 0;
    }

    public String uniqueKey() {
        return this.systemFlaw == -1 ? (this.apkType == 0 || this.apkType == 1) ? this.packageName : this.apkType == 2 ? this.path : null : "flaw_" + this.systemFlaw;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeString(this.packageName);
        parcel.writeString(this.softName);
        parcel.writeString(this.version);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.path);
        parcel.writeInt(this.apkType);
        parcel.writeString(this.certMd5);
        parcel.writeInt(this.size);
        parcel.writeString(this.dexSha1);
        parcel.writeTypedList(this.plugins);
        parcel.writeString(this.name);
        parcel.writeInt(this.type);
        parcel.writeInt(this.advice);
        parcel.writeInt(this.malwareid);
        parcel.writeString(this.name);
        parcel.writeString(this.label);
        parcel.writeString(this.discription);
        parcel.writeString(this.url);
        parcel.writeInt(this.safeLevel);
        parcel.writeString(this.shortDesc);
        if (this.dirtyDataPathes == null || this.dirtyDataPathes.size() == 0) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(this.dirtyDataPathes.size());
            parcel.writeStringList(this.dirtyDataPathes);
        }
        parcel.writeInt(this.special);
        parcel.writeInt(this.systemFlaw);
        parcel.writeByte((byte) (!this.isInPayList ? 0 : 1));
        parcel.writeByte((byte) (!this.isInStealAccountList ? 0 : 1));
        parcel.writeByte((byte) (!this.needRootToHandle ? 0 : 1));
        if (this.needOpenAppMonitorToHandle) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
        parcel.writeInt(this.product);
        parcel.writeInt(this.category);
        parcel.writeString(this.officialPackName);
        parcel.writeString(this.officialCertMd5);
    }
}
