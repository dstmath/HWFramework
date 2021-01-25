package com.tencent.mm.security.keystore.soter;

import com.huawei.hwpartsecurity.BuildConfig;
import java.math.BigInteger;
import java.security.spec.RSAKeyGenParameterSpec;

public class SoterRSAKeyGenParameterSpec extends RSAKeyGenParameterSpec {
    private String mAutoSignedKeyNameWhenGetPublicKey;
    private boolean mIsAutoAddCounterWhenGetPublicKey;
    private boolean mIsAutoSignedWithCommonkWhenGetPublicKey;
    private boolean mIsForSoter;
    private boolean mIsNeedUseNextAttk;
    private boolean mIsSecmsgFidCounterSignedWhenSign;

    public SoterRSAKeyGenParameterSpec(int keySize, BigInteger publicExponent, Builder builder) {
        super(keySize, publicExponent);
        this.mAutoSignedKeyNameWhenGetPublicKey = BuildConfig.FLAVOR;
        this.mIsForSoter = builder.mIsForSoter;
        this.mIsAutoSignedWithCommonkWhenGetPublicKey = builder.mIsAutoSignedWithCommonkWhenGetPublicKey;
        this.mAutoSignedKeyNameWhenGetPublicKey = builder.mAutoSignedKeyNameWhenGetPublicKey;
        this.mIsSecmsgFidCounterSignedWhenSign = builder.mIsSecmsgFidCounterSignedWhenSign;
        this.mIsAutoAddCounterWhenGetPublicKey = builder.mIsAutoAddCounterWhenGetPublicKey;
        this.mIsNeedUseNextAttk = builder.mIsNeedUseNextAttk;
    }

    public SoterRSAKeyGenParameterSpec(Builder builder) {
        this(2048, RSAKeyGenParameterSpec.F4, builder);
    }

    public boolean isForSoter() {
        return this.mIsForSoter;
    }

    public void setIsForSoter(boolean isSoterKey) {
        this.mIsForSoter = isSoterKey;
    }

    public boolean isAutoSignedWithCommonkWhenGetPublicKey() {
        return this.mIsAutoSignedWithCommonkWhenGetPublicKey;
    }

    public void setIsAutoSignedWithCommonkWhenGetPublicKey(boolean isAutoSignedWithCommonKey) {
        this.mIsAutoSignedWithCommonkWhenGetPublicKey = isAutoSignedWithCommonKey;
    }

    public String getAutoSignedKeyNameWhenGetPublicKey() {
        return this.mAutoSignedKeyNameWhenGetPublicKey;
    }

    public void setAutoSignedKeyNameWhenGetPublicKey(String autoSignedKeyNameWhenGetPublicKey) {
        this.mAutoSignedKeyNameWhenGetPublicKey = autoSignedKeyNameWhenGetPublicKey;
    }

    public boolean isSecmsgFidCounterSignedWhenSign() {
        return this.mIsSecmsgFidCounterSignedWhenSign;
    }

    public void setIsSecmsgFidCounterSignedWhenSign(boolean isSecMsgFidCounterSigned) {
        this.mIsSecmsgFidCounterSignedWhenSign = isSecMsgFidCounterSigned;
    }

    public boolean isAutoAddCounterWhenGetPublicKey() {
        return this.mIsAutoAddCounterWhenGetPublicKey;
    }

    public void setIsAutoAddCounterWhenGetPublicKey(boolean isAutoAddCounter) {
        this.mIsAutoAddCounterWhenGetPublicKey = isAutoAddCounter;
    }

    public boolean isNeedUseNextAttk() {
        return this.mIsNeedUseNextAttk;
    }

    public void setIsNeedUseNextAttk(boolean isUseNextAttk) {
        this.mIsNeedUseNextAttk = isUseNextAttk;
    }

    @Override // java.lang.Object
    public String toString() {
        return "SoterRSAKeyGenParameterSpec{isForSoter=" + this.mIsForSoter + ", isAutoSignedWithCommonkWhenGetPublicKey=" + this.mIsAutoSignedWithCommonkWhenGetPublicKey + ", mAutoSignedKeyNameWhenGetPublicKey='" + this.mAutoSignedKeyNameWhenGetPublicKey + "', isSecmsgFidCounterSignedWhenSign=" + this.mIsSecmsgFidCounterSignedWhenSign + ", isAutoAddCounterWhenGetPublicKey=" + this.mIsAutoAddCounterWhenGetPublicKey + ", isNeedUseNextAttk=" + this.mIsNeedUseNextAttk + '}';
    }

    public static final class Builder {
        private String mAutoSignedKeyNameWhenGetPublicKey;
        private boolean mIsAutoAddCounterWhenGetPublicKey;
        private boolean mIsAutoSignedWithCommonkWhenGetPublicKey;
        private boolean mIsForSoter;
        private boolean mIsNeedUseNextAttk;
        private boolean mIsSecmsgFidCounterSignedWhenSign;

        public Builder setIsForSoter(boolean isForSoter) {
            this.mIsForSoter = isForSoter;
            return this;
        }

        public Builder setIsAutoSignedWithCommonkWhenGetPublicKey(boolean isAutoSignedWithCommonkWhenGetPublicKey) {
            this.mIsAutoSignedWithCommonkWhenGetPublicKey = isAutoSignedWithCommonkWhenGetPublicKey;
            return this;
        }

        public Builder setAutoSignedKeyNameWhenGetPublicKey(String autoSignedKeyNameWhenGetPublicKey) {
            this.mAutoSignedKeyNameWhenGetPublicKey = autoSignedKeyNameWhenGetPublicKey;
            return this;
        }

        public Builder setIsSecmsgFidCounterSignedWhenSign(boolean isSecmsgFidCounterSignedWhenSign) {
            this.mIsSecmsgFidCounterSignedWhenSign = isSecmsgFidCounterSignedWhenSign;
            return this;
        }

        public Builder setIsAutoAddCounterWhenGetPublicKey(boolean isAutoAddCounterWhenGetPublicKey) {
            this.mIsAutoAddCounterWhenGetPublicKey = isAutoAddCounterWhenGetPublicKey;
            return this;
        }

        public Builder setIsNeedUseNextAttk(boolean isNeedUseNextAttk) {
            this.mIsNeedUseNextAttk = isNeedUseNextAttk;
            return this;
        }

        public SoterRSAKeyGenParameterSpec build() {
            return new SoterRSAKeyGenParameterSpec(this);
        }
    }
}
