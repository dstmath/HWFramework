package ohos.security.permission;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PermissionDef implements Sequenceable {
    public static final int FLAG_DISCARDED = 1;
    public static final int PRIVILEGED = 8;
    public static final int RESTRICTED = 2;
    public static final int SIGNATURE = 1;
    public static final int SYSTEM = 4;
    public static final int SYSTEM_GRANT = 0;
    public static final int USER_GRANT = 1;
    public int availableScope;
    public int descriptionRes;
    public int grantMode;
    public String group;
    public int labelRes;
    public String name;
    public int permissionFlags;
    public String reminderDesc;
    public int reminderIcon;
    public int usageInfo;

    public PermissionDef() {
    }

    public PermissionDef(PermissionDef permissionDef) {
        this.name = permissionDef.name;
        this.permissionFlags = permissionDef.permissionFlags;
        this.grantMode = permissionDef.grantMode;
        this.availableScope = permissionDef.availableScope;
        this.labelRes = permissionDef.labelRes;
        this.descriptionRes = permissionDef.descriptionRes;
        this.group = permissionDef.group;
        this.usageInfo = permissionDef.usageInfo;
        this.reminderDesc = permissionDef.reminderDesc;
        this.reminderIcon = permissionDef.reminderIcon;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.name);
        parcel.writeInt(this.permissionFlags);
        parcel.writeInt(this.grantMode);
        parcel.writeInt(this.availableScope);
        parcel.writeInt(this.labelRes);
        parcel.writeInt(this.descriptionRes);
        parcel.writeString(this.group);
        parcel.writeInt(this.usageInfo);
        parcel.writeString(this.reminderDesc);
        parcel.writeInt(this.reminderIcon);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.name = parcel.readString();
        this.permissionFlags = parcel.readInt();
        this.grantMode = parcel.readInt();
        this.availableScope = parcel.readInt();
        this.labelRes = parcel.readInt();
        this.descriptionRes = parcel.readInt();
        this.group = parcel.readString();
        this.usageInfo = parcel.readInt();
        this.reminderDesc = parcel.readString();
        this.reminderIcon = parcel.readInt();
        return true;
    }
}
