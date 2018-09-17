package android.content.pm;

import android.graphics.drawable.Drawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class PermissionGroupInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<PermissionGroupInfo> CREATOR = new Creator<PermissionGroupInfo>() {
        public PermissionGroupInfo createFromParcel(Parcel source) {
            return new PermissionGroupInfo(source, null);
        }

        public PermissionGroupInfo[] newArray(int size) {
            return new PermissionGroupInfo[size];
        }
    };
    public static final int FLAG_PERSONAL_INFO = 1;
    public int descriptionRes;
    public int flags;
    public CharSequence nonLocalizedDescription;
    public int priority;

    /* synthetic */ PermissionGroupInfo(Parcel source, PermissionGroupInfo -this1) {
        this(source);
    }

    public PermissionGroupInfo(PermissionGroupInfo orig) {
        super((PackageItemInfo) orig);
        this.descriptionRes = orig.descriptionRes;
        this.nonLocalizedDescription = orig.nonLocalizedDescription;
        this.flags = orig.flags;
        this.priority = orig.priority;
    }

    public CharSequence loadDescription(PackageManager pm) {
        if (this.nonLocalizedDescription != null) {
            return this.nonLocalizedDescription;
        }
        if (this.descriptionRes != 0) {
            CharSequence label = pm.getText(this.packageName, this.descriptionRes, null);
            if (label != null) {
                return label;
            }
        }
        return null;
    }

    public String toString() {
        return "PermissionGroupInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.name + " flgs=0x" + Integer.toHexString(this.flags) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeInt(this.descriptionRes);
        TextUtils.writeToParcel(this.nonLocalizedDescription, dest, parcelableFlags);
        dest.writeInt(this.flags);
        dest.writeInt(this.priority);
    }

    private PermissionGroupInfo(Parcel source) {
        super(source);
        this.descriptionRes = source.readInt();
        this.nonLocalizedDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.flags = source.readInt();
        this.priority = source.readInt();
    }

    public Drawable loadIcon(PackageManager pm) {
        return pm.loadItemIcon(this, getApplicationInfo());
    }
}
