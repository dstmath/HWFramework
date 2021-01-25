package android.accounts;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class AuthenticatorDescription implements Parcelable {
    public static final Parcelable.Creator<AuthenticatorDescription> CREATOR = new Parcelable.Creator<AuthenticatorDescription>() {
        /* class android.accounts.AuthenticatorDescription.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AuthenticatorDescription createFromParcel(Parcel source) {
            return new AuthenticatorDescription(source);
        }

        @Override // android.os.Parcelable.Creator
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

    public AuthenticatorDescription(String type2, String packageName2, int labelId2, int iconId2, int smallIconId2, int prefId, boolean customTokens2) {
        if (type2 == null) {
            throw new IllegalArgumentException("type cannot be null");
        } else if (packageName2 != null) {
            this.type = type2;
            this.packageName = packageName2;
            this.labelId = labelId2;
            this.iconId = iconId2;
            this.smallIconId = smallIconId2;
            this.accountPreferencesId = prefId;
            this.customTokens = customTokens2;
        } else {
            throw new IllegalArgumentException("packageName cannot be null");
        }
    }

    public AuthenticatorDescription(String type2, String packageName2, int labelId2, int iconId2, int smallIconId2, int prefId) {
        this(type2, packageName2, labelId2, iconId2, smallIconId2, prefId, false);
    }

    public static AuthenticatorDescription newKey(String type2) {
        if (type2 != null) {
            return new AuthenticatorDescription(type2);
        }
        throw new IllegalArgumentException("type cannot be null");
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private AuthenticatorDescription(String type2) {
        this.type = type2;
        this.packageName = null;
        this.labelId = 0;
        this.iconId = 0;
        this.smallIconId = 0;
        this.accountPreferencesId = 0;
        this.customTokens = false;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private AuthenticatorDescription(Parcel source) {
        this.type = source.readString();
        this.packageName = source.readString();
        this.labelId = source.readInt();
        this.iconId = source.readInt();
        this.smallIconId = source.readInt();
        this.accountPreferencesId = source.readInt();
        this.customTokens = source.readByte() != 1 ? false : true;
    }

    @Override // android.os.Parcelable
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.packageName);
        dest.writeInt(this.labelId);
        dest.writeInt(this.iconId);
        dest.writeInt(this.smallIconId);
        dest.writeInt(this.accountPreferencesId);
        dest.writeByte(this.customTokens ? (byte) 1 : 0);
    }
}
