package com.huawei.distributed.kms.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class DistributedKeySecurityLevel implements Parcelable {
    public static final Parcelable.Creator<DistributedKeySecurityLevel> CREATOR = new Parcelable.Creator<DistributedKeySecurityLevel>() {
        /* class com.huawei.distributed.kms.entity.DistributedKeySecurityLevel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DistributedKeySecurityLevel createFromParcel(Parcel source) {
            return new DistributedKeySecurityLevel(source);
        }

        @Override // android.os.Parcelable.Creator
        public DistributedKeySecurityLevel[] newArray(int size) {
            return new DistributedKeySecurityLevel[size];
        }
    };
    private static final int HASH_CODE_FACTOR = 31;
    public static final int PROCESS_TYPE_COMPOSITE_DISTRIBUTE_KEY = 3;
    public static final int PROCESS_TYPE_DELETE_REMOTE_KEY = 6;
    public static final int PROCESS_TYPE_DISTRIBUTE_KEY = 0;
    public static final int PROCESS_TYPE_GENERATE_AND_DISTRIBUTE_KEY = 5;
    public static final int PROCESS_TYPE_GENERATE_KEK = 2;
    public static final int PROCESS_TYPE_PK_PLAINTEXT = 4;
    public static final int PROCESS_TYPE_PRE_CONSULT = 1;
    public static final int TYPE_BASE_WRAP = 0;
    public static final int TYPE_DELETE = 12;
    public static final int TYPE_DELETE_KEY_ESCROW = 6;
    public static final int TYPE_LOCAL_EXISTED_KEY_ESCROW = 10;
    public static final int TYPE_LOCAL_KEY_ESCROW = 4;
    public static final int TYPE_ONE_WAY_TEE_WRAP = 11;
    public static final int TYPE_REMOTE_KEY_DECRYPT = 9;
    public static final int TYPE_REMOTE_KEY_ENCRYPT = 8;
    public static final int TYPE_REMOTE_KEY_ESCROW = 5;
    public static final int TYPE_RETRIEVE_KEY_ESCROW = 7;
    public static final int TYPE_TCIS_WRAP = 2;
    public static final int TYPE_TEE_COMPOSITE_TCIS_WRAP = 3;
    public static final int TYPE_TEE_WRAP = 1;
    private int mLevel = 0;
    private int mProcessType = 0;

    public DistributedKeySecurityLevel(int level) {
        this.mLevel = level;
    }

    public DistributedKeySecurityLevel(@NonNull Parcel source) {
        this.mLevel = source.readInt();
        this.mProcessType = source.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(@NonNull Parcel dest, int flag) {
        dest.writeInt(this.mLevel);
        dest.writeInt(this.mProcessType);
    }

    public void setProcessType(int processType) {
        this.mProcessType = processType;
    }

    public int getProcessType() {
        return this.mProcessType;
    }

    public int getLevel() {
        return this.mLevel;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (this.mLevel * 31) + this.mProcessType;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof DistributedKeySecurityLevel)) {
            return false;
        }
        DistributedKeySecurityLevel other = (DistributedKeySecurityLevel) obj;
        if (this.mLevel == other.getLevel() && this.mProcessType == other.getProcessType()) {
            return true;
        }
        return false;
    }
}
