package android.security.keystore;

import java.security.Key;

public class AndroidKeyStoreKey implements Key {
    private final String mAlgorithm;
    private final String mAlias;
    private final int mUid;

    public AndroidKeyStoreKey(String alias, int uid, String algorithm) {
        this.mAlias = alias;
        this.mUid = uid;
        this.mAlgorithm = algorithm;
    }

    String getAlias() {
        return this.mAlias;
    }

    int getUid() {
        return this.mUid;
    }

    public String getAlgorithm() {
        return this.mAlgorithm;
    }

    public String getFormat() {
        return null;
    }

    public byte[] getEncoded() {
        return null;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((this.mAlgorithm == null ? 0 : this.mAlgorithm.hashCode()) + 31) * 31;
        if (this.mAlias != null) {
            i = this.mAlias.hashCode();
        }
        return ((hashCode + i) * 31) + this.mUid;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AndroidKeyStoreKey other = (AndroidKeyStoreKey) obj;
        if (this.mAlgorithm == null) {
            if (other.mAlgorithm != null) {
                return false;
            }
        } else if (!this.mAlgorithm.equals(other.mAlgorithm)) {
            return false;
        }
        if (this.mAlias == null) {
            if (other.mAlias != null) {
                return false;
            }
        } else if (!this.mAlias.equals(other.mAlias)) {
            return false;
        }
        return this.mUid == other.mUid;
    }
}
