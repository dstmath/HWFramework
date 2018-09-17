package android.security;

import android.content.Context;
import android.security.keystore.KeyProperties.KeyAlgorithm;
import android.text.TextUtils;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

@Deprecated
public final class KeyPairGeneratorSpec implements AlgorithmParameterSpec {
    private final Context mContext;
    private final Date mEndDate;
    private final int mFlags;
    private final int mKeySize;
    private final String mKeyType;
    private final String mKeystoreAlias;
    private final BigInteger mSerialNumber;
    private final AlgorithmParameterSpec mSpec;
    private final Date mStartDate;
    private final X500Principal mSubjectDN;

    @Deprecated
    public static final class Builder {
        private final Context mContext;
        private Date mEndDate;
        private int mFlags;
        private int mKeySize = -1;
        private String mKeyType;
        private String mKeystoreAlias;
        private BigInteger mSerialNumber;
        private AlgorithmParameterSpec mSpec;
        private Date mStartDate;
        private X500Principal mSubjectDN;

        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("context == null");
            }
            this.mContext = context;
        }

        public Builder setAlias(String alias) {
            if (alias == null) {
                throw new NullPointerException("alias == null");
            }
            this.mKeystoreAlias = alias;
            return this;
        }

        public Builder setKeyType(String keyType) throws NoSuchAlgorithmException {
            if (keyType == null) {
                throw new NullPointerException("keyType == null");
            }
            try {
                KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(keyType);
                this.mKeyType = keyType;
                return this;
            } catch (IllegalArgumentException e) {
                throw new NoSuchAlgorithmException("Unsupported key type: " + keyType);
            }
        }

        public Builder setKeySize(int keySize) {
            if (keySize < 0) {
                throw new IllegalArgumentException("keySize < 0");
            }
            this.mKeySize = keySize;
            return this;
        }

        public Builder setAlgorithmParameterSpec(AlgorithmParameterSpec spec) {
            if (spec == null) {
                throw new NullPointerException("spec == null");
            }
            this.mSpec = spec;
            return this;
        }

        public Builder setSubject(X500Principal subject) {
            if (subject == null) {
                throw new NullPointerException("subject == null");
            }
            this.mSubjectDN = subject;
            return this;
        }

        public Builder setSerialNumber(BigInteger serialNumber) {
            if (serialNumber == null) {
                throw new NullPointerException("serialNumber == null");
            }
            this.mSerialNumber = serialNumber;
            return this;
        }

        public Builder setStartDate(Date startDate) {
            if (startDate == null) {
                throw new NullPointerException("startDate == null");
            }
            this.mStartDate = startDate;
            return this;
        }

        public Builder setEndDate(Date endDate) {
            if (endDate == null) {
                throw new NullPointerException("endDate == null");
            }
            this.mEndDate = endDate;
            return this;
        }

        public Builder setEncryptionRequired() {
            this.mFlags |= 1;
            return this;
        }

        public KeyPairGeneratorSpec build() {
            return new KeyPairGeneratorSpec(this.mContext, this.mKeystoreAlias, this.mKeyType, this.mKeySize, this.mSpec, this.mSubjectDN, this.mSerialNumber, this.mStartDate, this.mEndDate, this.mFlags);
        }
    }

    public KeyPairGeneratorSpec(Context context, String keyStoreAlias, String keyType, int keySize, AlgorithmParameterSpec spec, X500Principal subjectDN, BigInteger serialNumber, Date startDate, Date endDate, int flags) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        } else if (TextUtils.isEmpty(keyStoreAlias)) {
            throw new IllegalArgumentException("keyStoreAlias must not be empty");
        } else if (subjectDN == null) {
            throw new IllegalArgumentException("subjectDN == null");
        } else if (serialNumber == null) {
            throw new IllegalArgumentException("serialNumber == null");
        } else if (startDate == null) {
            throw new IllegalArgumentException("startDate == null");
        } else if (endDate == null) {
            throw new IllegalArgumentException("endDate == null");
        } else if (endDate.before(startDate)) {
            throw new IllegalArgumentException("endDate < startDate");
        } else if (endDate.before(startDate)) {
            throw new IllegalArgumentException("endDate < startDate");
        } else {
            this.mContext = context;
            this.mKeystoreAlias = keyStoreAlias;
            this.mKeyType = keyType;
            this.mKeySize = keySize;
            this.mSpec = spec;
            this.mSubjectDN = subjectDN;
            this.mSerialNumber = serialNumber;
            this.mStartDate = startDate;
            this.mEndDate = endDate;
            this.mFlags = flags;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public String getKeystoreAlias() {
        return this.mKeystoreAlias;
    }

    public String getKeyType() {
        return this.mKeyType;
    }

    public int getKeySize() {
        return this.mKeySize;
    }

    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.mSpec;
    }

    public X500Principal getSubjectDN() {
        return this.mSubjectDN;
    }

    public BigInteger getSerialNumber() {
        return this.mSerialNumber;
    }

    public Date getStartDate() {
        return this.mStartDate;
    }

    public Date getEndDate() {
        return this.mEndDate;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public boolean isEncryptionRequired() {
        return (this.mFlags & 1) != 0;
    }
}
