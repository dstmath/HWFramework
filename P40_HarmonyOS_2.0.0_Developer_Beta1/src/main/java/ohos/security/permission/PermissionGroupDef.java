package ohos.security.permission;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PermissionGroupDef implements Sequenceable {
    public int descriptionRes;
    public int iconRes;
    public int labelRes;
    public String name;
    public int order;
    public int requestRes;

    public PermissionGroupDef() {
    }

    public PermissionGroupDef(PermissionGroupDef permissionGroupDef) {
        this.name = permissionGroupDef.name;
        this.iconRes = permissionGroupDef.iconRes;
        this.labelRes = permissionGroupDef.labelRes;
        this.descriptionRes = permissionGroupDef.descriptionRes;
        this.order = permissionGroupDef.order;
        this.requestRes = permissionGroupDef.requestRes;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.name);
        parcel.writeInt(this.iconRes);
        parcel.writeInt(this.labelRes);
        parcel.writeInt(this.descriptionRes);
        parcel.writeInt(this.order);
        parcel.writeInt(this.requestRes);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.name = parcel.readString();
        this.iconRes = parcel.readInt();
        this.labelRes = parcel.readInt();
        this.descriptionRes = parcel.readInt();
        this.order = parcel.readInt();
        this.requestRes = parcel.readInt();
        return true;
    }
}
