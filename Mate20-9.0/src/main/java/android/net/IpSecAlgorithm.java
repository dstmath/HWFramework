package android.net;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.HexDump;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public final class IpSecAlgorithm implements Parcelable {
    public static final String AUTH_CRYPT_AES_GCM = "rfc4106(gcm(aes))";
    public static final String AUTH_HMAC_MD5 = "hmac(md5)";
    public static final String AUTH_HMAC_SHA1 = "hmac(sha1)";
    public static final String AUTH_HMAC_SHA256 = "hmac(sha256)";
    public static final String AUTH_HMAC_SHA384 = "hmac(sha384)";
    public static final String AUTH_HMAC_SHA512 = "hmac(sha512)";
    public static final Parcelable.Creator<IpSecAlgorithm> CREATOR = new Parcelable.Creator<IpSecAlgorithm>() {
        public IpSecAlgorithm createFromParcel(Parcel in) {
            return new IpSecAlgorithm(in.readString(), in.createByteArray(), in.readInt());
        }

        public IpSecAlgorithm[] newArray(int size) {
            return new IpSecAlgorithm[size];
        }
    };
    public static final String CRYPT_AES_CBC = "cbc(aes)";
    public static final String CRYPT_NULL = "ecb(cipher_null)";
    private static final String TAG = "IpSecAlgorithm";
    private final byte[] mKey;
    private final String mName;
    private final int mTruncLenBits;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AlgorithmName {
    }

    public IpSecAlgorithm(String algorithm, byte[] key) {
        this(algorithm, key, 0);
    }

    public IpSecAlgorithm(String algorithm, byte[] key, int truncLenBits) {
        this.mName = algorithm;
        this.mKey = (byte[]) key.clone();
        this.mTruncLenBits = truncLenBits;
        checkValidOrThrow(this.mName, this.mKey.length * 8, this.mTruncLenBits);
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static void checkValidOrThrow(String name, int keyLen, int truncLen) {
        char c;
        boolean isValidLen;
        boolean isValidTruncLen = true;
        boolean z = false;
        switch (name.hashCode()) {
            case -1137603038:
                if (name.equals(AUTH_CRYPT_AES_GCM)) {
                    c = 6;
                    break;
                }
            case 394796030:
                if (name.equals(CRYPT_AES_CBC)) {
                    c = 0;
                    break;
                }
            case 559425185:
                if (name.equals(AUTH_HMAC_SHA256)) {
                    c = 3;
                    break;
                }
            case 559457797:
                if (name.equals(AUTH_HMAC_SHA384)) {
                    c = 4;
                    break;
                }
            case 559510590:
                if (name.equals(AUTH_HMAC_SHA512)) {
                    c = 5;
                    break;
                }
            case 759177996:
                if (name.equals(AUTH_HMAC_MD5)) {
                    c = 1;
                    break;
                }
            case 2065384259:
                if (name.equals(AUTH_HMAC_SHA1)) {
                    c = 2;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                if (keyLen == 128 || keyLen == 192 || keyLen == 256) {
                    z = true;
                }
                isValidLen = z;
                break;
            case 1:
                isValidLen = keyLen == 128;
                if (truncLen >= 96 && truncLen <= 128) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            case 2:
                isValidLen = keyLen == 160;
                if (truncLen >= 96 && truncLen <= 160) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            case 3:
                isValidLen = keyLen == 256;
                if (truncLen >= 96 && truncLen <= 256) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            case 4:
                isValidLen = keyLen == 384;
                if (truncLen >= 192 && truncLen <= 384) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            case 5:
                isValidLen = keyLen == 512;
                if (truncLen >= 256 && truncLen <= 512) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            case 6:
                isValidLen = keyLen == 160 || keyLen == 224 || keyLen == 288;
                if (truncLen == 64 || truncLen == 96 || truncLen == 128) {
                    z = true;
                }
                isValidTruncLen = z;
                break;
            default:
                throw new IllegalArgumentException("Couldn't find an algorithm: " + name);
        }
        if (!isValidLen) {
            throw new IllegalArgumentException("Invalid key material keyLength: " + keyLen);
        } else if (!isValidTruncLen) {
            throw new IllegalArgumentException("Invalid truncation keyLength: " + truncLen);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean isAuthentication() {
        char c;
        String name = getName();
        switch (name.hashCode()) {
            case 559425185:
                if (name.equals(AUTH_HMAC_SHA256)) {
                    c = 2;
                    break;
                }
            case 559457797:
                if (name.equals(AUTH_HMAC_SHA384)) {
                    c = 3;
                    break;
                }
            case 559510590:
                if (name.equals(AUTH_HMAC_SHA512)) {
                    c = 4;
                    break;
                }
            case 759177996:
                if (name.equals(AUTH_HMAC_MD5)) {
                    c = 0;
                    break;
                }
            case 2065384259:
                if (name.equals(AUTH_HMAC_SHA1)) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    public boolean isEncryption() {
        return getName().equals(CRYPT_AES_CBC);
    }

    public boolean isAead() {
        return getName().equals(AUTH_CRYPT_AES_GCM);
    }

    private static boolean isUnsafeBuild() {
        return Build.IS_DEBUGGABLE && Build.IS_ENG;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{mName=");
        sb.append(this.mName);
        sb.append(", mKey=");
        sb.append(isUnsafeBuild() ? HexDump.toHexString(this.mKey) : "<hidden>");
        sb.append(", mTruncLenBits=");
        sb.append(this.mTruncLenBits);
        sb.append("}");
        return sb.toString();
    }

    @VisibleForTesting
    public static boolean equals(IpSecAlgorithm lhs, IpSecAlgorithm rhs) {
        boolean z = false;
        if (lhs == null || rhs == null) {
            if (lhs == rhs) {
                z = true;
            }
            return z;
        }
        if (lhs.mName.equals(rhs.mName) && Arrays.equals(lhs.mKey, rhs.mKey) && lhs.mTruncLenBits == rhs.mTruncLenBits) {
            z = true;
        }
        return z;
    }
}
