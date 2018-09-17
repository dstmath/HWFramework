package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwPFWStartupSetting implements Parcelable, Cloneable {
    public static final Creator<HwPFWStartupSetting> CREATOR = null;
    public static final int STARTUP_STATUS_ALLOW = 1;
    public static final int STARTUP_STATUS_FORBID = 0;
    public static final int STARTUP_STATUS_UNKNOWN = 2;
    public static final int STARTUP_TYPE_NUM = 2;
    public static final int STARTUP_TYPE_RECEIVER = 0;
    public static final int STARTUP_TYPE_SERVICE_PROVIDER = 1;
    private int mAllow;
    private String mPackageName;
    private int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.pfw.HwPFWStartupSetting.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.pfw.HwPFWStartupSetting.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.pfw.HwPFWStartupSetting.<clinit>():void");
    }

    public HwPFWStartupSetting(String packageName, int type, int allow) {
        this.mPackageName = packageName;
        this.mType = type;
        this.mAllow = allow;
    }

    public HwPFWStartupSetting(String packageName, int type) {
        this.mPackageName = packageName;
        this.mType = type;
        this.mAllow = STARTUP_TYPE_NUM;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getTypeValue() {
        return this.mType;
    }

    public int getAllowValue() {
        return this.mAllow;
    }

    public boolean valid() {
        if (this.mType == 0 || STARTUP_TYPE_SERVICE_PROVIDER == this.mType) {
            return this.mAllow == 0 || STARTUP_TYPE_SERVICE_PROVIDER == this.mAllow || STARTUP_TYPE_NUM == this.mAllow;
        } else {
            return false;
        }
    }

    public int describeContents() {
        return STARTUP_TYPE_RECEIVER;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAllow);
    }

    private HwPFWStartupSetting(Parcel source) {
        this.mPackageName = source.readString();
        this.mType = source.readInt();
        this.mAllow = source.readInt();
    }

    public String toString() {
        return "HwPFWStartupSetting {" + this.mPackageName + ", type: " + this.mType + ", allow: " + this.mAllow + "}";
    }

    protected Object clone() throws CloneNotSupportedException {
        HwPFWStartupSetting o = null;
        try {
            return (HwPFWStartupSetting) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            return o;
        }
    }
}
