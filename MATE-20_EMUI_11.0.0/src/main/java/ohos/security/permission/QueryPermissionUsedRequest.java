package ohos.security.permission;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class QueryPermissionUsedRequest implements Sequenceable {
    private static final int DEFAULT_CAPACITY = 10;
    private long beginTimeMillis;
    private String bundleName;
    private String deviceLabel;
    private long endTimeMillis;
    private int flag = FlagEnum.FLAG_PERMISSION_USAGE_SUMMARY.getFlag();
    private List<String> permissionNames = new ArrayList(10);

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.deviceLabel);
        parcel.writeString(this.bundleName);
        parcel.writeStringList(this.permissionNames);
        parcel.writeLong(this.beginTimeMillis);
        parcel.writeLong(this.endTimeMillis);
        parcel.writeInt(this.flag);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.deviceLabel = parcel.readString();
        this.bundleName = parcel.readString();
        this.permissionNames = parcel.readStringList();
        this.beginTimeMillis = parcel.readLong();
        this.endTimeMillis = parcel.readLong();
        this.flag = parcel.readInt();
        return true;
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

    public List<String> getPermissionNames() {
        return this.permissionNames;
    }

    public void setPermissionNames(List<String> list) {
        this.permissionNames = list;
    }

    public long getBeginTimeMillis() {
        return this.beginTimeMillis;
    }

    public void setBeginTimeMillis(long j) {
        this.beginTimeMillis = j;
    }

    public long getEndTimeMillis() {
        return this.endTimeMillis;
    }

    public void setEndTimeMillis(long j) {
        this.endTimeMillis = j;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int i) {
        this.flag = i;
    }

    public String toString() {
        return "QueryPermissionUsedRequest{deviceLabel='" + this.deviceLabel + "', bundleName='" + this.bundleName + "', permissionNames=" + this.permissionNames + ", beginTimeMillis=" + this.beginTimeMillis + ", endTimeMillis=" + this.endTimeMillis + ", flag=" + this.flag + '}';
    }

    public enum FlagEnum {
        FLAG_PERMISSION_USAGE_SUMMARY(0),
        FLAG_PERMISSION_USAGE_DETAIL(1);
        
        private int flag;

        private FlagEnum(int i) {
            this.flag = i;
        }

        public int getFlag() {
            return this.flag;
        }
    }
}
