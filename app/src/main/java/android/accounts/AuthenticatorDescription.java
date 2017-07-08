package android.accounts;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AuthenticatorDescription implements Parcelable {
    public static final Creator<AuthenticatorDescription> CREATOR = null;
    public final int accountPreferencesId;
    public final boolean customTokens;
    public final int iconId;
    public final int labelId;
    public final String packageName;
    public final int smallIconId;
    public final String type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.accounts.AuthenticatorDescription.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.accounts.AuthenticatorDescription.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AuthenticatorDescription.<clinit>():void");
    }

    public AuthenticatorDescription(String type, String packageName, int labelId, int iconId, int smallIconId, int prefId, boolean customTokens) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        } else if (packageName == null) {
            throw new IllegalArgumentException("packageName cannot be null");
        } else {
            this.type = type;
            this.packageName = packageName;
            this.labelId = labelId;
            this.iconId = iconId;
            this.smallIconId = smallIconId;
            this.accountPreferencesId = prefId;
            this.customTokens = customTokens;
        }
    }

    public AuthenticatorDescription(String type, String packageName, int labelId, int iconId, int smallIconId, int prefId) {
        this(type, packageName, labelId, iconId, smallIconId, prefId, false);
    }

    public static AuthenticatorDescription newKey(String type) {
        if (type != null) {
            return new AuthenticatorDescription(type);
        }
        throw new IllegalArgumentException("type cannot be null");
    }

    private AuthenticatorDescription(String type) {
        this.type = type;
        this.packageName = null;
        this.labelId = 0;
        this.iconId = 0;
        this.smallIconId = 0;
        this.accountPreferencesId = 0;
        this.customTokens = false;
    }

    private AuthenticatorDescription(Parcel source) {
        boolean z = true;
        this.type = source.readString();
        this.packageName = source.readString();
        this.labelId = source.readInt();
        this.iconId = source.readInt();
        this.smallIconId = source.readInt();
        this.accountPreferencesId = source.readInt();
        if (source.readByte() != (byte) 1) {
            z = false;
        }
        this.customTokens = z;
    }

    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AuthenticatorDescription)) {
            return false;
        }
        return this.type.equals(((AuthenticatorDescription) o).type);
    }

    public String toString() {
        return "AuthenticatorDescription {type=" + this.type + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeString(this.type);
        dest.writeString(this.packageName);
        dest.writeInt(this.labelId);
        dest.writeInt(this.iconId);
        dest.writeInt(this.smallIconId);
        dest.writeInt(this.accountPreferencesId);
        if (this.customTokens) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
    }
}
