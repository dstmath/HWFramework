package android.security.keystore.recovery;

import android.annotation.SystemApi;
import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.util.List;

@SystemApi
public final class KeyChainSnapshot implements Parcelable {
    public static final Parcelable.Creator<KeyChainSnapshot> CREATOR = new Parcelable.Creator<KeyChainSnapshot>() {
        public KeyChainSnapshot createFromParcel(Parcel in) {
            return new KeyChainSnapshot(in);
        }

        public KeyChainSnapshot[] newArray(int length) {
            return new KeyChainSnapshot[length];
        }
    };
    private static final long DEFAULT_COUNTER_ID = 1;
    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    /* access modifiers changed from: private */
    public RecoveryCertPath mCertPath;
    /* access modifiers changed from: private */
    public long mCounterId;
    /* access modifiers changed from: private */
    public byte[] mEncryptedRecoveryKeyBlob;
    /* access modifiers changed from: private */
    public List<WrappedApplicationKey> mEntryRecoveryData;
    /* access modifiers changed from: private */
    public List<KeyChainProtectionParams> mKeyChainProtectionParams;
    /* access modifiers changed from: private */
    public int mMaxAttempts;
    /* access modifiers changed from: private */
    public byte[] mServerParams;
    /* access modifiers changed from: private */
    public int mSnapshotVersion;

    public static class Builder {
        private KeyChainSnapshot mInstance = new KeyChainSnapshot();

        public Builder setSnapshotVersion(int snapshotVersion) {
            int unused = this.mInstance.mSnapshotVersion = snapshotVersion;
            return this;
        }

        public Builder setMaxAttempts(int maxAttempts) {
            int unused = this.mInstance.mMaxAttempts = maxAttempts;
            return this;
        }

        public Builder setCounterId(long counterId) {
            long unused = this.mInstance.mCounterId = counterId;
            return this;
        }

        public Builder setServerParams(byte[] serverParams) {
            byte[] unused = this.mInstance.mServerParams = serverParams;
            return this;
        }

        @Deprecated
        public Builder setTrustedHardwarePublicKey(byte[] publicKey) {
            throw new UnsupportedOperationException();
        }

        public Builder setTrustedHardwareCertPath(CertPath certPath) throws CertificateException {
            RecoveryCertPath unused = this.mInstance.mCertPath = RecoveryCertPath.createRecoveryCertPath(certPath);
            return this;
        }

        public Builder setKeyChainProtectionParams(List<KeyChainProtectionParams> keyChainProtectionParams) {
            List unused = this.mInstance.mKeyChainProtectionParams = keyChainProtectionParams;
            return this;
        }

        public Builder setWrappedApplicationKeys(List<WrappedApplicationKey> entryRecoveryData) {
            List unused = this.mInstance.mEntryRecoveryData = entryRecoveryData;
            return this;
        }

        public Builder setEncryptedRecoveryKeyBlob(byte[] encryptedRecoveryKeyBlob) {
            byte[] unused = this.mInstance.mEncryptedRecoveryKeyBlob = encryptedRecoveryKeyBlob;
            return this;
        }

        public KeyChainSnapshot build() {
            Preconditions.checkCollectionElementsNotNull(this.mInstance.mKeyChainProtectionParams, "keyChainProtectionParams");
            Preconditions.checkCollectionElementsNotNull(this.mInstance.mEntryRecoveryData, "entryRecoveryData");
            Preconditions.checkNotNull(this.mInstance.mEncryptedRecoveryKeyBlob);
            Preconditions.checkNotNull(this.mInstance.mServerParams);
            Preconditions.checkNotNull(this.mInstance.mCertPath);
            return this.mInstance;
        }
    }

    private KeyChainSnapshot() {
        this.mMaxAttempts = 10;
        this.mCounterId = 1;
    }

    public int getSnapshotVersion() {
        return this.mSnapshotVersion;
    }

    public int getMaxAttempts() {
        return this.mMaxAttempts;
    }

    public long getCounterId() {
        return this.mCounterId;
    }

    public byte[] getServerParams() {
        return this.mServerParams;
    }

    @Deprecated
    public byte[] getTrustedHardwarePublicKey() {
        throw new UnsupportedOperationException();
    }

    public CertPath getTrustedHardwareCertPath() {
        try {
            return this.mCertPath.getCertPath();
        } catch (CertificateException e) {
            throw new BadParcelableException((Exception) e);
        }
    }

    public List<KeyChainProtectionParams> getKeyChainProtectionParams() {
        return this.mKeyChainProtectionParams;
    }

    public List<WrappedApplicationKey> getWrappedApplicationKeys() {
        return this.mEntryRecoveryData;
    }

    public byte[] getEncryptedRecoveryKeyBlob() {
        return this.mEncryptedRecoveryKeyBlob;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mSnapshotVersion);
        out.writeTypedList(this.mKeyChainProtectionParams);
        out.writeByteArray(this.mEncryptedRecoveryKeyBlob);
        out.writeTypedList(this.mEntryRecoveryData);
        out.writeInt(this.mMaxAttempts);
        out.writeLong(this.mCounterId);
        out.writeByteArray(this.mServerParams);
        out.writeTypedObject(this.mCertPath, 0);
    }

    protected KeyChainSnapshot(Parcel in) {
        this.mMaxAttempts = 10;
        this.mCounterId = 1;
        this.mSnapshotVersion = in.readInt();
        this.mKeyChainProtectionParams = in.createTypedArrayList(KeyChainProtectionParams.CREATOR);
        this.mEncryptedRecoveryKeyBlob = in.createByteArray();
        this.mEntryRecoveryData = in.createTypedArrayList(WrappedApplicationKey.CREATOR);
        this.mMaxAttempts = in.readInt();
        this.mCounterId = in.readLong();
        this.mServerParams = in.createByteArray();
        this.mCertPath = (RecoveryCertPath) in.readTypedObject(RecoveryCertPath.CREATOR);
    }

    public int describeContents() {
        return 0;
    }
}
