package android.security.keystore;

import android.os.Parcel;
import android.os.Parcelable;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

public final class ParcelableKeyGenParameterSpec implements Parcelable {
    private static final int ALGORITHM_PARAMETER_SPEC_EC = 3;
    private static final int ALGORITHM_PARAMETER_SPEC_NONE = 1;
    private static final int ALGORITHM_PARAMETER_SPEC_RSA = 2;
    public static final Parcelable.Creator<ParcelableKeyGenParameterSpec> CREATOR = new Parcelable.Creator<ParcelableKeyGenParameterSpec>() {
        /* class android.security.keystore.ParcelableKeyGenParameterSpec.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableKeyGenParameterSpec createFromParcel(Parcel in) {
            return new ParcelableKeyGenParameterSpec(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableKeyGenParameterSpec[] newArray(int size) {
            return new ParcelableKeyGenParameterSpec[size];
        }
    };
    private final KeyGenParameterSpec mSpec;

    public ParcelableKeyGenParameterSpec(KeyGenParameterSpec spec) {
        this.mSpec = spec;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static void writeOptionalDate(Parcel out, Date date) {
        if (date != null) {
            out.writeBoolean(true);
            out.writeLong(date.getTime());
            return;
        }
        out.writeBoolean(false);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mSpec.getKeystoreAlias());
        out.writeInt(this.mSpec.getPurposes());
        out.writeInt(this.mSpec.getUid());
        out.writeInt(this.mSpec.getKeySize());
        AlgorithmParameterSpec algoSpec = this.mSpec.getAlgorithmParameterSpec();
        if (algoSpec == null) {
            out.writeInt(1);
        } else if ((algoSpec instanceof RSAKeyGenParameterSpec) == 1) {
            RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algoSpec;
            out.writeInt(2);
            out.writeInt(rsaSpec.getKeysize());
            out.writeByteArray(rsaSpec.getPublicExponent().toByteArray());
        } else if ((algoSpec instanceof ECGenParameterSpec) == 1) {
            out.writeInt(3);
            out.writeString(((ECGenParameterSpec) algoSpec).getName());
        } else {
            throw new IllegalArgumentException(String.format("Unknown algorithm parameter spec: %s", algoSpec.getClass()));
        }
        out.writeByteArray(this.mSpec.getCertificateSubject().getEncoded());
        out.writeByteArray(this.mSpec.getCertificateSerialNumber().toByteArray());
        out.writeLong(this.mSpec.getCertificateNotBefore().getTime());
        out.writeLong(this.mSpec.getCertificateNotAfter().getTime());
        writeOptionalDate(out, this.mSpec.getKeyValidityStart());
        writeOptionalDate(out, this.mSpec.getKeyValidityForOriginationEnd());
        writeOptionalDate(out, this.mSpec.getKeyValidityForConsumptionEnd());
        if (this.mSpec.isDigestsSpecified() == 1) {
            out.writeStringArray(this.mSpec.getDigests());
        } else {
            out.writeStringArray(null);
        }
        out.writeStringArray(this.mSpec.getEncryptionPaddings());
        out.writeStringArray(this.mSpec.getSignaturePaddings());
        out.writeStringArray(this.mSpec.getBlockModes());
        out.writeBoolean(this.mSpec.isRandomizedEncryptionRequired());
        out.writeBoolean(this.mSpec.isUserAuthenticationRequired());
        out.writeInt(this.mSpec.getUserAuthenticationValidityDurationSeconds());
        out.writeBoolean(this.mSpec.isUserPresenceRequired());
        out.writeByteArray(this.mSpec.getAttestationChallenge());
        out.writeBoolean(this.mSpec.isUniqueIdIncluded());
        out.writeBoolean(this.mSpec.isUserAuthenticationValidWhileOnBody());
        out.writeBoolean(this.mSpec.isInvalidatedByBiometricEnrollment());
        out.writeBoolean(this.mSpec.isStrongBoxBacked());
        out.writeBoolean(this.mSpec.isUserConfirmationRequired());
        out.writeBoolean(this.mSpec.isUnlockedDeviceRequired());
    }

    private static Date readDateOrNull(Parcel in) {
        if (in.readBoolean() == 1) {
            return new Date(in.readLong());
        }
        return null;
    }

    private ParcelableKeyGenParameterSpec(Parcel in) {
        AlgorithmParameterSpec algorithmSpec;
        String keystoreAlias = in.readString();
        int purposes = in.readInt();
        int uid = in.readInt();
        int keySize = in.readInt();
        int keySpecType = in.readInt();
        if (keySpecType == 1) {
            algorithmSpec = null;
        } else if (keySpecType == 2) {
            algorithmSpec = new RSAKeyGenParameterSpec(in.readInt(), new BigInteger(in.createByteArray()));
        } else if (keySpecType == 3) {
            algorithmSpec = new ECGenParameterSpec(in.readString());
        } else {
            throw new IllegalArgumentException(String.format("Unknown algorithm parameter spec: %d", Integer.valueOf(keySpecType)));
        }
        this.mSpec = new KeyGenParameterSpec(keystoreAlias, uid, keySize, algorithmSpec, new X500Principal(in.createByteArray()), new BigInteger(in.createByteArray()), new Date(in.readLong()), new Date(in.readLong()), readDateOrNull(in), readDateOrNull(in), readDateOrNull(in), purposes, in.createStringArray(), in.createStringArray(), in.createStringArray(), in.createStringArray(), in.readBoolean(), in.readBoolean(), in.readInt(), in.readBoolean(), in.createByteArray(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean());
    }

    public KeyGenParameterSpec getSpec() {
        return this.mSpec;
    }
}
