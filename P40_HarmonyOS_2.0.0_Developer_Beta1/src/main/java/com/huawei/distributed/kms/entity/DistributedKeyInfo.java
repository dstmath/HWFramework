package com.huawei.distributed.kms.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.Arrays;
import java.util.Objects;

public class DistributedKeyInfo implements Parcelable {
    public static final int ALLOW_ACCESS_DEFINE_CREATOR = 2;
    public static final int ALLOW_ACCESS_DEFINE_OWNER = 1;
    public static final Parcelable.Creator<DistributedKeyInfo> CREATOR = new Parcelable.Creator<DistributedKeyInfo>() {
        /* class com.huawei.distributed.kms.entity.DistributedKeyInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DistributedKeyInfo createFromParcel(Parcel source) {
            return new DistributedKeyInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DistributedKeyInfo[] newArray(int size) {
            return new DistributedKeyInfo[size];
        }
    };
    private static final int DEFAULT_LEN = 0;
    private static final int HASH_CODE_FACTOR = 31;
    private static final int MAX_ENCRYPTED_OP_KEY = 4096;
    private static final int MAX_KEY_LENGTH = 4096;
    private final String mCallerPackageName;
    private final String mCreatorPackageName;
    private byte[] mEncryptedOpKey;
    private String mKekAlias;
    private final String mOwnerPackageName;
    private byte[] mPublicKey;
    private final String mToBeWrappedKeyAlias;

    public DistributedKeyInfo(@NonNull Parcel source) {
        this.mKekAlias = BuildConfig.FLAVOR;
        this.mPublicKey = new byte[0];
        this.mEncryptedOpKey = new byte[0];
        this.mToBeWrappedKeyAlias = getStringOrDefault(source.readString());
        this.mCallerPackageName = getStringOrDefault(source.readString());
        this.mOwnerPackageName = getStringOrDefault(source.readString());
        this.mCreatorPackageName = getStringOrDefault(source.readString());
        this.mKekAlias = getStringOrDefault(source.readString());
        int publicKeyLen = source.readInt();
        this.mPublicKey = new byte[((publicKeyLen < 0 || publicKeyLen > 4096) ? 0 : publicKeyLen)];
        source.readByteArray(this.mPublicKey);
        int encryptedOpKeyLen = source.readInt();
        this.mEncryptedOpKey = new byte[((encryptedOpKeyLen < 0 || encryptedOpKeyLen > 4096) ? 0 : encryptedOpKeyLen)];
        source.readByteArray(this.mEncryptedOpKey);
    }

    public DistributedKeyInfo(String toBeWrappedKeyAlias, String callerPackageName, String ownerPackageName, String creatorPackageName) {
        this.mKekAlias = BuildConfig.FLAVOR;
        this.mPublicKey = new byte[0];
        this.mEncryptedOpKey = new byte[0];
        this.mToBeWrappedKeyAlias = getStringOrDefault(toBeWrappedKeyAlias);
        this.mCallerPackageName = getStringOrDefault(callerPackageName);
        this.mOwnerPackageName = getStringOrDefault(ownerPackageName);
        this.mCreatorPackageName = getStringOrDefault(creatorPackageName);
    }

    @Override // java.lang.Object
    public String toString() {
        return "DistributedKeyInfo{mToBeWrappedKeyAlias='" + this.mToBeWrappedKeyAlias + "', mCallerPackageName='" + this.mCallerPackageName + "', mOwnerPackageName='" + this.mOwnerPackageName + "', mCreatorPackageName='" + this.mCreatorPackageName + "', mPublicKey=" + Arrays.toString(this.mPublicKey) + ", mKekAlias='" + this.mKekAlias + "', mEncryptedOpKey=" + Arrays.toString(this.mEncryptedOpKey) + '}';
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(@NonNull Parcel dest, int flag) {
        dest.writeString(this.mToBeWrappedKeyAlias);
        dest.writeString(this.mCallerPackageName);
        dest.writeString(this.mOwnerPackageName);
        dest.writeString(this.mCreatorPackageName);
        dest.writeString(this.mKekAlias);
        dest.writeInt(this.mPublicKey.length);
        dest.writeByteArray(this.mPublicKey);
        dest.writeInt(this.mEncryptedOpKey.length);
        dest.writeByteArray(this.mEncryptedOpKey);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DistributedKeyInfo)) {
            return false;
        }
        DistributedKeyInfo that = (DistributedKeyInfo) obj;
        if (!this.mToBeWrappedKeyAlias.equals(that.mToBeWrappedKeyAlias) || !this.mCallerPackageName.equals(that.mCallerPackageName) || !this.mOwnerPackageName.equals(that.mOwnerPackageName) || !this.mCreatorPackageName.equals(that.mCreatorPackageName) || !this.mKekAlias.equals(that.mKekAlias) || !Arrays.equals(this.mPublicKey, that.mPublicKey) || !Arrays.equals(this.mEncryptedOpKey, that.mEncryptedOpKey)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((Objects.hash(this.mToBeWrappedKeyAlias, this.mCallerPackageName, this.mOwnerPackageName, this.mCreatorPackageName, this.mKekAlias) * 31) + Arrays.hashCode(this.mPublicKey)) * 31) + Arrays.hashCode(this.mEncryptedOpKey);
    }

    public String getToBeWrappedKeyAlias() {
        return this.mToBeWrappedKeyAlias;
    }

    public String getCallerPackageName() {
        return this.mCallerPackageName;
    }

    public byte[] getPublicKey() {
        return (byte[]) this.mPublicKey.clone();
    }

    public void setPublicKey(byte[] publicKey) {
        this.mPublicKey = publicKey == null ? new byte[0] : (byte[]) publicKey.clone();
    }

    public String getKekAlias() {
        return this.mKekAlias;
    }

    public void setKekAlias(String kekAlias) {
        this.mKekAlias = kekAlias;
    }

    public byte[] getEncryptedOpKey() {
        return (byte[]) this.mEncryptedOpKey.clone();
    }

    public void setEncryptedOpKey(byte[] encryptedOpKey) {
        this.mEncryptedOpKey = encryptedOpKey == null ? new byte[0] : (byte[]) encryptedOpKey.clone();
    }

    public String getOwner() {
        return this.mOwnerPackageName;
    }

    public String getCreator() {
        return this.mCreatorPackageName;
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }
}
