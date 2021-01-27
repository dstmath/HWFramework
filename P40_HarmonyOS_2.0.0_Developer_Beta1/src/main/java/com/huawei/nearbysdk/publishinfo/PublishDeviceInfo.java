package com.huawei.nearbysdk.publishinfo;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.BuildConfig;
import com.huawei.nearbysdk.HwLog;
import java.io.Serializable;
import java.util.Objects;

public class PublishDeviceInfo implements Parcelable, Serializable {
    public static final Parcelable.Creator<PublishDeviceInfo> CREATOR = new Parcelable.Creator<PublishDeviceInfo>() {
        /* class com.huawei.nearbysdk.publishinfo.PublishDeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PublishDeviceInfo createFromParcel(Parcel source) {
            int version = source.readInt();
            String modelID = source.readString();
            String subModelID = source.readString();
            byte[] sequenceNumberValue = new byte[1];
            source.readByteArray(sequenceNumberValue);
            return new Builder().withVersion(version).withModelID(modelID).withSubModelID(subModelID).withSequenceNumber(sequenceNumberValue).build();
        }

        @Override // android.os.Parcelable.Creator
        public PublishDeviceInfo[] newArray(int size) {
            return new PublishDeviceInfo[size];
        }
    };
    private static final String DEFAULT_MODELID = "";
    private static final byte[] DEFAULT_SEQUENCENUM_VALUE = {1};
    private static final String DEFAULT_SUBMODELID = "";
    private static final int DEFAULT_VERSION = 1;
    private static final int HASH_NUM = 31;
    private static final int INIT_SEQUENCENUM = -1;
    public static final int MODELID_LENGTH = 6;
    public static final int SUBMODELID_LENGTH = 2;
    private static final String TAG = "PublishDeviceInfo";
    public static final byte TAG_MODELID = 3;
    public static final byte TAG_SEQUENCENUM = 16;
    public static final byte TAG_SUB_MODELID = 4;
    private static final long serialVersionUID = 1;
    private String mModelID;
    private byte[] mSequenceNumber;
    private String mSubModelID;
    private int mVersion;

    private PublishDeviceInfo(Builder builder) {
        this.mVersion = 1;
        this.mModelID = BuildConfig.FLAVOR;
        this.mSubModelID = BuildConfig.FLAVOR;
        this.mSequenceNumber = new byte[1];
        this.mVersion = builder.mVersion;
        this.mModelID = builder.mModelID;
        this.mSubModelID = builder.mSubModelID;
        this.mSequenceNumber = builder.mSequenceNumber;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mVersion);
        dest.writeString(this.mModelID);
        dest.writeString(this.mSubModelID);
        dest.writeByteArray(this.mSequenceNumber);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void updateSequenceNum() {
        byte[] bArr = this.mSequenceNumber;
        bArr[0] = (byte) (bArr[0] + 1);
        if (this.mSequenceNumber[0] == -1) {
            byte[] bArr2 = this.mSequenceNumber;
            bArr2[0] = (byte) (bArr2[0] + 1);
        }
        HwLog.d(TAG, "update sequenceNum is " + (this.mSequenceNumber[0] & 255));
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public String getCarModelID() {
        return this.mModelID;
    }

    public void setModelID(String modelID) {
        this.mModelID = modelID;
    }

    public String getSubModelID() {
        return this.mSubModelID;
    }

    public void setSubModelID(String subModelID) {
        this.mSubModelID = subModelID;
    }

    public byte[] getSequenceNumber() {
        return (byte[]) this.mSequenceNumber.clone();
    }

    public void setSequenceNumber(byte[] sequenceNumber) {
        this.mSequenceNumber = (byte[]) sequenceNumber.clone();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PublishDeviceInfo)) {
            return false;
        }
        PublishDeviceInfo that = (PublishDeviceInfo) obj;
        if (this.mModelID == null || this.mSubModelID == null) {
            return false;
        }
        return this.mVersion == that.mVersion && this.mModelID.equals(that.mModelID) && this.mSubModelID.equals(that.mSubModelID);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mVersion), this.mModelID, this.mSubModelID);
    }

    public static class Builder {
        private String mModelID;
        private byte[] mSequenceNumber = PublishDeviceInfo.DEFAULT_SEQUENCENUM_VALUE;
        private String mSubModelID;
        private int mVersion = 1;

        public Builder withVersion(int version) {
            this.mVersion = version;
            return this;
        }

        public Builder withModelID(String modelID) {
            this.mModelID = modelID;
            return this;
        }

        public Builder withSubModelID(String subModelID) {
            this.mSubModelID = subModelID;
            return this;
        }

        public Builder withSequenceNumber(byte[] sequenceNumber) {
            this.mSequenceNumber = (byte[]) sequenceNumber.clone();
            return this;
        }

        public PublishDeviceInfo build() {
            return new PublishDeviceInfo(this);
        }
    }
}
