package com.huawei.permission.cloud;

import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class RecommendPermissionInfo implements Parcelable {
    public static final Parcelable.Creator<RecommendPermissionInfo> CREATOR = new Parcelable.Creator<RecommendPermissionInfo>() {
        /* class com.huawei.permission.cloud.RecommendPermissionInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RecommendPermissionInfo createFromParcel(Parcel source) {
            return new RecommendPermissionInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public RecommendPermissionInfo[] newArray(int size) {
            return new RecommendPermissionInfo[size];
        }
    };
    private static final int DEFAULT_SIZE = 16;
    public static final int FLAG_DANGEROUS_GROUP = 2;
    public static final int FLAG_SINGLE_GROUP = 1;
    private static final int MAX_LENGTH = 4096;
    public static final int TYPE_DANGEROUS = 1;
    public static final int TYPE_NORMAL = 0;
    private int mDescriptionRes;
    private int mFlags;
    private String mKey;
    private String[] mRequestPermissions;
    private String mResPackageName;
    private int mStatus;
    private int mType;

    public RecommendPermissionInfo(Parcel in) {
        int length = in.readInt();
        if (checkLength(length)) {
            this.mRequestPermissions = new String[length];
            for (int i = 0; i < length; i++) {
                this.mRequestPermissions[i] = in.readString();
            }
        }
        this.mKey = in.readString();
        this.mStatus = in.readInt();
        this.mType = in.readInt();
        this.mFlags = in.readInt();
        this.mDescriptionRes = in.readInt();
        this.mResPackageName = in.readString();
    }

    public RecommendPermissionInfo() {
    }

    public CharSequence loadDescription(PackageManager pm) {
        CharSequence label;
        int i = this.mDescriptionRes;
        if (i == 0 || (label = pm.getText(this.mResPackageName, i, null)) == null) {
            return null;
        }
        return label;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(this.mRequestPermissions);
        parcel.writeString(this.mKey);
        parcel.writeInt(this.mStatus);
        parcel.writeInt(this.mType);
        parcel.writeInt(this.mFlags);
        parcel.writeInt(this.mDescriptionRes);
        parcel.writeString(this.mResPackageName);
    }

    @Override // java.lang.Object
    @NonNull
    public String toString() {
        StringBuffer buf = new StringBuffer(16);
        buf.append("[requestPermissions=");
        String[] strArr = this.mRequestPermissions;
        if (strArr == null || strArr.length < 1) {
            buf.append("null");
        } else {
            int length = strArr.length;
            buf.append(strArr[0]);
            for (int i = 1; i < length; i++) {
                buf.append(",");
                buf.append(this.mRequestPermissions[i]);
            }
        }
        buf.append(" key=");
        buf.append(this.mKey);
        buf.append(" status=");
        buf.append(this.mStatus);
        buf.append(" type=");
        buf.append(this.mType);
        buf.append(" flags=");
        buf.append(this.mFlags);
        buf.append("]");
        return buf.toString();
    }

    public String[] getRequestPermissions() {
        return (String[]) this.mRequestPermissions.clone();
    }

    public void setRequestPermissions(String[] requestPermissions) {
        this.mRequestPermissions = (String[]) requestPermissions.clone();
    }

    public String getKey() {
        return this.mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public int getDescriptionRes() {
        return this.mDescriptionRes;
    }

    public void setDescriptionRes(int descriptionRes) {
        this.mDescriptionRes = descriptionRes;
    }

    public String getResPackageName() {
        return this.mResPackageName;
    }

    public void setResPackageName(String resPackageName) {
        this.mResPackageName = resPackageName;
    }

    private boolean checkLength(int length) {
        return length >= 0 && length < MAX_LENGTH;
    }
}
