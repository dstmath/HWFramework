package android.security.keystore.soter;

import java.math.BigInteger;
import java.security.spec.RSAKeyGenParameterSpec;

public class SoterRSAKeyGenParameterSpec extends RSAKeyGenParameterSpec {
    private boolean isAutoAddCounterWhenGetPublicKey;
    private boolean isAutoSignedWithCommonkWhenGetPublicKey;
    private boolean isForSoter;
    private boolean isNeedUseNextAttk;
    private boolean isSecmsgFidCounterSignedWhenSign;
    private String mAutoSignedKeyNameWhenGetPublicKey;

    public SoterRSAKeyGenParameterSpec(int keysize, BigInteger publicExponent, boolean isForSoter2, boolean isAutoSignedWithCommonkWhenGetPublicKey2, String signedKeyNameWhenGetPublicKey, boolean isSecmsgFidCounterSignedWhenSign2, boolean isAutoAddCounterWhenGetPublicKey2, boolean isNeedNextAttk) {
        super(keysize, publicExponent);
        this.isForSoter = false;
        this.isAutoSignedWithCommonkWhenGetPublicKey = false;
        this.mAutoSignedKeyNameWhenGetPublicKey = "";
        this.isSecmsgFidCounterSignedWhenSign = false;
        this.isAutoAddCounterWhenGetPublicKey = false;
        this.isNeedUseNextAttk = false;
        this.isForSoter = isForSoter2;
        this.isAutoSignedWithCommonkWhenGetPublicKey = isAutoSignedWithCommonkWhenGetPublicKey2;
        this.mAutoSignedKeyNameWhenGetPublicKey = signedKeyNameWhenGetPublicKey;
        this.isSecmsgFidCounterSignedWhenSign = isSecmsgFidCounterSignedWhenSign2;
        this.isAutoAddCounterWhenGetPublicKey = isAutoAddCounterWhenGetPublicKey2;
        this.isNeedUseNextAttk = isNeedNextAttk;
    }

    public SoterRSAKeyGenParameterSpec(boolean isForSoter2, boolean isAutoSignedWithCommonkWhenGetPublicKey2, String signedKeyNameWhenGetPublicKey, boolean isSecmsgFidCounterSignedWhenSign2, boolean isAutoAddCounterWhenGetPubli, boolean isNeedNextAttkcKey) {
        this(2048, RSAKeyGenParameterSpec.F4, isForSoter2, isAutoSignedWithCommonkWhenGetPublicKey2, signedKeyNameWhenGetPublicKey, isSecmsgFidCounterSignedWhenSign2, isAutoAddCounterWhenGetPubli, isNeedNextAttkcKey);
    }

    public boolean isForSoter() {
        return this.isForSoter;
    }

    public void setIsForSoter(boolean isForSoter2) {
        this.isForSoter = isForSoter2;
    }

    public boolean isAutoSignedWithCommonkWhenGetPublicKey() {
        return this.isAutoSignedWithCommonkWhenGetPublicKey;
    }

    public void setIsAutoSignedWithCommonkWhenGetPublicKey(boolean isAutoSignedWithCommonkWhenGetPublicKey2) {
        this.isAutoSignedWithCommonkWhenGetPublicKey = isAutoSignedWithCommonkWhenGetPublicKey2;
    }

    public String getAutoSignedKeyNameWhenGetPublicKey() {
        return this.mAutoSignedKeyNameWhenGetPublicKey;
    }

    public void setAutoSignedKeyNameWhenGetPublicKey(String mAutoSignedKeyNameWhenGetPublicKey2) {
        this.mAutoSignedKeyNameWhenGetPublicKey = mAutoSignedKeyNameWhenGetPublicKey2;
    }

    public boolean isSecmsgFidCounterSignedWhenSign() {
        return this.isSecmsgFidCounterSignedWhenSign;
    }

    public void setIsSecmsgFidCounterSignedWhenSign(boolean isSecmsgFidCounterSignedWhenSign2) {
        this.isSecmsgFidCounterSignedWhenSign = isSecmsgFidCounterSignedWhenSign2;
    }

    public boolean isAutoAddCounterWhenGetPublicKey() {
        return this.isAutoAddCounterWhenGetPublicKey;
    }

    public void setIsAutoAddCounterWhenGetPublicKey(boolean isAutoAddCounterWhenGetPublicKey2) {
        this.isAutoAddCounterWhenGetPublicKey = isAutoAddCounterWhenGetPublicKey2;
    }

    public boolean isNeedUseNextAttk() {
        return this.isNeedUseNextAttk;
    }

    public void setIsNeedUseNextAttk(boolean isNeedUseNextAttk2) {
        this.isNeedUseNextAttk = isNeedUseNextAttk2;
    }

    public String toString() {
        return "SoterRSAKeyGenParameterSpec{isForSoter=" + this.isForSoter + ", isAutoSignedWithCommonkWhenGetPublicKey=" + this.isAutoSignedWithCommonkWhenGetPublicKey + ", mAutoSignedKeyNameWhenGetPublicKey='" + this.mAutoSignedKeyNameWhenGetPublicKey + '\'' + ", isSecmsgFidCounterSignedWhenSign=" + this.isSecmsgFidCounterSignedWhenSign + ", isAutoAddCounterWhenGetPublicKey=" + this.isAutoAddCounterWhenGetPublicKey + ", isNeedUseNextAttk=" + this.isNeedUseNextAttk + '}';
    }
}
