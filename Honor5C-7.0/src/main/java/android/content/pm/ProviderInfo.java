package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PatternMatcher;
import android.util.Printer;

public final class ProviderInfo extends ComponentInfo implements Parcelable {
    public static final Creator<ProviderInfo> CREATOR = null;
    public static final int FLAG_SINGLE_USER = 1073741824;
    public String authority;
    public int flags;
    public boolean grantUriPermissions;
    public int initOrder;
    @Deprecated
    public boolean isSyncable;
    public boolean multiprocess;
    public PathPermission[] pathPermissions;
    public String readPermission;
    public PatternMatcher[] uriPermissionPatterns;
    public String writePermission;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ProviderInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.ProviderInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ProviderInfo.<clinit>():void");
    }

    public ProviderInfo() {
        this.authority = null;
        this.readPermission = null;
        this.writePermission = null;
        this.grantUriPermissions = false;
        this.uriPermissionPatterns = null;
        this.pathPermissions = null;
        this.multiprocess = false;
        this.initOrder = 0;
        this.flags = 0;
        this.isSyncable = false;
    }

    public ProviderInfo(ProviderInfo orig) {
        super((ComponentInfo) orig);
        this.authority = null;
        this.readPermission = null;
        this.writePermission = null;
        this.grantUriPermissions = false;
        this.uriPermissionPatterns = null;
        this.pathPermissions = null;
        this.multiprocess = false;
        this.initOrder = 0;
        this.flags = 0;
        this.isSyncable = false;
        this.authority = orig.authority;
        this.readPermission = orig.readPermission;
        this.writePermission = orig.writePermission;
        this.grantUriPermissions = orig.grantUriPermissions;
        this.uriPermissionPatterns = orig.uriPermissionPatterns;
        this.pathPermissions = orig.pathPermissions;
        this.multiprocess = orig.multiprocess;
        this.initOrder = orig.initOrder;
        this.flags = orig.flags;
        this.isSyncable = orig.isSyncable;
    }

    public void dump(Printer pw, String prefix) {
        dump(pw, prefix, 3);
    }

    public void dump(Printer pw, String prefix, int flags) {
        super.dumpFront(pw, prefix);
        pw.println(prefix + "authority=" + this.authority);
        pw.println(prefix + "flags=0x" + Integer.toHexString(flags));
        super.dumpBack(pw, prefix, flags);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int parcelableFlags) {
        int i;
        int i2 = 1;
        super.writeToParcel(out, parcelableFlags);
        out.writeString(this.authority);
        out.writeString(this.readPermission);
        out.writeString(this.writePermission);
        if (this.grantUriPermissions) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeTypedArray(this.uriPermissionPatterns, parcelableFlags);
        out.writeTypedArray(this.pathPermissions, parcelableFlags);
        if (this.multiprocess) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeInt(this.initOrder);
        out.writeInt(this.flags);
        if (!this.isSyncable) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public String toString() {
        return "ContentProviderInfo{name=" + this.authority + " className=" + this.name + "}";
    }

    private ProviderInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        super(in);
        this.authority = null;
        this.readPermission = null;
        this.writePermission = null;
        this.grantUriPermissions = false;
        this.uriPermissionPatterns = null;
        this.pathPermissions = null;
        this.multiprocess = false;
        this.initOrder = 0;
        this.flags = 0;
        this.isSyncable = false;
        this.authority = in.readString();
        this.readPermission = in.readString();
        this.writePermission = in.readString();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.grantUriPermissions = z;
        this.uriPermissionPatterns = (PatternMatcher[]) in.createTypedArray(PatternMatcher.CREATOR);
        this.pathPermissions = (PathPermission[]) in.createTypedArray(PathPermission.CREATOR);
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.multiprocess = z;
        this.initOrder = in.readInt();
        this.flags = in.readInt();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.isSyncable = z2;
    }
}
