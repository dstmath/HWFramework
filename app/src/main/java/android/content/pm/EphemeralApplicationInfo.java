package android.content.pm;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class EphemeralApplicationInfo implements Parcelable {
    public static final Creator<EphemeralApplicationInfo> CREATOR = null;
    private final ApplicationInfo mApplicationInfo;
    private final String[] mGrantedPermissions;
    private final CharSequence mLabelText;
    private final String mPackageName;
    private final String[] mRequestedPermissions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.EphemeralApplicationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.EphemeralApplicationInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.EphemeralApplicationInfo.<clinit>():void");
    }

    public EphemeralApplicationInfo(ApplicationInfo appInfo, String[] requestedPermissions, String[] grantedPermissions) {
        this.mApplicationInfo = appInfo;
        this.mPackageName = null;
        this.mLabelText = null;
        this.mRequestedPermissions = requestedPermissions;
        this.mGrantedPermissions = grantedPermissions;
    }

    public EphemeralApplicationInfo(String packageName, CharSequence label, String[] requestedPermissions, String[] grantedPermissions) {
        this.mApplicationInfo = null;
        this.mPackageName = packageName;
        this.mLabelText = label;
        this.mRequestedPermissions = requestedPermissions;
        this.mGrantedPermissions = grantedPermissions;
    }

    private EphemeralApplicationInfo(Parcel parcel) {
        this.mPackageName = parcel.readString();
        this.mLabelText = parcel.readCharSequence();
        this.mRequestedPermissions = parcel.readStringArray();
        this.mGrantedPermissions = parcel.createStringArray();
        this.mApplicationInfo = (ApplicationInfo) parcel.readParcelable(null);
    }

    public String getPackageName() {
        if (this.mApplicationInfo != null) {
            return this.mApplicationInfo.packageName;
        }
        return this.mPackageName;
    }

    public CharSequence loadLabel(PackageManager packageManager) {
        if (this.mApplicationInfo != null) {
            return this.mApplicationInfo.loadLabel(packageManager);
        }
        return this.mLabelText;
    }

    public Drawable loadIcon(PackageManager packageManager) {
        if (this.mApplicationInfo != null) {
            return this.mApplicationInfo.loadIcon(packageManager);
        }
        return packageManager.getEphemeralApplicationIcon(this.mPackageName);
    }

    public String[] getRequestedPermissions() {
        return this.mRequestedPermissions;
    }

    public String[] getGrantedPermissions() {
        return this.mGrantedPermissions;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mPackageName);
        parcel.writeCharSequence(this.mLabelText);
        parcel.writeStringArray(this.mRequestedPermissions);
        parcel.writeStringArray(this.mGrantedPermissions);
        parcel.writeParcelable(this.mApplicationInfo, flags);
    }
}
