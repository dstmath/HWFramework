package android.net.lowpan;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.HexDump;
import java.util.Arrays;
import java.util.Objects;

public class LowpanCredential implements Parcelable {
    public static final Parcelable.Creator<LowpanCredential> CREATOR = new Parcelable.Creator<LowpanCredential>() {
        /* class android.net.lowpan.LowpanCredential.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LowpanCredential createFromParcel(Parcel in) {
            LowpanCredential credential = new LowpanCredential();
            credential.mMasterKey = in.createByteArray();
            credential.mMasterKeyIndex = in.readInt();
            return credential;
        }

        @Override // android.os.Parcelable.Creator
        public LowpanCredential[] newArray(int size) {
            return new LowpanCredential[size];
        }
    };
    public static final int UNSPECIFIED_KEY_INDEX = 0;
    private byte[] mMasterKey = null;
    private int mMasterKeyIndex = 0;

    LowpanCredential() {
    }

    private LowpanCredential(byte[] masterKey, int keyIndex) {
        setMasterKey(masterKey, keyIndex);
    }

    private LowpanCredential(byte[] masterKey) {
        setMasterKey(masterKey);
    }

    public static LowpanCredential createMasterKey(byte[] masterKey) {
        return new LowpanCredential(masterKey);
    }

    public static LowpanCredential createMasterKey(byte[] masterKey, int keyIndex) {
        return new LowpanCredential(masterKey, keyIndex);
    }

    /* access modifiers changed from: package-private */
    public void setMasterKey(byte[] masterKey) {
        if (masterKey != null) {
            masterKey = (byte[]) masterKey.clone();
        }
        this.mMasterKey = masterKey;
    }

    /* access modifiers changed from: package-private */
    public void setMasterKeyIndex(int keyIndex) {
        this.mMasterKeyIndex = keyIndex;
    }

    /* access modifiers changed from: package-private */
    public void setMasterKey(byte[] masterKey, int keyIndex) {
        setMasterKey(masterKey);
        setMasterKeyIndex(keyIndex);
    }

    public byte[] getMasterKey() {
        byte[] bArr = this.mMasterKey;
        if (bArr != null) {
            return (byte[]) bArr.clone();
        }
        return null;
    }

    public int getMasterKeyIndex() {
        return this.mMasterKeyIndex;
    }

    public boolean isMasterKey() {
        return this.mMasterKey != null;
    }

    public String toSensitiveString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<LowpanCredential");
        if (isMasterKey()) {
            sb.append(" MasterKey:");
            sb.append(HexDump.toHexString(this.mMasterKey));
            if (this.mMasterKeyIndex != 0) {
                sb.append(", Index:");
                sb.append(this.mMasterKeyIndex);
            }
        } else {
            sb.append(" empty");
        }
        sb.append(">");
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<LowpanCredential");
        if (isMasterKey()) {
            sb.append(" MasterKey");
            if (this.mMasterKeyIndex != 0) {
                sb.append(", Index:");
                sb.append(this.mMasterKeyIndex);
            }
        } else {
            sb.append(" empty");
        }
        sb.append(">");
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LowpanCredential)) {
            return false;
        }
        LowpanCredential rhs = (LowpanCredential) obj;
        if (!Arrays.equals(this.mMasterKey, rhs.mMasterKey) || this.mMasterKeyIndex != rhs.mMasterKeyIndex) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(Arrays.hashCode(this.mMasterKey)), Integer.valueOf(this.mMasterKeyIndex));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mMasterKey);
        dest.writeInt(this.mMasterKeyIndex);
    }
}
