package android.security.keystore;

import android.annotation.UnsupportedAppUsage;
import android.text.TextUtils;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

public final class KeyGenParameterSpec implements AlgorithmParameterSpec, UserAuthArgs {
    private static final Date DEFAULT_CERT_NOT_AFTER = new Date(2461449600000L);
    private static final Date DEFAULT_CERT_NOT_BEFORE = new Date(0);
    private static final BigInteger DEFAULT_CERT_SERIAL_NUMBER = new BigInteger("1");
    private static final X500Principal DEFAULT_CERT_SUBJECT = new X500Principal("CN=fake");
    private final byte[] mAttestationChallenge;
    private final String[] mBlockModes;
    private final Date mCertificateNotAfter;
    private final Date mCertificateNotBefore;
    private final BigInteger mCertificateSerialNumber;
    private final X500Principal mCertificateSubject;
    private final String[] mDigests;
    private final String[] mEncryptionPaddings;
    private final boolean mInvalidatedByBiometricEnrollment;
    private final boolean mIsStrongBoxBacked;
    private final int mKeySize;
    private final Date mKeyValidityForConsumptionEnd;
    private final Date mKeyValidityForOriginationEnd;
    private final Date mKeyValidityStart;
    private final String mKeystoreAlias;
    private final int mPurposes;
    private final boolean mRandomizedEncryptionRequired;
    private final String[] mSignaturePaddings;
    private final AlgorithmParameterSpec mSpec;
    private final int mUid;
    private final boolean mUniqueIdIncluded;
    private final boolean mUnlockedDeviceRequired;
    private final boolean mUserAuthenticationRequired;
    private final boolean mUserAuthenticationValidWhileOnBody;
    private final int mUserAuthenticationValidityDurationSeconds;
    private final boolean mUserConfirmationRequired;
    private final boolean mUserPresenceRequired;

    public KeyGenParameterSpec(String keyStoreAlias, int uid, int keySize, AlgorithmParameterSpec spec, X500Principal certificateSubject, BigInteger certificateSerialNumber, Date certificateNotBefore, Date certificateNotAfter, Date keyValidityStart, Date keyValidityForOriginationEnd, Date keyValidityForConsumptionEnd, int purposes, String[] digests, String[] encryptionPaddings, String[] signaturePaddings, String[] blockModes, boolean randomizedEncryptionRequired, boolean userAuthenticationRequired, int userAuthenticationValidityDurationSeconds, boolean userPresenceRequired, byte[] attestationChallenge, boolean uniqueIdIncluded, boolean userAuthenticationValidWhileOnBody, boolean invalidatedByBiometricEnrollment, boolean isStrongBoxBacked, boolean userConfirmationRequired, boolean unlockedDeviceRequired) {
        X500Principal certificateSubject2;
        Date certificateNotBefore2;
        Date certificateNotAfter2;
        BigInteger certificateSerialNumber2;
        if (!TextUtils.isEmpty(keyStoreAlias)) {
            if (certificateSubject == null) {
                certificateSubject2 = DEFAULT_CERT_SUBJECT;
            } else {
                certificateSubject2 = certificateSubject;
            }
            if (certificateNotBefore == null) {
                certificateNotBefore2 = DEFAULT_CERT_NOT_BEFORE;
            } else {
                certificateNotBefore2 = certificateNotBefore;
            }
            if (certificateNotAfter == null) {
                certificateNotAfter2 = DEFAULT_CERT_NOT_AFTER;
            } else {
                certificateNotAfter2 = certificateNotAfter;
            }
            if (certificateSerialNumber == null) {
                certificateSerialNumber2 = DEFAULT_CERT_SERIAL_NUMBER;
            } else {
                certificateSerialNumber2 = certificateSerialNumber;
            }
            if (!certificateNotAfter2.before(certificateNotBefore2)) {
                this.mKeystoreAlias = keyStoreAlias;
                this.mUid = uid;
                this.mKeySize = keySize;
                this.mSpec = spec;
                this.mCertificateSubject = certificateSubject2;
                this.mCertificateSerialNumber = certificateSerialNumber2;
                this.mCertificateNotBefore = Utils.cloneIfNotNull(certificateNotBefore2);
                this.mCertificateNotAfter = Utils.cloneIfNotNull(certificateNotAfter2);
                this.mKeyValidityStart = Utils.cloneIfNotNull(keyValidityStart);
                this.mKeyValidityForOriginationEnd = Utils.cloneIfNotNull(keyValidityForOriginationEnd);
                this.mKeyValidityForConsumptionEnd = Utils.cloneIfNotNull(keyValidityForConsumptionEnd);
                this.mPurposes = purposes;
                this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
                this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(encryptionPaddings));
                this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(signaturePaddings));
                this.mBlockModes = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(blockModes));
                this.mRandomizedEncryptionRequired = randomizedEncryptionRequired;
                this.mUserAuthenticationRequired = userAuthenticationRequired;
                this.mUserPresenceRequired = userPresenceRequired;
                this.mUserAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
                this.mAttestationChallenge = Utils.cloneIfNotNull(attestationChallenge);
                this.mUniqueIdIncluded = uniqueIdIncluded;
                this.mUserAuthenticationValidWhileOnBody = userAuthenticationValidWhileOnBody;
                this.mInvalidatedByBiometricEnrollment = invalidatedByBiometricEnrollment;
                this.mIsStrongBoxBacked = isStrongBoxBacked;
                this.mUserConfirmationRequired = userConfirmationRequired;
                this.mUnlockedDeviceRequired = unlockedDeviceRequired;
                return;
            }
            throw new IllegalArgumentException("certificateNotAfter < certificateNotBefore");
        }
        throw new IllegalArgumentException("keyStoreAlias must not be empty");
    }

    public String getKeystoreAlias() {
        return this.mKeystoreAlias;
    }

    @UnsupportedAppUsage
    public int getUid() {
        return this.mUid;
    }

    public int getKeySize() {
        return this.mKeySize;
    }

    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.mSpec;
    }

    public X500Principal getCertificateSubject() {
        return this.mCertificateSubject;
    }

    public BigInteger getCertificateSerialNumber() {
        return this.mCertificateSerialNumber;
    }

    public Date getCertificateNotBefore() {
        return Utils.cloneIfNotNull(this.mCertificateNotBefore);
    }

    public Date getCertificateNotAfter() {
        return Utils.cloneIfNotNull(this.mCertificateNotAfter);
    }

    public Date getKeyValidityStart() {
        return Utils.cloneIfNotNull(this.mKeyValidityStart);
    }

    public Date getKeyValidityForConsumptionEnd() {
        return Utils.cloneIfNotNull(this.mKeyValidityForConsumptionEnd);
    }

    public Date getKeyValidityForOriginationEnd() {
        return Utils.cloneIfNotNull(this.mKeyValidityForOriginationEnd);
    }

    public int getPurposes() {
        return this.mPurposes;
    }

    public String[] getDigests() {
        String[] strArr = this.mDigests;
        if (strArr != null) {
            return ArrayUtils.cloneIfNotEmpty(strArr);
        }
        throw new IllegalStateException("Digests not specified");
    }

    public boolean isDigestsSpecified() {
        return this.mDigests != null;
    }

    public String[] getEncryptionPaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mEncryptionPaddings);
    }

    public String[] getSignaturePaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mSignaturePaddings);
    }

    public String[] getBlockModes() {
        return ArrayUtils.cloneIfNotEmpty(this.mBlockModes);
    }

    public boolean isRandomizedEncryptionRequired() {
        return this.mRandomizedEncryptionRequired;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isUserAuthenticationRequired() {
        return this.mUserAuthenticationRequired;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isUserConfirmationRequired() {
        return this.mUserConfirmationRequired;
    }

    @Override // android.security.keystore.UserAuthArgs
    public int getUserAuthenticationValidityDurationSeconds() {
        return this.mUserAuthenticationValidityDurationSeconds;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isUserPresenceRequired() {
        return this.mUserPresenceRequired;
    }

    public byte[] getAttestationChallenge() {
        return Utils.cloneIfNotNull(this.mAttestationChallenge);
    }

    @UnsupportedAppUsage
    public boolean isUniqueIdIncluded() {
        return this.mUniqueIdIncluded;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isUserAuthenticationValidWhileOnBody() {
        return this.mUserAuthenticationValidWhileOnBody;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isInvalidatedByBiometricEnrollment() {
        return this.mInvalidatedByBiometricEnrollment;
    }

    public boolean isStrongBoxBacked() {
        return this.mIsStrongBoxBacked;
    }

    @Override // android.security.keystore.UserAuthArgs
    public boolean isUnlockedDeviceRequired() {
        return this.mUnlockedDeviceRequired;
    }

    @Override // android.security.keystore.UserAuthArgs
    public long getBoundToSpecificSecureUserId() {
        return 0;
    }

    public static final class Builder {
        private byte[] mAttestationChallenge;
        private String[] mBlockModes;
        private Date mCertificateNotAfter;
        private Date mCertificateNotBefore;
        private BigInteger mCertificateSerialNumber;
        private X500Principal mCertificateSubject;
        private String[] mDigests;
        private String[] mEncryptionPaddings;
        private boolean mInvalidatedByBiometricEnrollment;
        private boolean mIsStrongBoxBacked;
        private int mKeySize;
        private Date mKeyValidityForConsumptionEnd;
        private Date mKeyValidityForOriginationEnd;
        private Date mKeyValidityStart;
        private final String mKeystoreAlias;
        private int mPurposes;
        private boolean mRandomizedEncryptionRequired;
        private String[] mSignaturePaddings;
        private AlgorithmParameterSpec mSpec;
        private int mUid;
        private boolean mUniqueIdIncluded;
        private boolean mUnlockedDeviceRequired;
        private boolean mUserAuthenticationRequired;
        private boolean mUserAuthenticationValidWhileOnBody;
        private int mUserAuthenticationValidityDurationSeconds;
        private boolean mUserConfirmationRequired;
        private boolean mUserPresenceRequired;

        public Builder(String keystoreAlias, int purposes) {
            this.mUid = -1;
            this.mKeySize = -1;
            this.mRandomizedEncryptionRequired = true;
            this.mUserAuthenticationValidityDurationSeconds = -1;
            this.mUserPresenceRequired = false;
            this.mAttestationChallenge = null;
            this.mUniqueIdIncluded = false;
            this.mInvalidatedByBiometricEnrollment = true;
            this.mIsStrongBoxBacked = false;
            this.mUnlockedDeviceRequired = false;
            if (keystoreAlias == null) {
                throw new NullPointerException("keystoreAlias == null");
            } else if (!keystoreAlias.isEmpty()) {
                this.mKeystoreAlias = keystoreAlias;
                this.mPurposes = purposes;
            } else {
                throw new IllegalArgumentException("keystoreAlias must not be empty");
            }
        }

        public Builder(KeyGenParameterSpec sourceSpec) {
            this(sourceSpec.getKeystoreAlias(), sourceSpec.getPurposes());
            this.mUid = sourceSpec.getUid();
            this.mKeySize = sourceSpec.getKeySize();
            this.mSpec = sourceSpec.getAlgorithmParameterSpec();
            this.mCertificateSubject = sourceSpec.getCertificateSubject();
            this.mCertificateSerialNumber = sourceSpec.getCertificateSerialNumber();
            this.mCertificateNotBefore = sourceSpec.getCertificateNotBefore();
            this.mCertificateNotAfter = sourceSpec.getCertificateNotAfter();
            this.mKeyValidityStart = sourceSpec.getKeyValidityStart();
            this.mKeyValidityForOriginationEnd = sourceSpec.getKeyValidityForOriginationEnd();
            this.mKeyValidityForConsumptionEnd = sourceSpec.getKeyValidityForConsumptionEnd();
            this.mPurposes = sourceSpec.getPurposes();
            if (sourceSpec.isDigestsSpecified()) {
                this.mDigests = sourceSpec.getDigests();
            }
            this.mEncryptionPaddings = sourceSpec.getEncryptionPaddings();
            this.mSignaturePaddings = sourceSpec.getSignaturePaddings();
            this.mBlockModes = sourceSpec.getBlockModes();
            this.mRandomizedEncryptionRequired = sourceSpec.isRandomizedEncryptionRequired();
            this.mUserAuthenticationRequired = sourceSpec.isUserAuthenticationRequired();
            this.mUserAuthenticationValidityDurationSeconds = sourceSpec.getUserAuthenticationValidityDurationSeconds();
            this.mUserPresenceRequired = sourceSpec.isUserPresenceRequired();
            this.mAttestationChallenge = sourceSpec.getAttestationChallenge();
            this.mUniqueIdIncluded = sourceSpec.isUniqueIdIncluded();
            this.mUserAuthenticationValidWhileOnBody = sourceSpec.isUserAuthenticationValidWhileOnBody();
            this.mInvalidatedByBiometricEnrollment = sourceSpec.isInvalidatedByBiometricEnrollment();
            this.mIsStrongBoxBacked = sourceSpec.isStrongBoxBacked();
            this.mUserConfirmationRequired = sourceSpec.isUserConfirmationRequired();
            this.mUnlockedDeviceRequired = sourceSpec.isUnlockedDeviceRequired();
        }

        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        public Builder setKeySize(int keySize) {
            if (keySize >= 0) {
                this.mKeySize = keySize;
                return this;
            }
            throw new IllegalArgumentException("keySize < 0");
        }

        public Builder setAlgorithmParameterSpec(AlgorithmParameterSpec spec) {
            if (spec != null) {
                this.mSpec = spec;
                return this;
            }
            throw new NullPointerException("spec == null");
        }

        public Builder setCertificateSubject(X500Principal subject) {
            if (subject != null) {
                this.mCertificateSubject = subject;
                return this;
            }
            throw new NullPointerException("subject == null");
        }

        public Builder setCertificateSerialNumber(BigInteger serialNumber) {
            if (serialNumber != null) {
                this.mCertificateSerialNumber = serialNumber;
                return this;
            }
            throw new NullPointerException("serialNumber == null");
        }

        public Builder setCertificateNotBefore(Date date) {
            if (date != null) {
                this.mCertificateNotBefore = Utils.cloneIfNotNull(date);
                return this;
            }
            throw new NullPointerException("date == null");
        }

        public Builder setCertificateNotAfter(Date date) {
            if (date != null) {
                this.mCertificateNotAfter = Utils.cloneIfNotNull(date);
                return this;
            }
            throw new NullPointerException("date == null");
        }

        public Builder setKeyValidityStart(Date startDate) {
            this.mKeyValidityStart = Utils.cloneIfNotNull(startDate);
            return this;
        }

        public Builder setKeyValidityEnd(Date endDate) {
            setKeyValidityForOriginationEnd(endDate);
            setKeyValidityForConsumptionEnd(endDate);
            return this;
        }

        public Builder setKeyValidityForOriginationEnd(Date endDate) {
            this.mKeyValidityForOriginationEnd = Utils.cloneIfNotNull(endDate);
            return this;
        }

        public Builder setKeyValidityForConsumptionEnd(Date endDate) {
            this.mKeyValidityForConsumptionEnd = Utils.cloneIfNotNull(endDate);
            return this;
        }

        public Builder setDigests(String... digests) {
            this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
            return this;
        }

        public Builder setEncryptionPaddings(String... paddings) {
            this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        public Builder setSignaturePaddings(String... paddings) {
            this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        public Builder setBlockModes(String... blockModes) {
            this.mBlockModes = ArrayUtils.cloneIfNotEmpty(blockModes);
            return this;
        }

        public Builder setRandomizedEncryptionRequired(boolean required) {
            this.mRandomizedEncryptionRequired = required;
            return this;
        }

        public Builder setUserAuthenticationRequired(boolean required) {
            this.mUserAuthenticationRequired = required;
            return this;
        }

        public Builder setUserConfirmationRequired(boolean required) {
            this.mUserConfirmationRequired = required;
            return this;
        }

        public Builder setUserAuthenticationValidityDurationSeconds(int seconds) {
            if (seconds >= -1) {
                this.mUserAuthenticationValidityDurationSeconds = seconds;
                return this;
            }
            throw new IllegalArgumentException("seconds must be -1 or larger");
        }

        public Builder setUserPresenceRequired(boolean required) {
            this.mUserPresenceRequired = required;
            return this;
        }

        public Builder setAttestationChallenge(byte[] attestationChallenge) {
            this.mAttestationChallenge = attestationChallenge;
            return this;
        }

        public Builder setUniqueIdIncluded(boolean uniqueIdIncluded) {
            this.mUniqueIdIncluded = uniqueIdIncluded;
            return this;
        }

        public Builder setUserAuthenticationValidWhileOnBody(boolean remainsValid) {
            this.mUserAuthenticationValidWhileOnBody = remainsValid;
            return this;
        }

        public Builder setInvalidatedByBiometricEnrollment(boolean invalidateKey) {
            this.mInvalidatedByBiometricEnrollment = invalidateKey;
            return this;
        }

        public Builder setIsStrongBoxBacked(boolean isStrongBoxBacked) {
            this.mIsStrongBoxBacked = isStrongBoxBacked;
            return this;
        }

        public Builder setUnlockedDeviceRequired(boolean unlockedDeviceRequired) {
            this.mUnlockedDeviceRequired = unlockedDeviceRequired;
            return this;
        }

        public KeyGenParameterSpec build() {
            return new KeyGenParameterSpec(this.mKeystoreAlias, this.mUid, this.mKeySize, this.mSpec, this.mCertificateSubject, this.mCertificateSerialNumber, this.mCertificateNotBefore, this.mCertificateNotAfter, this.mKeyValidityStart, this.mKeyValidityForOriginationEnd, this.mKeyValidityForConsumptionEnd, this.mPurposes, this.mDigests, this.mEncryptionPaddings, this.mSignaturePaddings, this.mBlockModes, this.mRandomizedEncryptionRequired, this.mUserAuthenticationRequired, this.mUserAuthenticationValidityDurationSeconds, this.mUserPresenceRequired, this.mAttestationChallenge, this.mUniqueIdIncluded, this.mUserAuthenticationValidWhileOnBody, this.mInvalidatedByBiometricEnrollment, this.mIsStrongBoxBacked, this.mUserConfirmationRequired, this.mUnlockedDeviceRequired);
        }
    }
}
