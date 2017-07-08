package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PackageCleanItem implements Parcelable {
    public static final Creator<PackageCleanItem> CREATOR = null;
    public final boolean andCode;
    public final String packageName;
    public final int userId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageCleanItem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageCleanItem.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageCleanItem.<clinit>():void");
    }

    public PackageCleanItem(int userId, String packageName, boolean andCode) {
        this.userId = userId;
        this.packageName = packageName;
        this.andCode = andCode;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj != null) {
            try {
                PackageCleanItem other = (PackageCleanItem) obj;
                if (this.userId != other.userId || !this.packageName.equals(other.packageName)) {
                    z = false;
                } else if (this.andCode != other.andCode) {
                    z = false;
                }
                return z;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    public int hashCode() {
        return ((((this.userId + 527) * 31) + this.packageName.hashCode()) * 31) + (this.andCode ? 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.userId);
        dest.writeString(this.packageName);
        dest.writeInt(this.andCode ? 1 : 0);
    }

    private PackageCleanItem(Parcel source) {
        boolean z = false;
        this.userId = source.readInt();
        this.packageName = source.readString();
        if (source.readInt() != 0) {
            z = true;
        }
        this.andCode = z;
    }
}
