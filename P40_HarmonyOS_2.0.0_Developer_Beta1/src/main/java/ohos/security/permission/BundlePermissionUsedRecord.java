package ohos.security.permission;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class BundlePermissionUsedRecord implements Sequenceable {
    private static final int DEFAULT_CAPACITY = 10;
    private String bundleLabel;
    private String bundleName;
    private String deviceId;
    private String deviceLabel;
    private List<PermissionUsedRecord> permissionUsedRecords = new ArrayList(10);

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.deviceLabel);
        parcel.writeString(this.bundleName);
        parcel.writeString(this.bundleLabel);
        parcel.writeInt(this.permissionUsedRecords.size());
        for (PermissionUsedRecord permissionUsedRecord : this.permissionUsedRecords) {
            parcel.writeSequenceable(permissionUsedRecord);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.deviceId = parcel.readString();
        this.deviceLabel = parcel.readString();
        this.bundleName = parcel.readString();
        this.bundleLabel = parcel.readString();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            PermissionUsedRecord permissionUsedRecord = new PermissionUsedRecord();
            if (parcel.readSequenceable(permissionUsedRecord)) {
                this.permissionUsedRecords.add(permissionUsedRecord);
            }
        }
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

    public List<PermissionUsedRecord> getPermissionUsedRecords() {
        return this.permissionUsedRecords;
    }

    public void setPermissionUsedRecords(List<PermissionUsedRecord> list) {
        this.permissionUsedRecords = list;
    }

    public String toString() {
        return "BundlePermissionUsedRecord{deviceLabel='" + this.deviceLabel + "', bundleName='" + this.bundleName + "', bundleLabel='" + this.bundleLabel + "', permissionUsedRecords=" + this.permissionUsedRecords + '}';
    }
}
