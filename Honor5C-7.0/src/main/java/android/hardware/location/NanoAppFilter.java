package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public class NanoAppFilter {
    public static final int APP_ANY = -1;
    public static final Creator<NanoAppFilter> CREATOR = null;
    public static final int FLAGS_VERSION_ANY = -1;
    public static final int FLAGS_VERSION_GREAT_THAN = 2;
    public static final int FLAGS_VERSION_LESS_THAN = 4;
    public static final int FLAGS_VERSION_STRICTLY_EQUAL = 8;
    public static final int HUB_ANY = -1;
    private static final String TAG = "NanoAppFilter";
    public static final int VENDOR_ANY = -1;
    private long mAppId;
    private long mAppIdVendorMask;
    private int mAppVersion;
    private int mContextHubId;
    private int mVersionRestrictionMask;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.NanoAppFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.NanoAppFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.NanoAppFilter.<clinit>():void");
    }

    private NanoAppFilter(Parcel in) {
        this.mAppId = in.readLong();
        this.mAppVersion = in.readInt();
        this.mVersionRestrictionMask = in.readInt();
        this.mAppIdVendorMask = (long) in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mAppId);
        out.writeInt(this.mAppVersion);
        out.writeInt(this.mVersionRestrictionMask);
        out.writeLong(this.mAppIdVendorMask);
    }

    public NanoAppFilter(long appId, int appVersion, int versionMask, long vendorMask) {
        this.mAppId = appId;
        this.mAppVersion = appVersion;
        this.mVersionRestrictionMask = versionMask;
        this.mAppIdVendorMask = vendorMask;
    }

    private boolean versionsMatch(int versionRestrictionMask, int expected, int actual) {
        return true;
    }

    public boolean testMatch(NanoAppInstanceInfo info) {
        if ((this.mContextHubId == VENDOR_ANY || info.getContexthubId() == this.mContextHubId) && (this.mAppId == -1 || info.getAppId() == this.mAppId)) {
            return versionsMatch(this.mVersionRestrictionMask, this.mAppVersion, info.getAppVersion());
        }
        return false;
    }
}
