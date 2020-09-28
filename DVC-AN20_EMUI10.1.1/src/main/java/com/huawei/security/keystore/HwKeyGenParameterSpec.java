package com.huawei.security.keystore;

import android.security.keystore.KeyGenParameterSpec;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.Optional;
import javax.security.auth.x500.X500Principal;

public final class HwKeyGenParameterSpec implements AlgorithmParameterSpec, UserAuthArgs {
    private static final Date DEFAULT_CERT_NOT_AFTER = new Date(2461449600000L);
    private static final Date DEFAULT_CERT_NOT_BEFORE = new Date(0);
    private static final BigInteger DEFAULT_CERT_SERIAL_NUMBER = new BigInteger("1");
    private static final X500Principal DEFAULT_CERT_SUBJECT = new X500Principal("CN=fake");
    public static final long INVALID_SECURE_USER_ID = 0;
    private final byte[] mAttestationChallenge;
    private final String[] mBlockModes;
    private final Date mCertificateNotAfter;
    private final Date mCertificateNotBefore;
    private final BigInteger mCertificateSerialNumber;
    private final X500Principal mCertificateSubject;
    private final String[] mDigests;
    private final String[] mEncryptionPaddings;
    private final boolean mInvalidatedByBiometricEnrollment;
    private final boolean mIsAdditionalProtectionAllowed;
    private final boolean mIsInvalidatedBySystemRooting;
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

    public HwKeyGenParameterSpec(Builder builder) {
        if (!TextUtils.isEmpty(builder.mKeystoreAlias)) {
            builder.mCertificateSubject = (X500Principal) Optional.ofNullable(builder.mCertificateSubject).orElse(DEFAULT_CERT_SUBJECT);
            builder.mCertificateNotBefore = (Date) Optional.ofNullable(builder.mCertificateNotBefore).orElse(DEFAULT_CERT_NOT_BEFORE);
            builder.mCertificateNotAfter = (Date) Optional.ofNullable(builder.mCertificateNotAfter).orElse(DEFAULT_CERT_NOT_AFTER);
            builder.mCertificateSerialNumber = (BigInteger) Optional.ofNullable(builder.mCertificateSerialNumber).orElse(DEFAULT_CERT_SERIAL_NUMBER);
            if (!builder.mCertificateNotAfter.before(builder.mCertificateNotBefore)) {
                this.mKeystoreAlias = builder.mKeystoreAlias;
                this.mUid = builder.mUid;
                this.mKeySize = builder.mKeySize;
                this.mSpec = builder.mSpec;
                this.mCertificateSubject = builder.mCertificateSubject;
                this.mCertificateSerialNumber = builder.mCertificateSerialNumber;
                this.mCertificateNotBefore = ArrayUtils.cloneIfNotNull(builder.mCertificateNotBefore);
                this.mCertificateNotAfter = ArrayUtils.cloneIfNotNull(builder.mCertificateNotAfter);
                this.mKeyValidityStart = ArrayUtils.cloneIfNotNull(builder.mKeyValidityStart);
                this.mKeyValidityForOriginationEnd = ArrayUtils.cloneIfNotNull(builder.mKeyValidityForOriginationEnd);
                this.mKeyValidityForConsumptionEnd = ArrayUtils.cloneIfNotNull(builder.mKeyValidityForConsumptionEnd);
                this.mPurposes = builder.mPurposes;
                this.mDigests = ArrayUtils.cloneIfNotEmpty(builder.mDigests);
                this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(builder.mEncryptionPaddings));
                this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(builder.mSignaturePaddings));
                this.mBlockModes = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(builder.mBlockModes));
                this.mRandomizedEncryptionRequired = builder.mRandomizedEncryptionRequired;
                this.mUserAuthenticationRequired = builder.mUserAuthenticationRequired;
                this.mUserPresenceRequired = builder.mUserPresenceRequired;
                this.mUserAuthenticationValidityDurationSeconds = builder.mUserAuthenticationValidityDurationSeconds;
                this.mAttestationChallenge = ArrayUtils.cloneIfNotNull(builder.mAttestationChallenge);
                this.mIsAdditionalProtectionAllowed = builder.mIsAdditionalProtectionAllowed;
                this.mUniqueIdIncluded = builder.mUniqueIdIncluded;
                this.mUserAuthenticationValidWhileOnBody = builder.mUserAuthenticationValidWhileOnBody;
                this.mInvalidatedByBiometricEnrollment = builder.mInvalidatedByBiometricEnrollment;
                this.mIsStrongBoxBacked = builder.mIsStrongBoxBacked;
                this.mUserConfirmationRequired = builder.mUserConfirmationRequired;
                this.mUnlockedDeviceRequired = builder.mUnlockedDeviceRequired;
                this.mIsInvalidatedBySystemRooting = builder.mIsInvalidatedBySystemRooting;
                return;
            }
            throw new IllegalArgumentException("certificateNotAfter < certificateNotBefore");
        }
        throw new IllegalArgumentException("keyStoreAlias must not be empty");
    }

    public static HwKeyGenParameterSpec getInstance(Builder builder) {
        return new HwKeyGenParameterSpec(builder);
    }

    @NonNull
    public String getKeystoreAlias() {
        return this.mKeystoreAlias;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getKeySize() {
        return this.mKeySize;
    }

    @Nullable
    public AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return this.mSpec;
    }

    @NonNull
    public X500Principal getCertificateSubject() {
        return this.mCertificateSubject;
    }

    @NonNull
    public BigInteger getCertificateSerialNumber() {
        return this.mCertificateSerialNumber;
    }

    @NonNull
    public Date getCertificateNotBefore() {
        return ArrayUtils.cloneIfNotNull(this.mCertificateNotBefore);
    }

    @NonNull
    public Date getCertificateNotAfter() {
        return ArrayUtils.cloneIfNotNull(this.mCertificateNotAfter);
    }

    @Nullable
    public Date getKeyValidityStart() {
        return ArrayUtils.cloneIfNotNull(this.mKeyValidityStart);
    }

    @Nullable
    public Date getKeyValidityForConsumptionEnd() {
        return ArrayUtils.cloneIfNotNull(this.mKeyValidityForConsumptionEnd);
    }

    @Nullable
    public Date getKeyValidityForOriginationEnd() {
        return ArrayUtils.cloneIfNotNull(this.mKeyValidityForOriginationEnd);
    }

    public int getPurposes() {
        return this.mPurposes;
    }

    @NonNull
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

    @NonNull
    public String[] getEncryptionPaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mEncryptionPaddings);
    }

    @NonNull
    public String[] getSignaturePaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mSignaturePaddings);
    }

    @NonNull
    public String[] getBlockModes() {
        return ArrayUtils.cloneIfNotEmpty(this.mBlockModes);
    }

    public boolean isRandomizedEncryptionRequired() {
        return this.mRandomizedEncryptionRequired;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isUserAuthenticationRequired() {
        return this.mUserAuthenticationRequired;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isUserConfirmationRequired() {
        return this.mUserConfirmationRequired;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public int getUserAuthenticationValidityDurationSeconds() {
        return this.mUserAuthenticationValidityDurationSeconds;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isUserPresenceRequired() {
        return this.mUserPresenceRequired;
    }

    public byte[] getAttestationChallenge() {
        return ArrayUtils.cloneIfNotNull(this.mAttestationChallenge);
    }

    public boolean isUniqueIdIncluded() {
        return this.mUniqueIdIncluded;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isUserAuthenticationValidWhileOnBody() {
        return this.mUserAuthenticationValidWhileOnBody;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isInvalidatedByBiometricEnrollment() {
        return this.mInvalidatedByBiometricEnrollment;
    }

    public boolean isStrongBoxBacked() {
        return this.mIsStrongBoxBacked;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
    public boolean isUnlockedDeviceRequired() {
        return this.mUnlockedDeviceRequired;
    }

    public boolean isAdditionalProtectionAllowed() {
        return this.mIsAdditionalProtectionAllowed;
    }

    public boolean isInvalidatedBySystemRooting() {
        return this.mIsInvalidatedBySystemRooting;
    }

    @Override // com.huawei.security.keystore.UserAuthArgs
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
        private boolean mIsAdditionalProtectionAllowed;
        private boolean mIsInvalidatedBySystemRooting;
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

        public Builder(@Nullable String keystoreAlias, int purposes) {
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
            this.mIsAdditionalProtectionAllowed = false;
            this.mIsInvalidatedBySystemRooting = false;
            if (keystoreAlias == null) {
                throw new IllegalArgumentException("keystoreAlias == null");
            } else if (!keystoreAlias.isEmpty()) {
                this.mKeystoreAlias = keystoreAlias;
                this.mPurposes = purposes;
            } else {
                throw new IllegalArgumentException("keystoreAlias must not be empty");
            }
        }

        public Builder(@NonNull HwKeyGenParameterSpec sourceSpec) {
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
            this.mIsAdditionalProtectionAllowed = sourceSpec.isAdditionalProtectionAllowed();
            this.mIsInvalidatedBySystemRooting = sourceSpec.isInvalidatedBySystemRooting();
        }

        public Builder(KeyGenParameterSpec params) {
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
            this.mIsAdditionalProtectionAllowed = false;
            this.mIsInvalidatedBySystemRooting = false;
            this.mKeystoreAlias = params.getKeystoreAlias();
            this.mUid = -1;
            this.mKeySize = params.getKeySize();
            this.mSpec = params.getAlgorithmParameterSpec();
            this.mCertificateSubject = params.getCertificateSubject();
            this.mCertificateSerialNumber = params.getCertificateSerialNumber();
            this.mCertificateNotBefore = params.getCertificateNotBefore();
            this.mCertificateNotAfter = params.getCertificateNotAfter();
            this.mKeyValidityStart = params.getKeyValidityStart();
            this.mKeyValidityForOriginationEnd = params.getKeyValidityForOriginationEnd();
            this.mKeyValidityForConsumptionEnd = params.getKeyValidityForConsumptionEnd();
            this.mPurposes = params.getPurposes();
            this.mDigests = params.getDigests();
            this.mEncryptionPaddings = params.getEncryptionPaddings();
            this.mSignaturePaddings = params.getSignaturePaddings();
            this.mBlockModes = params.getBlockModes();
            this.mRandomizedEncryptionRequired = params.isRandomizedEncryptionRequired();
            this.mUserAuthenticationRequired = params.isUserAuthenticationRequired();
            this.mUserAuthenticationValidityDurationSeconds = params.getUserAuthenticationValidityDurationSeconds();
            this.mUserPresenceRequired = false;
            this.mAttestationChallenge = params.getAttestationChallenge();
            this.mUniqueIdIncluded = false;
            this.mUserAuthenticationValidWhileOnBody = params.isUserAuthenticationValidWhileOnBody();
            this.mInvalidatedByBiometricEnrollment = params.isInvalidatedByBiometricEnrollment();
            this.mIsStrongBoxBacked = false;
            this.mUserConfirmationRequired = false;
            this.mUnlockedDeviceRequired = false;
            this.mIsAdditionalProtectionAllowed = false;
            this.mIsInvalidatedBySystemRooting = false;
        }

        @NonNull
        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        @NonNull
        public Builder setKeySize(int keySize) {
            if (keySize >= 0) {
                this.mKeySize = keySize;
                return this;
            }
            throw new IllegalArgumentException("keySize < 0");
        }

        public Builder setAlgorithmParameterSpec(@NonNull AlgorithmParameterSpec spec) {
            if (spec != null) {
                this.mSpec = spec;
                return this;
            }
            throw new NullPointerException("spec == null");
        }

        @NonNull
        public Builder setCertificateSubject(@NonNull X500Principal subject) {
            if (subject != null) {
                this.mCertificateSubject = subject;
                return this;
            }
            throw new NullPointerException("subject == null");
        }

        @NonNull
        public Builder setCertificateSerialNumber(@NonNull BigInteger serialNumber) {
            if (serialNumber != null) {
                this.mCertificateSerialNumber = serialNumber;
                return this;
            }
            throw new NullPointerException("serialNumber == null");
        }

        @NonNull
        public Builder setCertificateNotBefore(@NonNull Date date) {
            if (date != null) {
                this.mCertificateNotBefore = ArrayUtils.cloneIfNotNull(date);
                return this;
            }
            throw new NullPointerException("date == null");
        }

        @NonNull
        public Builder setCertificateNotAfter(@NonNull Date date) {
            if (date != null) {
                this.mCertificateNotAfter = ArrayUtils.cloneIfNotNull(date);
                return this;
            }
            throw new NullPointerException("date == null");
        }

        @NonNull
        public Builder setKeyValidityStart(Date startDate) {
            this.mKeyValidityStart = ArrayUtils.cloneIfNotNull(startDate);
            return this;
        }

        @NonNull
        public Builder setKeyValidityEnd(Date endDate) {
            setKeyValidityForOriginationEnd(endDate);
            setKeyValidityForConsumptionEnd(endDate);
            return this;
        }

        @NonNull
        public Builder setKeyValidityForOriginationEnd(Date endDate) {
            this.mKeyValidityForOriginationEnd = ArrayUtils.cloneIfNotNull(endDate);
            return this;
        }

        @NonNull
        public Builder setKeyValidityForConsumptionEnd(Date endDate) {
            this.mKeyValidityForConsumptionEnd = ArrayUtils.cloneIfNotNull(endDate);
            return this;
        }

        @NonNull
        public Builder setDigests(String... digests) {
            this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
            return this;
        }

        @NonNull
        public Builder setEncryptionPaddings(String... paddings) {
            this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        @NonNull
        public Builder setSignaturePaddings(String... paddings) {
            this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        @NonNull
        public Builder setBlockModes(String... blockModes) {
            this.mBlockModes = ArrayUtils.cloneIfNotEmpty(blockModes);
            return this;
        }

        @NonNull
        public Builder setRandomizedEncryptionRequired(boolean required) {
            this.mRandomizedEncryptionRequired = required;
            return this;
        }

        @NonNull
        public Builder setUserAuthenticationRequired(boolean required) {
            this.mUserAuthenticationRequired = required;
            return this;
        }

        @NonNull
        public Builder setUserConfirmationRequired(boolean required) {
            this.mUserConfirmationRequired = required;
            return this;
        }

        @NonNull
        public Builder setUserAuthenticationValidityDurationSeconds(@IntRange(from = -1) int seconds) {
            if (seconds >= -1) {
                this.mUserAuthenticationValidityDurationSeconds = seconds;
                return this;
            }
            throw new IllegalArgumentException("seconds must be -1 or larger");
        }

        @NonNull
        public Builder setUserPresenceRequired(boolean required) {
            this.mUserPresenceRequired = required;
            return this;
        }

        @NonNull
        public Builder setAttestationChallenge(byte[] attestationChallenge) {
            this.mAttestationChallenge = ArrayUtils.cloneIfNotNull(attestationChallenge);
            return this;
        }

        @NonNull
        public Builder setUniqueIdIncluded(boolean uniqueIdIncluded) {
            this.mUniqueIdIncluded = uniqueIdIncluded;
            return this;
        }

        @NonNull
        public Builder setUserAuthenticationValidWhileOnBody(boolean remainsValid) {
            this.mUserAuthenticationValidWhileOnBody = remainsValid;
            return this;
        }

        @NonNull
        public Builder setInvalidatedByBiometricEnrollment(boolean invalidateKey) {
            this.mInvalidatedByBiometricEnrollment = invalidateKey;
            return this;
        }

        @NonNull
        public Builder setIsStrongBoxBacked(boolean isStrongBoxBacked) {
            this.mIsStrongBoxBacked = isStrongBoxBacked;
            return this;
        }

        @NonNull
        public Builder setUnlockedDeviceRequired(boolean unlockedDeviceRequired) {
            this.mUnlockedDeviceRequired = unlockedDeviceRequired;
            return this;
        }

        @NonNull
        public Builder setIsAddtionalProtectionAllowed(boolean allowed) {
            this.mIsAdditionalProtectionAllowed = allowed;
            return this;
        }

        @NonNull
        public Builder setInvalidatedBySystemRooting(boolean needed) {
            this.mIsInvalidatedBySystemRooting = needed;
            return this;
        }

        @NonNull
        public HwKeyGenParameterSpec build() {
            return new HwKeyGenParameterSpec(this);
        }
    }
}
