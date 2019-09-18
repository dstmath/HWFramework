package android.net.lowpan;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.HexDump;
import java.util.Arrays;
import java.util.Objects;

public class LowpanCredential implements Parcelable {
    public static final Parcelable.Creator<LowpanCredential> CREATOR = new Parcelable.Creator<LowpanCredential>() {
        public LowpanCredential createFromParcel(Parcel in) {
            LowpanCredential credential = new LowpanCredential();
            byte[] unused = credential.mMasterKey = in.createByteArray();
            int unused2 = credential.mMasterKeyIndex = in.readInt();
            return credential;
        }

        public LowpanCredential[] newArray(int size) {
            return new LowpanCredential[size];
        }
    };
    public static final int UNSPECIFIED_KEY_INDEX = 0;
    /* access modifiers changed from: private */
    public byte[] mMasterKey = null;
    /* access modifiers changed from: private */
    public int mMasterKeyIndex = 0;

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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: byte[]} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void setMasterKey(byte[] masterKey) {
        if (masterKey != null) {
            masterKey = masterKey.clone();
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
        if (this.mMasterKey != null) {
            return (byte[]) this.mMasterKey.clone();
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
        boolean z = false;
        if (!(obj instanceof LowpanCredential)) {
            return false;
        }
        LowpanCredential rhs = (LowpanCredential) obj;
        if (Arrays.equals(this.mMasterKey, rhs.mMasterKey) && this.mMasterKeyIndex == rhs.mMasterKeyIndex) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(Arrays.hashCode(this.mMasterKey)), Integer.valueOf(this.mMasterKeyIndex)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mMasterKey);
        dest.writeInt(this.mMasterKeyIndex);
    }
}
