package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.hwpartsecurity.BuildConfig;

public class PurgeGroupInfo implements Parcelable {
    public static final Parcelable.Creator<PurgeGroupInfo> CREATOR = new Parcelable.Creator<PurgeGroupInfo>() {
        /* class com.huawei.coauthservice.identitymgr.model.PurgeGroupInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PurgeGroupInfo createFromParcel(Parcel in) {
            return new PurgeGroupInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public PurgeGroupInfo[] newArray(int size) {
            return new PurgeGroupInfo[size];
        }
    };
    private String moduleName;
    private UserType userType;

    public PurgeGroupInfo() {
    }

    protected PurgeGroupInfo(Parcel in) {
        if (in != null) {
            this.moduleName = getStringOrDefault(in.readString());
            this.userType = getUserTypeOrDefault((UserType) in.readParcelable(UserType.class.getClassLoader()));
        }
    }

    public void setModuleName(String moduleName2) {
        this.moduleName = moduleName2;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void setUserType(UserType userType2) {
        this.userType = userType2;
    }

    public UserType getUserType() {
        return this.userType;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.moduleName);
        dest.writeParcelable(this.userType, flags);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return "PurgeGroupInfo{moduleName='" + this.moduleName + "', idmUserType=" + this.userType + '}';
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }

    private UserType getUserTypeOrDefault(UserType readUserType) {
        if (readUserType != null) {
            return readUserType;
        }
        return UserType.SAME_USER_ID;
    }
}
