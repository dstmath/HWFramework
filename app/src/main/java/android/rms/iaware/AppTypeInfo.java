package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AppTypeInfo implements Parcelable {
    public static final int APP_TYPE_ALARM = 5;
    public static final int APP_TYPE_BANK = 16;
    public static final int APP_TYPE_BROWSER = 18;
    public static final int APP_TYPE_BUSINESS = 11;
    public static final int APP_TYPE_CAMERA = 17;
    public static final int APP_TYPE_EBOOK = 105;
    public static final int APP_TYPE_EMAIL = 1;
    public static final int APP_TYPE_GAME = 9;
    public static final int APP_TYPE_IM = 0;
    public static final int APP_TYPE_INPUTMETHOD = 103;
    public static final int APP_TYPE_LAUNCHER = 101;
    public static final int APP_TYPE_LEXICON = 10;
    public static final int APP_TYPE_MUSIC = 7;
    public static final int APP_TYPE_NAVI = 3;
    public static final int APP_TYPE_OFFICE = 12;
    public static final int APP_TYPE_OTHERS = 255;
    public static final int APP_TYPE_READING = 6;
    public static final int APP_TYPE_SCRLOCK = 104;
    public static final int APP_TYPE_SHOPPING = 14;
    public static final int APP_TYPE_SMS = 102;
    public static final int APP_TYPE_SPORT = 2;
    public static final int APP_TYPE_STOCKS = 4;
    public static final int APP_TYPE_THEME = 13;
    public static final int APP_TYPE_TOOL = 15;
    public static final int APP_TYPE_UNKNOWN = -1;
    public static final int APP_TYPE_VIDEO = 8;
    public static final Creator<AppTypeInfo> CREATOR = null;
    public static final int PG_APP_TYPE_ALARM = 310;
    public static final int PG_APP_TYPE_BROWSER = 306;
    public static final int PG_APP_TYPE_EBOOK = 307;
    public static final int PG_APP_TYPE_EMAIL = 303;
    public static final int PG_APP_TYPE_GAME = 305;
    public static final int PG_APP_TYPE_IM = 311;
    public static final int PG_APP_TYPE_INPUTMETHOD = 304;
    public static final int PG_APP_TYPE_LAUNCHER = 301;
    public static final int PG_APP_TYPE_MUSIC = 312;
    public static final int PG_APP_TYPE_SCRLOCK = 309;
    public static final int PG_APP_TYPE_SMS = 302;
    public static final int PG_APP_TYPE_VIDEO = 308;
    private int attribute;
    private String pkgName;
    private int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AppTypeInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AppTypeInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AppTypeInfo.<clinit>():void");
    }

    public AppTypeInfo(Parcel source) {
        this.type = source.readInt();
        this.attribute = source.readInt();
        this.pkgName = source.readString();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAttribute() {
        return this.attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String toString() {
        return "AppTypeInfo [type=" + this.type + ", attribute=" + this.attribute + "]";
    }

    public int describeContents() {
        return APP_TYPE_IM;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.attribute);
        dest.writeString(this.pkgName);
    }
}
