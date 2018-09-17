package android.accounts;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AuthenticatorDescription implements Parcelable {
    public static final Creator<AuthenticatorDescription> CREATOR = new Creator<AuthenticatorDescription>() {
        public AuthenticatorDescription createFromParcel(Parcel source) {
            return new AuthenticatorDescription(source, null);
        }

        public AuthenticatorDescription[] newArray(int size) {
            return new AuthenticatorDescription[size];
        }
    };
    public final int accountPreferencesId;
    public final boolean customTokens;
    public final int iconId;
    public final int labelId;
    public final String packageName;
    public final int smallIconId;
    public final String type;

    /* synthetic */ AuthenticatorDescription(Parcel source, AuthenticatorDescription -this1) {
        this(source);
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
        dest.writeString(this.type);
        dest.writeString(this.packageName);
        dest.writeInt(this.labelId);
        dest.writeInt(this.iconId);
        dest.writeInt(this.smallIconId);
        dest.writeInt(this.accountPreferencesId);
        dest.writeByte((byte) (this.customTokens ? 1 : 0));
    }
}
