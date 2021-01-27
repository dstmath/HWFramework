package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupInfo implements Parcelable {
    public static final Parcelable.Creator<CreateGroupInfo> CREATOR = new Parcelable.Creator<CreateGroupInfo>() {
        /* class com.huawei.coauthservice.identitymgr.model.CreateGroupInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CreateGroupInfo createFromParcel(Parcel in) {
            return new CreateGroupInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public CreateGroupInfo[] newArray(int size) {
            return new CreateGroupInfo[size];
        }
    };
    private static final int MAX_DEVICE_LEN = 10;
    private String moduleName;
    private boolean overwrite = true;
    private List<DeviceInfo> peerDeviceInfoList;
    private UserType userType;

    public CreateGroupInfo() {
    }

    protected CreateGroupInfo(Parcel in) {
        if (in != null) {
            this.peerDeviceInfoList = getDeviceInfoListOrDefault(in.readArrayList(DeviceInfo.class.getClassLoader()));
            this.moduleName = getStringOrDefault(in.readString());
            this.overwrite = in.readBoolean();
            this.userType = getUserTypeOrDefault((UserType) in.readParcelable(UserType.class.getClassLoader()));
        }
    }

    public List<DeviceInfo> getPeerDeviceInfoList() {
        return this.peerDeviceInfoList;
    }

    public void setPeerDeviceInfoList(List<DeviceInfo> peerDeviceInfoList2) {
        this.peerDeviceInfoList = peerDeviceInfoList2;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void setModuleName(String moduleName2) {
        this.moduleName = moduleName2;
    }

    public boolean isOverwrite() {
        return this.overwrite;
    }

    public void setOverwrite(boolean overwrite2) {
        this.overwrite = overwrite2;
    }

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType2) {
        this.userType = userType2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.peerDeviceInfoList);
        dest.writeString(this.moduleName);
        dest.writeBoolean(this.overwrite);
        dest.writeParcelable(this.userType, flags);
    }

    @Override // java.lang.Object
    public String toString() {
        return "CreateGroupInfo{peerDeviceInfoList=" + this.peerDeviceInfoList + ", moduleName='" + this.moduleName + "', overwrite=" + this.overwrite + ", userType=" + this.userType + '}';
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
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

    private UserType getUserTypeOrDefault(UserType readUserType) {
        if (readUserType != null) {
            return readUserType;
        }
        return UserType.SAME_USER_ID;
    }
}
