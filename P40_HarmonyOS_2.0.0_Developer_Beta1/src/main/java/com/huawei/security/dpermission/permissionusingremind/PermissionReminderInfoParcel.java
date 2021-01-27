package com.huawei.security.dpermission.permissionusingremind;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class PermissionReminderInfoParcel implements Parcelable {
    public static final Parcelable.Creator<PermissionReminderInfoParcel> CREATOR = new Parcelable.Creator<PermissionReminderInfoParcel>() {
        /* class com.huawei.security.dpermission.permissionusingremind.PermissionReminderInfoParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PermissionReminderInfoParcel createFromParcel(Parcel in) {
            return new PermissionReminderInfoParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public PermissionReminderInfoParcel[] newArray(int size) {
            return new PermissionReminderInfoParcel[size];
        }
    };
    private String bundleLabel;
    private String bundleName;
    private String deviceId;
    private String deviceLabel;
    private String permName;

    public PermissionReminderInfoParcel() {
    }

    public PermissionReminderInfoParcel(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.deviceId = in.readString();
        this.deviceLabel = in.readString();
        this.bundleName = in.readString();
        this.bundleLabel = in.readString();
        this.permName = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceLabel);
        dest.writeString(this.bundleName);
        dest.writeString(this.bundleLabel);
        dest.writeString(this.permName);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getDeviceLabel() {
        return this.deviceLabel;
    }

    public void setDeviceLabel(String deviceLabel2) {
        this.deviceLabel = deviceLabel2;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String bundleName2) {
        this.bundleName = bundleName2;
    }

    public String getBundleLabel() {
        return this.bundleLabel;
    }

    public void setBundleLabel(String bundleLabel2) {
        this.bundleLabel = bundleLabel2;
    }

    public String getPermName() {
        return this.permName;
    }

    public void setPermName(String permName2) {
        this.permName = permName2;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PermissionReminderInfoParcel that = (PermissionReminderInfoParcel) obj;
        if (!Objects.equals(this.deviceId, that.deviceId) || !Objects.equals(this.bundleName, that.bundleName) || !Objects.equals(this.permName, that.permName)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.deviceId, this.bundleName, this.permName);
    }

    @Override // java.lang.Object
    public String toString() {
        return "PermissionReminderInfoParcel{deviceLabel='" + this.deviceLabel + "', bundleName='" + this.bundleName + "', bundleLabel='" + this.bundleLabel + "', permName='" + this.permName + "'}";
    }
}
