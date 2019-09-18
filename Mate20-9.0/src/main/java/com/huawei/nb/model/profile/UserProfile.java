package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfile extends AManagedObject {
    public static final Parcelable.Creator<UserProfile> CREATOR = new Parcelable.Creator<UserProfile>() {
        public UserProfile createFromParcel(Parcel in) {
            return new UserProfile(in);
        }

        public UserProfile[] newArray(int size) {
            return new UserProfile[size];
        }
    };
    private String deviceID;
    private String deviceToken;
    private String hwId;
    private Integer id;
    private String regDate;
    private String userProfile;

    public UserProfile(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.regDate = cursor.getString(2);
        this.deviceToken = cursor.getString(3);
        this.deviceID = cursor.getString(4);
        this.hwId = cursor.getString(5);
        this.userProfile = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UserProfile(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.regDate = in.readByte() == 0 ? null : in.readString();
        this.deviceToken = in.readByte() == 0 ? null : in.readString();
        this.deviceID = in.readByte() == 0 ? null : in.readString();
        this.hwId = in.readByte() == 0 ? null : in.readString();
        this.userProfile = in.readByte() != 0 ? in.readString() : str;
    }

    private UserProfile(Integer id2, String regDate2, String deviceToken2, String deviceID2, String hwId2, String userProfile2) {
        this.id = id2;
        this.regDate = regDate2;
        this.deviceToken = deviceToken2;
        this.deviceID = deviceID2;
        this.hwId = hwId2;
        this.userProfile = userProfile2;
    }

    public UserProfile() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getRegDate() {
        return this.regDate;
    }

    public void setRegDate(String regDate2) {
        this.regDate = regDate2;
        setValue();
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(String deviceToken2) {
        this.deviceToken = deviceToken2;
        setValue();
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(String deviceID2) {
        this.deviceID = deviceID2;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String hwId2) {
        this.hwId = hwId2;
        setValue();
    }

    public String getUserProfile() {
        return this.userProfile;
    }

    public void setUserProfile(String userProfile2) {
        this.userProfile = userProfile2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.regDate != null) {
            out.writeByte((byte) 1);
            out.writeString(this.regDate);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deviceToken != null) {
            out.writeByte((byte) 1);
            out.writeString(this.deviceToken);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deviceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.deviceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.hwId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.userProfile != null) {
            out.writeByte((byte) 1);
            out.writeString(this.userProfile);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<UserProfile> getHelper() {
        return UserProfileHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.profile.UserProfile";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UserProfile { id: ").append(this.id);
        sb.append(", regDate: ").append(this.regDate);
        sb.append(", deviceToken: ").append(this.deviceToken);
        sb.append(", deviceID: ").append(this.deviceID);
        sb.append(", hwId: ").append(this.hwId);
        sb.append(", userProfile: ").append(this.userProfile);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
