package android.os;

import android.os.Parcelable.Creator;

public class JankAppInfo implements Parcelable {
    public static final Creator<JankAppInfo> CREATOR = null;
    public boolean coreApp;
    public int flags;
    public String packageName;
    public boolean systemApp;
    public int versionCode;
    public String versionName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankAppInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankAppInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankAppInfo.<clinit>():void");
    }

    private JankAppInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.versionName = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.coreApp = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.systemApp = z2;
        this.flags = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        int i;
        int i2 = 1;
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.versionName);
        if (this.coreApp) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.systemApp) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.flags);
    }
}
