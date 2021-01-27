package ohos.security.permission;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PermissionReminderInfo implements Sequenceable {
    private String bundleLabel;
    private String bundleName;
    private String deviceId;
    private String deviceLabel;
    private String permName;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.deviceLabel);
        parcel.writeString(this.bundleName);
        parcel.writeString(this.bundleLabel);
        parcel.writeString(this.permName);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.deviceId = parcel.readString();
        this.deviceLabel = parcel.readString();
        this.bundleName = parcel.readString();
        this.bundleLabel = parcel.readString();
        this.permName = parcel.readString();
        return true;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public String getDeviceLabel() {
        return this.deviceLabel;
    }

    public void setDeviceLabel(String str) {
        this.deviceLabel = str;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String str) {
        this.bundleName = str;
    }

    public String getBundleLabel() {
        return this.bundleLabel;
    }

    public void setBundleLabel(String str) {
        this.bundleLabel = str;
    }

    public String getPermName() {
        return this.permName;
    }

    public void setPermName(String str) {
        this.permName = str;
    }

    public String toString() {
        return "PermissionReminderInfo{deviceId='" + this.deviceId + "', deviceLabel='" + this.deviceLabel + "', bundleName='" + this.bundleName + "', bundleLabel='" + this.bundleLabel + "', permName='" + this.permName + "'}";
    }
}
