package tmsdk.common.module.qscanner;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

/* compiled from: Unknown */
public class QScanAdPluginEntity implements Parcelable {
    public static final Creator<QScanAdPluginEntity> CREATOR = null;
    public ArrayList<String> banIps;
    public ArrayList<String> banUrls;
    public long behaviors;
    public int id;
    public String name;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.qscanner.QScanAdPluginEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.qscanner.QScanAdPluginEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.qscanner.QScanAdPluginEntity.<clinit>():void");
    }

    public QScanAdPluginEntity() {
        this.id = 0;
        this.type = 0;
        this.behaviors = 0;
        this.banUrls = null;
        this.banIps = null;
        this.name = null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.type);
        parcel.writeLong(this.behaviors);
        parcel.writeStringList(this.banUrls);
        parcel.writeStringList(this.banIps);
        parcel.writeString(this.name);
    }
}
