package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class DeleteGroupInfo implements Parcelable {
    public static final Parcelable.Creator<DeleteGroupInfo> CREATOR = new Parcelable.Creator<DeleteGroupInfo>() {
        /* class com.huawei.coauthservice.identitymgr.model.DeleteGroupInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeleteGroupInfo createFromParcel(Parcel in) {
            return new DeleteGroupInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeleteGroupInfo[] newArray(int size) {
            return new DeleteGroupInfo[size];
        }
    };
    private static final int MAX_DEVICE_LEN = 10;
    private String groupId;
    private List<DeviceInfo> peerDeviceInfoList;
    private UserType userType;

    public DeleteGroupInfo() {
    }

    protected DeleteGroupInfo(Parcel in) {
        if (in != null) {
            this.groupId = getStringOrDefault(in.readString());
            this.userType = getUserTypeOrDefault((UserType) in.readParcelable(UserType.class.getClassLoader()));
            this.peerDeviceInfoList = getDeviceInfoListOrDefault(in.readArrayList(DeviceInfo.class.getClassLoader()));
        }
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId2) {
        this.groupId = groupId2;
    }

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType2) {
        this.userType = userType2;
    }

    public List<DeviceInfo> getPeerDeviceInfoList() {
        return this.peerDeviceInfoList;
    }

    public void setPeerDeviceInfoList(List<DeviceInfo> peerDeviceInfoList2) {
        this.peerDeviceInfoList = peerDeviceInfoList2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupId);
        dest.writeParcelable(this.userType, flags);
        dest.writeList(this.peerDeviceInfoList);
    }

    @Override // java.lang.Object
    public String toString() {
        return "DeleteGroupInfo{groupId='" + HwDeviceUtils.maskString(this.groupId) + "', userType=" + this.userType + ", peerDeviceInfoList=" + this.peerDeviceInfoList + '}';
    }

    private UserType getUserTypeOrDefault(UserType readUserType) {
        if (readUserType != null) {
            return readUserType;
        }
        return UserType.SAME_USER_ID;
    }

    private List<DeviceInfo> getDeviceInfoListOrDefault(List<DeviceInfo> readPeerDeviceInfoList) {
        if (readPeerDeviceInfoList == null || !checkDeviceInfoListLen(readPeerDeviceInfoList.size())) {
            return new ArrayList(0);
        }
        return readPeerDeviceInfoList;
    }

    private boolean checkDeviceInfoListLen(int deviceInfoListLen) {
        if (deviceInfoListLen <= 0 || deviceInfoListLen > 10) {
            return false;
        }
        return true;
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
