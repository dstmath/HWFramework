package android.telephony;

import android.annotation.SystemApi;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

@SystemApi
public final class UiccAccessRule implements Parcelable {
    public static final Parcelable.Creator<UiccAccessRule> CREATOR = new Parcelable.Creator<UiccAccessRule>() {
        /* class android.telephony.UiccAccessRule.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UiccAccessRule createFromParcel(Parcel in) {
            return new UiccAccessRule(in);
        }

        @Override // android.os.Parcelable.Creator
        public UiccAccessRule[] newArray(int size) {
            return new UiccAccessRule[size];
        }
    };
    private static final int ENCODING_VERSION = 1;
    private static final String TAG = "UiccAccessRule";
    private final long mAccessType;
    private final byte[] mCertificateHash;
    private final String mPackageName;

    public static byte[] encodeRules(UiccAccessRule[] accessRules) {
        if (accessRules == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(baos);
            output.writeInt(1);
            output.writeInt(accessRules.length);
            for (UiccAccessRule accessRule : accessRules) {
                output.writeInt(accessRule.mCertificateHash.length);
                output.write(accessRule.mCertificateHash);
                if (accessRule.mPackageName != null) {
                    output.writeBoolean(true);
                    output.writeUTF(accessRule.mPackageName);
                } else {
                    output.writeBoolean(false);
                }
                output.writeLong(accessRule.mAccessType);
            }
            output.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("ByteArrayOutputStream should never lead to an IOException", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r0.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0050, code lost:
        throw r3;
     */
    public static UiccAccessRule[] decodeRules(byte[] encodedRules) {
        if (encodedRules == null) {
            return null;
        }
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(encodedRules));
            input.readInt();
            int count = input.readInt();
            UiccAccessRule[] accessRules = new UiccAccessRule[count];
            for (int i = 0; i < count; i++) {
                byte[] certificateHash = new byte[input.readInt()];
                input.readFully(certificateHash);
                accessRules[i] = new UiccAccessRule(certificateHash, input.readBoolean() ? input.readUTF() : null, input.readLong());
            }
            input.close();
            input.close();
            return accessRules;
        } catch (IOException e) {
            throw new IllegalStateException("ByteArrayInputStream should never lead to an IOException", e);
        }
    }

    public UiccAccessRule(byte[] certificateHash, String packageName, long accessType) {
        this.mCertificateHash = certificateHash;
        this.mPackageName = packageName;
        this.mAccessType = accessType;
    }

    UiccAccessRule(Parcel in) {
        this.mCertificateHash = in.createByteArray();
        this.mPackageName = in.readString();
        this.mAccessType = in.readLong();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mCertificateHash);
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mAccessType);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getCertificateHexString() {
        return IccUtils.bytesToHexString(this.mCertificateHash);
    }

    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
            throw new IllegalArgumentException("Must use GET_SIGNATURES when looking up package info");
        }
        for (Signature sig : packageInfo.signatures) {
            int accessStatus = getCarrierPrivilegeStatus(sig, packageInfo.packageName);
            if (accessStatus != 0) {
                return accessStatus;
            }
        }
        return 0;
    }

    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        byte[] certHash = getCertHash(signature, KeyProperties.DIGEST_SHA1);
        byte[] certHash256 = getCertHash(signature, KeyProperties.DIGEST_SHA256);
        if (matches(certHash, packageName) || matches(certHash256, packageName)) {
            return 1;
        }
        return 0;
    }

    private boolean matches(byte[] certHash, String packageName) {
        return certHash != null && Arrays.equals(this.mCertificateHash, certHash) && (TextUtils.isEmpty(this.mPackageName) || this.mPackageName.equals(packageName));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UiccAccessRule that = (UiccAccessRule) obj;
        if (!Arrays.equals(this.mCertificateHash, that.mCertificateHash) || !Objects.equals(this.mPackageName, that.mPackageName) || this.mAccessType != that.mAccessType) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((1 * 31) + Arrays.hashCode(this.mCertificateHash)) * 31) + Objects.hashCode(this.mPackageName)) * 31) + Objects.hashCode(Long.valueOf(this.mAccessType));
    }

    public String toString() {
        return "cert: " + IccUtils.bytesToHexString(this.mCertificateHash) + " pkg: " + this.mPackageName + " access: " + this.mAccessType;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static byte[] getCertHash(Signature signature, String algo) {
        try {
            return MessageDigest.getInstance(algo).digest(signature.toByteArray());
        } catch (NoSuchAlgorithmException ex) {
            Rlog.e(TAG, "NoSuchAlgorithmException: " + ex);
            return null;
        }
    }
}
