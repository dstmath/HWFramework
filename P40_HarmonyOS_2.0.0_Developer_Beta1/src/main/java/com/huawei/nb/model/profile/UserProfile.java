package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfile extends AManagedObject {
    public static final Parcelable.Creator<UserProfile> CREATOR = new Parcelable.Creator<UserProfile>() {
        /* class com.huawei.nb.model.profile.UserProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserProfile createFromParcel(Parcel parcel) {
            return new UserProfile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public UserProfile[] newArray(int i) {
            return new UserProfile[i];
        }
    };
    private String deviceID;
    private String deviceToken;
    private String hwId;
    private Integer id;
    private String regDate;
    private String userProfile;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.profile.UserProfile";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public UserProfile(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.regDate = cursor.getString(2);
        this.deviceToken = cursor.getString(3);
        this.deviceID = cursor.getString(4);
        this.hwId = cursor.getString(5);
        this.userProfile = cursor.getString(6);
    }

    public UserProfile(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.regDate = parcel.readByte() == 0 ? null : parcel.readString();
        this.deviceToken = parcel.readByte() == 0 ? null : parcel.readString();
        this.deviceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.hwId = parcel.readByte() == 0 ? null : parcel.readString();
        this.userProfile = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private UserProfile(Integer num, String str, String str2, String str3, String str4, String str5) {
        this.id = num;
        this.regDate = str;
        this.deviceToken = str2;
        this.deviceID = str3;
        this.hwId = str4;
        this.userProfile = str5;
    }

    public UserProfile() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getRegDate() {
        return this.regDate;
    }

    public void setRegDate(String str) {
        this.regDate = str;
        setValue();
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(String str) {
        this.deviceToken = str;
        setValue();
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(String str) {
        this.deviceID = str;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String str) {
        this.hwId = str;
        setValue();
    }

    public String getUserProfile() {
        return this.userProfile;
    }

    public void setUserProfile(String str) {
        this.userProfile = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.regDate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.regDate);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.deviceToken != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.deviceToken);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.deviceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.deviceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.hwId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.userProfile != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.userProfile);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<UserProfile> getHelper() {
        return UserProfileHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "UserProfile { id: " + this.id + ", regDate: " + this.regDate + ", deviceToken: " + this.deviceToken + ", deviceID: " + this.deviceID + ", hwId: " + this.hwId + ", userProfile: " + this.userProfile + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
