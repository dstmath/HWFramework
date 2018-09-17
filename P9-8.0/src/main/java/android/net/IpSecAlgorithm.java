package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class IpSecAlgorithm implements Parcelable {
    public static final String AUTH_HMAC_MD5 = "hmac(md5)";
    public static final String AUTH_HMAC_SHA1 = "hmac(sha1)";
    public static final String AUTH_HMAC_SHA256 = "hmac(sha256)";
    public static final String AUTH_HMAC_SHA384 = "hmac(sha384)";
    public static final String AUTH_HMAC_SHA512 = "hmac(sha512)";
    public static final Creator<IpSecAlgorithm> CREATOR = new Creator<IpSecAlgorithm>() {
        public IpSecAlgorithm createFromParcel(Parcel in) {
            return new IpSecAlgorithm(in, null);
        }

        public IpSecAlgorithm[] newArray(int size) {
            return new IpSecAlgorithm[size];
        }
    };
    public static final String CRYPT_AES_CBC = "cbc(aes)";
    private final byte[] mKey;
    private final String mName;
    private final int mTruncLenBits;

    public IpSecAlgorithm(String algorithm, byte[] key) {
        this(algorithm, key, key.length * 8);
    }

    public IpSecAlgorithm(String algoName, byte[] key, int truncLenBits) {
        if (isTruncationLengthValid(algoName, truncLenBits)) {
            this.mName = algoName;
            this.mKey = (byte[]) key.clone();
            this.mTruncLenBits = Math.min(truncLenBits, key.length * 8);
            return;
        }
        throw new IllegalArgumentException("Unknown algorithm or invalid length");
    }

    public String getName() {
        return this.mName;
    }

    public byte[] getKey() {
        return (byte[]) this.mKey.clone();
    }

    public int getTruncationLengthBits() {
        return this.mTruncLenBits;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mName);
        out.writeByteArray(this.mKey);
        out.writeInt(this.mTruncLenBits);
    }

    private IpSecAlgorithm(Parcel in) {
        this.mName = in.readString();
        this.mKey = in.createByteArray();
        this.mTruncLenBits = in.readInt();
    }

    private static boolean isTruncationLengthValid(String algo, int truncLenBits) {
        boolean z = true;
        boolean z2 = false;
        if (algo.equals(CRYPT_AES_CBC)) {
            if (!(truncLenBits == 128 || truncLenBits == 192 || truncLenBits == 256)) {
                z = false;
            }
            return z;
        } else if (algo.equals(AUTH_HMAC_MD5)) {
            if (truncLenBits >= 96 && truncLenBits <= 128) {
                z2 = true;
            }
            return z2;
        } else if (algo.equals(AUTH_HMAC_SHA1)) {
            if (truncLenBits >= 96 && truncLenBits <= 160) {
                z2 = true;
            }
            return z2;
        } else if (algo.equals(AUTH_HMAC_SHA256)) {
            if (truncLenBits >= 96 && truncLenBits <= 256) {
                z2 = true;
            }
            return z2;
        } else if (algo.equals(AUTH_HMAC_SHA384)) {
            if (truncLenBits >= 192 && truncLenBits <= 384) {
                z2 = true;
            }
            return z2;
        } else if (!algo.equals(AUTH_HMAC_SHA512)) {
            return false;
        } else {
            if (truncLenBits >= 256 && truncLenBits <= 512) {
                z2 = true;
            }
            return z2;
        }
    }
}
