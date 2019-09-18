package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.util.Log;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public final class ImsiEncryptionInfo implements Parcelable {
    public static final Parcelable.Creator<ImsiEncryptionInfo> CREATOR = new Parcelable.Creator<ImsiEncryptionInfo>() {
        public ImsiEncryptionInfo createFromParcel(Parcel in) {
            return new ImsiEncryptionInfo(in);
        }

        public ImsiEncryptionInfo[] newArray(int size) {
            return new ImsiEncryptionInfo[size];
        }
    };
    private static final String LOG_TAG = "ImsiEncryptionInfo";
    private final Date expirationTime;
    private final String keyIdentifier;
    private final int keyType;
    private final String mcc;
    private final String mnc;
    private final PublicKey publicKey;

    public ImsiEncryptionInfo(String mcc2, String mnc2, int keyType2, String keyIdentifier2, byte[] key, Date expirationTime2) {
        this(mcc2, mnc2, keyType2, keyIdentifier2, makeKeyObject(key), expirationTime2);
    }

    public ImsiEncryptionInfo(String mcc2, String mnc2, int keyType2, String keyIdentifier2, PublicKey publicKey2, Date expirationTime2) {
        this.mcc = mcc2;
        this.mnc = mnc2;
        this.keyType = keyType2;
        this.publicKey = publicKey2;
        this.keyIdentifier = keyIdentifier2;
        this.expirationTime = expirationTime2;
    }

    public ImsiEncryptionInfo(Parcel in) {
        byte[] b = new byte[in.readInt()];
        in.readByteArray(b);
        this.publicKey = makeKeyObject(b);
        this.mcc = in.readString();
        this.mnc = in.readString();
        this.keyIdentifier = in.readString();
        this.keyType = in.readInt();
        this.expirationTime = new Date(in.readLong());
    }

    public String getMnc() {
        return this.mnc;
    }

    public String getMcc() {
        return this.mcc;
    }

    public String getKeyIdentifier() {
        return this.keyIdentifier;
    }

    public int getKeyType() {
        return this.keyType;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public Date getExpirationTime() {
        return this.expirationTime;
    }

    private static PublicKey makeKeyObject(byte[] publicKeyBytes) {
        try {
            return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Log.e(LOG_TAG, "Error makeKeyObject: unable to convert into PublicKey", ex);
            throw new IllegalArgumentException();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        byte[] b = this.publicKey.getEncoded();
        dest.writeInt(b.length);
        dest.writeByteArray(b);
        dest.writeString(this.mcc);
        dest.writeString(this.mnc);
        dest.writeString(this.keyIdentifier);
        dest.writeInt(this.keyType);
        dest.writeLong(this.expirationTime.getTime());
    }

    public String toString() {
        return "[ImsiEncryptionInfo mcc=" + this.mcc + "mnc=" + this.mnc + "publicKey=" + this.publicKey + ", keyIdentifier=" + this.keyIdentifier + ", keyType=" + this.keyType + ", expirationTime=" + this.expirationTime + "]";
    }
}
