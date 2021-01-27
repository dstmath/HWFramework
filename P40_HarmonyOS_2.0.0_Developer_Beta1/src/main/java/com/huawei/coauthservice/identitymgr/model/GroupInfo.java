package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;
import com.huawei.hwpartsecurity.BuildConfig;

public class GroupInfo implements Parcelable {
    public static final Parcelable.Creator<GroupInfo> CREATOR = new Parcelable.Creator<GroupInfo>() {
        /* class com.huawei.coauthservice.identitymgr.model.GroupInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GroupInfo createFromParcel(Parcel in) {
            return new GroupInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public GroupInfo[] newArray(int size) {
            return new GroupInfo[size];
        }
    };
    private String adminId;
    private String adminName;
    private String groupId;
    private String groupName;

    public GroupInfo() {
    }

    protected GroupInfo(Parcel in) {
        if (in != null) {
            this.groupId = getStringOrDefault(in.readString());
            this.groupName = getStringOrDefault(in.readString());
            this.adminId = getStringOrDefault(in.readString());
            this.adminName = getStringOrDefault(in.readString());
        }
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId2) {
        this.groupId = groupId2;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName2) {
        this.groupName = groupName2;
    }

    public String getAdminId() {
        return this.adminId;
    }

    public void setAdminId(String adminId2) {
        this.adminId = adminId2;
    }

    public String getAdminName() {
        return this.adminName;
    }

    public void setAdminName(String adminName2) {
        this.adminName = adminName2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupId);
        dest.writeString(this.groupName);
        dest.writeString(this.adminId);
        dest.writeString(this.adminName);
    }

    @Override // java.lang.Object
    public String toString() {
        return "GroupInfo{groupId='" + HwDeviceUtils.maskString(this.groupId) + "', adminId='" + HwDeviceUtils.maskString(this.adminId) + "'}";
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
