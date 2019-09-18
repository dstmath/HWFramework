package android.security.keystore;

import java.security.KeyStore;
import java.util.Date;

public final class KeyProtection implements KeyStore.ProtectionParameter, UserAuthArgs {
    private final String[] mBlockModes;
    private final long mBoundToSecureUserId;
    private final boolean mCriticalToDeviceEncryption;
    private final String[] mDigests;
    private final String[] mEncryptionPaddings;
    private final boolean mInvalidatedByBiometricEnrollment;
    private final boolean mIsStrongBoxBacked;
    private final Date mKeyValidityForConsumptionEnd;
    private final Date mKeyValidityForOriginationEnd;
    private final Date mKeyValidityStart;
    private final int mPurposes;
    private final boolean mRandomizedEncryptionRequired;
    private final boolean mRollbackResistant;
    private final String[] mSignaturePaddings;
    private final boolean mUnlockedDeviceRequired;
    private final boolean mUserAuthenticationRequired;
    private final boolean mUserAuthenticationValidWhileOnBody;
    private final int mUserAuthenticationValidityDurationSeconds;
    private final boolean mUserConfirmationRequired;
    private final boolean mUserPresenceRequred;

    public static final class Builder {
        private String[] mBlockModes;
        private long mBoundToSecureUserId = 0;
        private boolean mCriticalToDeviceEncryption = false;
        private String[] mDigests;
        private String[] mEncryptionPaddings;
        private boolean mInvalidatedByBiometricEnrollment = true;
        private boolean mIsStrongBoxBacked = false;
        private Date mKeyValidityForConsumptionEnd;
        private Date mKeyValidityForOriginationEnd;
        private Date mKeyValidityStart;
        private int mPurposes;
        private boolean mRandomizedEncryptionRequired = true;
        private boolean mRollbackResistant = false;
        private String[] mSignaturePaddings;
        private boolean mUnlockedDeviceRequired = false;
        private boolean mUserAuthenticationRequired;
        private boolean mUserAuthenticationValidWhileOnBody;
        private int mUserAuthenticationValidityDurationSeconds = -1;
        private boolean mUserConfirmationRequired;
        private boolean mUserPresenceRequired = false;

        public Builder(int purposes) {
            this.mPurposes = purposes;
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

        public Builder setEncryptionPaddings(String... paddings) {
            this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        public Builder setSignaturePaddings(String... paddings) {
            this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(paddings);
            return this;
        }

        public Builder setDigests(String... digests) {
            this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
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

        public Builder setUserAuthenticationValidWhileOnBody(boolean remainsValid) {
            this.mUserAuthenticationValidWhileOnBody = remainsValid;
            return this;
        }

        public Builder setInvalidatedByBiometricEnrollment(boolean invalidateKey) {
            this.mInvalidatedByBiometricEnrollment = invalidateKey;
            return this;
        }

        public Builder setBoundToSpecificSecureUserId(long secureUserId) {
            this.mBoundToSecureUserId = secureUserId;
            return this;
        }

        public Builder setCriticalToDeviceEncryption(boolean critical) {
            this.mCriticalToDeviceEncryption = critical;
            return this;
        }

        public Builder setUnlockedDeviceRequired(boolean unlockedDeviceRequired) {
            this.mUnlockedDeviceRequired = unlockedDeviceRequired;
            return this;
        }

        public Builder setRollbackResistant(boolean rollbackResistant) {
            this.mRollbackResistant = rollbackResistant;
            return this;
        }

        public Builder setIsStrongBoxBacked(boolean isStrongBoxBacked) {
            this.mIsStrongBoxBacked = isStrongBoxBacked;
            return this;
        }

        public KeyProtection build() {
            Date date = this.mKeyValidityStart;
            Date date2 = this.mKeyValidityForOriginationEnd;
            Date date3 = this.mKeyValidityForConsumptionEnd;
            int i = this.mPurposes;
            String[] strArr = this.mEncryptionPaddings;
            String[] strArr2 = this.mSignaturePaddings;
            String[] strArr3 = this.mDigests;
            String[] strArr4 = this.mBlockModes;
            boolean z = this.mRandomizedEncryptionRequired;
            boolean z2 = this.mUserAuthenticationRequired;
            int i2 = this.mUserAuthenticationValidityDurationSeconds;
            boolean z3 = this.mUserPresenceRequired;
            boolean z4 = this.mUserAuthenticationValidWhileOnBody;
            boolean z5 = this.mInvalidatedByBiometricEnrollment;
            long j = this.mBoundToSecureUserId;
            boolean z6 = this.mCriticalToDeviceEncryption;
            long j2 = j;
            boolean z7 = this.mUserConfirmationRequired;
            boolean z8 = this.mUnlockedDeviceRequired;
            boolean z9 = z8;
            long j3 = j2;
            boolean z10 = z4;
            boolean z11 = z5;
            KeyProtection keyProtection = new KeyProtection(date, date2, date3, i, strArr, strArr2, strArr3, strArr4, z, z2, i2, z3, z10, z11, j3, z6, z7, z9, this.mRollbackResistant, this.mIsStrongBoxBacked);
            return keyProtection;
        }
    }

    private KeyProtection(Date keyValidityStart, Date keyValidityForOriginationEnd, Date keyValidityForConsumptionEnd, int purposes, String[] encryptionPaddings, String[] signaturePaddings, String[] digests, String[] blockModes, boolean randomizedEncryptionRequired, boolean userAuthenticationRequired, int userAuthenticationValidityDurationSeconds, boolean userPresenceRequred, boolean userAuthenticationValidWhileOnBody, boolean invalidatedByBiometricEnrollment, long boundToSecureUserId, boolean criticalToDeviceEncryption, boolean userConfirmationRequired, boolean unlockedDeviceRequired, boolean isStrongBoxBacked) {
        this.mKeyValidityStart = Utils.cloneIfNotNull(keyValidityStart);
        this.mKeyValidityForOriginationEnd = Utils.cloneIfNotNull(keyValidityForOriginationEnd);
        this.mKeyValidityForConsumptionEnd = Utils.cloneIfNotNull(keyValidityForConsumptionEnd);
        this.mPurposes = purposes;
        this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(encryptionPaddings));
        this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(signaturePaddings));
        this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
        this.mBlockModes = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(blockModes));
        this.mRandomizedEncryptionRequired = randomizedEncryptionRequired;
        this.mUserAuthenticationRequired = userAuthenticationRequired;
        this.mUserAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
        this.mUserPresenceRequred = userPresenceRequred;
        this.mUserAuthenticationValidWhileOnBody = userAuthenticationValidWhileOnBody;
        this.mInvalidatedByBiometricEnrollment = invalidatedByBiometricEnrollment;
        this.mBoundToSecureUserId = boundToSecureUserId;
        this.mCriticalToDeviceEncryption = criticalToDeviceEncryption;
        this.mUserConfirmationRequired = userConfirmationRequired;
        this.mUnlockedDeviceRequired = unlockedDeviceRequired;
        this.mRollbackResistant = false;
        this.mIsStrongBoxBacked = isStrongBoxBacked;
    }

    private KeyProtection(Date keyValidityStart, Date keyValidityForOriginationEnd, Date keyValidityForConsumptionEnd, int purposes, String[] encryptionPaddings, String[] signaturePaddings, String[] digests, String[] blockModes, boolean randomizedEncryptionRequired, boolean userAuthenticationRequired, int userAuthenticationValidityDurationSeconds, boolean userPresenceRequred, boolean userAuthenticationValidWhileOnBody, boolean invalidatedByBiometricEnrollment, long boundToSecureUserId, boolean criticalToDeviceEncryption, boolean userConfirmationRequired, boolean unlockedDeviceRequired, boolean rollbackResistant, boolean isStrongBoxBacked) {
        this.mKeyValidityStart = Utils.cloneIfNotNull(keyValidityStart);
        this.mKeyValidityForOriginationEnd = Utils.cloneIfNotNull(keyValidityForOriginationEnd);
        this.mKeyValidityForConsumptionEnd = Utils.cloneIfNotNull(keyValidityForConsumptionEnd);
        this.mPurposes = purposes;
        this.mEncryptionPaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(encryptionPaddings));
        this.mSignaturePaddings = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(signaturePaddings));
        this.mDigests = ArrayUtils.cloneIfNotEmpty(digests);
        this.mBlockModes = ArrayUtils.cloneIfNotEmpty(ArrayUtils.nullToEmpty(blockModes));
        this.mRandomizedEncryptionRequired = randomizedEncryptionRequired;
        this.mUserAuthenticationRequired = userAuthenticationRequired;
        this.mUserAuthenticationValidityDurationSeconds = userAuthenticationValidityDurationSeconds;
        this.mUserPresenceRequred = userPresenceRequred;
        this.mUserAuthenticationValidWhileOnBody = userAuthenticationValidWhileOnBody;
        this.mInvalidatedByBiometricEnrollment = invalidatedByBiometricEnrollment;
        this.mBoundToSecureUserId = boundToSecureUserId;
        this.mCriticalToDeviceEncryption = criticalToDeviceEncryption;
        this.mUserConfirmationRequired = userConfirmationRequired;
        this.mUnlockedDeviceRequired = unlockedDeviceRequired;
        this.mRollbackResistant = rollbackResistant;
        this.mIsStrongBoxBacked = isStrongBoxBacked;
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

    public String[] getEncryptionPaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mEncryptionPaddings);
    }

    public String[] getSignaturePaddings() {
        return ArrayUtils.cloneIfNotEmpty(this.mSignaturePaddings);
    }

    public String[] getDigests() {
        if (this.mDigests != null) {
            return ArrayUtils.cloneIfNotEmpty(this.mDigests);
        }
        throw new IllegalStateException("Digests not specified");
    }

    public boolean isDigestsSpecified() {
        return this.mDigests != null;
    }

    public String[] getBlockModes() {
        return ArrayUtils.cloneIfNotEmpty(this.mBlockModes);
    }

    public boolean isRandomizedEncryptionRequired() {
        return this.mRandomizedEncryptionRequired;
    }

    public boolean isUserAuthenticationRequired() {
        return this.mUserAuthenticationRequired;
    }

    public boolean isUserConfirmationRequired() {
        return this.mUserConfirmationRequired;
    }

    public int getUserAuthenticationValidityDurationSeconds() {
        return this.mUserAuthenticationValidityDurationSeconds;
    }

    public boolean isUserPresenceRequired() {
        return this.mUserPresenceRequred;
    }

    public boolean isUserAuthenticationValidWhileOnBody() {
        return this.mUserAuthenticationValidWhileOnBody;
    }

    public boolean isInvalidatedByBiometricEnrollment() {
        return this.mInvalidatedByBiometricEnrollment;
    }

    public long getBoundToSpecificSecureUserId() {
        return this.mBoundToSecureUserId;
    }

    public boolean isCriticalToDeviceEncryption() {
        return this.mCriticalToDeviceEncryption;
    }

    public boolean isUnlockedDeviceRequired() {
        return this.mUnlockedDeviceRequired;
    }

    public boolean isRollbackResistant() {
        return this.mRollbackResistant;
    }

    public boolean isStrongBoxBacked() {
        return this.mIsStrongBoxBacked;
    }
}
