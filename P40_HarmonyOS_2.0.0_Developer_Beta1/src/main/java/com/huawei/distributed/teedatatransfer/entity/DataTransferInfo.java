package com.huawei.distributed.teedatatransfer.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.Arrays;
import java.util.Objects;

public class DataTransferInfo implements Parcelable {
    public static final Parcelable.Creator<DataTransferInfo> CREATOR = new Parcelable.Creator<DataTransferInfo>() {
        /* class com.huawei.distributed.teedatatransfer.entity.DataTransferInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataTransferInfo createFromParcel(Parcel source) {
            return new DataTransferInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DataTransferInfo[] newArray(int size) {
            return new DataTransferInfo[size];
        }
    };
    private static final int DEFAULT_LEN = 0;
    private static final int HASH_CODE_FACTOR = 31;
    private static final int MAX_LENGTH = 150000;
    private byte[] mDekCipherText;
    private byte[] mFeatureCipherText;
    private int mHandle;
    private int mIsFinishedFlag;
    private byte[] mIv;
    private final String mPackageName;
    private int mRequestCode;
    private final String mTargetAlias;
    private byte[] mTlvData;
    private final int mUuidIndex;

    public DataTransferInfo(Parcel source) {
        this.mFeatureCipherText = new byte[0];
        this.mDekCipherText = new byte[0];
        this.mIv = new byte[0];
        this.mTlvData = new byte[0];
        this.mUuidIndex = source.readInt();
        this.mRequestCode = source.readInt();
        this.mIsFinishedFlag = source.readInt();
        this.mHandle = source.readInt();
        this.mTargetAlias = getStringOrDefault(source.readString());
        this.mPackageName = getStringOrDefault(source.readString());
        int featureCipherLen = source.readInt();
        this.mFeatureCipherText = new byte[((featureCipherLen < 0 || featureCipherLen > MAX_LENGTH) ? 0 : featureCipherLen)];
        source.readByteArray(this.mFeatureCipherText);
        int dekCipherLen = source.readInt();
        this.mDekCipherText = new byte[((dekCipherLen < 0 || dekCipherLen > MAX_LENGTH) ? 0 : dekCipherLen)];
        source.readByteArray(this.mDekCipherText);
        int ivLen = source.readInt();
        this.mIv = new byte[((ivLen < 0 || ivLen > MAX_LENGTH) ? 0 : ivLen)];
        source.readByteArray(this.mIv);
        int tlvDataLen = source.readInt();
        this.mTlvData = new byte[((tlvDataLen < 0 || tlvDataLen > MAX_LENGTH) ? 0 : tlvDataLen)];
        source.readByteArray(this.mTlvData);
    }

    public DataTransferInfo(int uuidIndex, String targetAlias, String packageName) {
        this.mFeatureCipherText = new byte[0];
        this.mDekCipherText = new byte[0];
        this.mIv = new byte[0];
        this.mTlvData = new byte[0];
        this.mUuidIndex = uuidIndex;
        this.mTargetAlias = getStringOrDefault(targetAlias);
        this.mPackageName = getStringOrDefault(packageName);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUuidIndex);
        dest.writeInt(this.mRequestCode);
        dest.writeInt(this.mIsFinishedFlag);
        dest.writeInt(this.mHandle);
        dest.writeString(this.mTargetAlias);
        dest.writeString(this.mPackageName);
        byteToParse(dest, this.mFeatureCipherText);
        byteToParse(dest, this.mDekCipherText);
        byteToParse(dest, this.mIv);
        byteToParse(dest, this.mTlvData);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getUuidIndex() {
        return this.mUuidIndex;
    }

    public int getRequestCode() {
        return this.mRequestCode;
    }

    public void setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
    }

    public int getFinishedFlag() {
        return this.mIsFinishedFlag;
    }

    public void setFinishedFlag(int isFinishedFlag) {
        this.mIsFinishedFlag = isFinishedFlag;
    }

    public int getHandle() {
        return this.mHandle;
    }

    public void setHandle(int handle) {
        this.mHandle = handle;
    }

    public String getTargetAlias() {
        return this.mTargetAlias;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public byte[] getFeatureCipherText() {
        return cloneIfNotEmpty(this.mFeatureCipherText);
    }

    public void setFeatureCipherText(byte[] featureCipherText) {
        this.mFeatureCipherText = cloneIfNotEmpty(featureCipherText);
    }

    public byte[] getDekCipherText() {
        return cloneIfNotEmpty(this.mDekCipherText);
    }

    public void setDekCipherText(byte[] returnCipherText) {
        this.mDekCipherText = cloneIfNotEmpty(returnCipherText);
    }

    public byte[] getIv() {
        return cloneIfNotEmpty(this.mIv);
    }

    public void setIv(byte[] iv) {
        this.mIv = cloneIfNotEmpty(iv);
    }

    public byte[] getTlvData() {
        return cloneIfNotEmpty(this.mTlvData);
    }

    public void setTlvData(byte[] tlvData) {
        this.mTlvData = cloneIfNotEmpty(tlvData);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (TextUtils.isEmpty(this.mTargetAlias) || TextUtils.isEmpty(this.mPackageName)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DataTransferInfo)) {
            return false;
        }
        DataTransferInfo that = (DataTransferInfo) obj;
        if (this.mUuidIndex != that.getUuidIndex() || this.mRequestCode != that.getRequestCode() || this.mIsFinishedFlag != that.getFinishedFlag() || this.mHandle != that.getHandle() || !this.mTargetAlias.equals(that.getTargetAlias()) || !this.mPackageName.equals(that.getPackageName()) || !Arrays.equals(this.mFeatureCipherText, that.getFeatureCipherText()) || !Arrays.equals(this.mDekCipherText, that.getDekCipherText()) || !Arrays.equals(this.mIv, that.getIv()) || !Arrays.equals(this.mTlvData, that.getTlvData())) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((((((((((((((this.mUuidIndex * 31) + this.mRequestCode) * 31) + this.mIsFinishedFlag) * 31) + this.mHandle) * 31) + Objects.hash(this.mTargetAlias, this.mPackageName)) * 31) + Arrays.hashCode(this.mFeatureCipherText)) * 31) + Arrays.hashCode(this.mDekCipherText)) * 31) + Arrays.hashCode(this.mIv)) * 31) + Arrays.hashCode(this.mTlvData);
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }

    private byte[] cloneIfNotEmpty(byte[] array) {
        return (array == null || array.length <= 0) ? new byte[0] : (byte[]) array.clone();
    }

    private void byteToParse(Parcel dest, byte[] bytes) {
        if (bytes != null) {
            dest.writeInt(bytes.length);
            dest.writeByteArray(bytes);
            return;
        }
        dest.writeInt(0);
    }
}
