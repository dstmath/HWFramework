package com.huawei.distributed.kms.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.huawei.hwpartsecurity.BuildConfig;

public class DistributedDeviceInfo implements Parcelable {
    public static final Parcelable.Creator<DistributedDeviceInfo> CREATOR = new Parcelable.Creator<DistributedDeviceInfo>() {
        /* class com.huawei.distributed.kms.entity.DistributedDeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DistributedDeviceInfo createFromParcel(Parcel source) {
            return new DistributedDeviceInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DistributedDeviceInfo[] newArray(int size) {
            return new DistributedDeviceInfo[size];
        }
    };
    private static final int HASH_CODE_FACTOR = 31;
    private final String mDeviceId;
    private final String mGroupId;
    private final int mRelationType;

    public DistributedDeviceInfo(String groupId, String deviceId, int relationType) {
        this.mGroupId = getStringOrDefault(groupId);
        this.mDeviceId = getStringOrDefault(deviceId);
        this.mRelationType = relationType;
    }

    public DistributedDeviceInfo(@NonNull Parcel source) {
        this.mGroupId = getStringOrDefault(source.readString());
        this.mDeviceId = getStringOrDefault(source.readString());
        this.mRelationType = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(@NonNull Parcel dest, int flag) {
        dest.writeString(this.mGroupId);
        dest.writeString(this.mDeviceId);
        dest.writeInt(this.mRelationType);
    }

    public String getGroupId() {
        return this.mGroupId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getRelationType() {
        return this.mRelationType;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (this.mGroupId.hashCode() * 31) + this.mDeviceId.hashCode() + this.mRelationType;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof DistributedDeviceInfo)) {
            return false;
        }
        DistributedDeviceInfo other = (DistributedDeviceInfo) obj;
        if (!this.mGroupId.equals(other.getGroupId()) || !this.mDeviceId.equals(other.getDeviceId()) || this.mRelationType != other.getRelationType()) {
            return false;
        }
        return true;
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
