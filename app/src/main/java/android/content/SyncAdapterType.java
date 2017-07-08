package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class SyncAdapterType implements Parcelable {
    public static final Creator<SyncAdapterType> CREATOR = null;
    public final String accountType;
    private final boolean allowParallelSyncs;
    public final String authority;
    private final boolean isAlwaysSyncable;
    public final boolean isKey;
    private final String settingsActivity;
    private final boolean supportsUploading;
    private final boolean userVisible;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.SyncAdapterType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.SyncAdapterType.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.SyncAdapterType.<clinit>():void");
    }

    public SyncAdapterType(String authority, String accountType, boolean userVisible, boolean supportsUploading) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = userVisible;
            this.supportsUploading = supportsUploading;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = false;
        }
    }

    public SyncAdapterType(String authority, String accountType, boolean userVisible, boolean supportsUploading, boolean isAlwaysSyncable, boolean allowParallelSyncs, String settingsActivity) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = userVisible;
            this.supportsUploading = supportsUploading;
            this.isAlwaysSyncable = isAlwaysSyncable;
            this.allowParallelSyncs = allowParallelSyncs;
            this.settingsActivity = settingsActivity;
            this.isKey = false;
        }
    }

    private SyncAdapterType(String authority, String accountType) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = true;
            this.supportsUploading = true;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = true;
        }
    }

    public boolean supportsUploading() {
        if (!this.isKey) {
            return this.supportsUploading;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isUserVisible() {
        if (!this.isKey) {
            return this.userVisible;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean allowParallelSyncs() {
        if (!this.isKey) {
            return this.allowParallelSyncs;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isAlwaysSyncable() {
        if (!this.isKey) {
            return this.isAlwaysSyncable;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public String getSettingsActivity() {
        if (!this.isKey) {
            return this.settingsActivity;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public static SyncAdapterType newKey(String authority, String accountType) {
        return new SyncAdapterType(authority, accountType);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof SyncAdapterType)) {
            return false;
        }
        SyncAdapterType other = (SyncAdapterType) o;
        if (this.authority.equals(other.authority)) {
            z = this.accountType.equals(other.accountType);
        }
        return z;
    }

    public int hashCode() {
        return ((this.authority.hashCode() + 527) * 31) + this.accountType.hashCode();
    }

    public String toString() {
        if (this.isKey) {
            return "SyncAdapterType Key {name=" + this.authority + ", type=" + this.accountType + "}";
        }
        return "SyncAdapterType {name=" + this.authority + ", type=" + this.accountType + ", userVisible=" + this.userVisible + ", supportsUploading=" + this.supportsUploading + ", isAlwaysSyncable=" + this.isAlwaysSyncable + ", allowParallelSyncs=" + this.allowParallelSyncs + ", settingsActivity=" + this.settingsActivity + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (this.isKey) {
            throw new IllegalStateException("keys aren't parcelable");
        }
        int i2;
        dest.writeString(this.authority);
        dest.writeString(this.accountType);
        if (this.userVisible) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (this.supportsUploading) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (this.isAlwaysSyncable) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!this.allowParallelSyncs) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.settingsActivity);
    }

    public SyncAdapterType(Parcel source) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4 = false;
        String readString = source.readString();
        String readString2 = source.readString();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        if (source.readInt() != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        if (source.readInt() != 0) {
            z3 = true;
        } else {
            z3 = false;
        }
        if (source.readInt() != 0) {
            z4 = true;
        }
        this(readString, readString2, z, z2, z3, z4, source.readString());
    }
}
