package android.security.keystore.soter;

import android.os.Process;
import android.security.keystore.SoterKeyStoreProvider;
import android.util.Log;
import com.huawei.security.HwCredentials;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.HwKeyGenParameterSpec;
import com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.ProviderException;
import java.security.UnrecoverableKeyException;

public class SoterKeyStoreKeyPairGeneratorSpi extends HwUniversalKeyStoreKeyPairGeneratorSpi {
    private static final String SOTER_DEFAULT_CHALLENGE = "counter";
    public static final String TAG = "HwSoterKeyPairGenerator";
    private boolean isAutoAddCounterWhenGetPublicKey = false;
    private boolean isAutoSignedWithCommonkWhenGetPublicKey = false;
    private boolean isNeedNextAttk = false;
    private boolean isSecmsgFidCounterSignedWhenSign = false;
    private String mAutoSignedKeyNameWhenGetPublicKey = "";
    private int mKeymasterAlgorithm = -1;

    public static class RSA extends SoterKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    protected SoterKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        super(keymasterAlgorithm);
    }

    /* access modifiers changed from: protected */
    public String getEntryAlias(String keystoreAlias) {
        SoterRSAKeyGenParameterSpec soterSpec = SoterUtil.convertKeyNameToParameterSpec(keystoreAlias);
        if (soterSpec == null || !soterSpec.isForSoter()) {
            throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
        this.isAutoSignedWithCommonkWhenGetPublicKey = soterSpec.isAutoSignedWithCommonkWhenGetPublicKey();
        this.mAutoSignedKeyNameWhenGetPublicKey = soterSpec.getAutoSignedKeyNameWhenGetPublicKey();
        this.isSecmsgFidCounterSignedWhenSign = soterSpec.isSecmsgFidCounterSignedWhenSign();
        this.isAutoAddCounterWhenGetPublicKey = soterSpec.isAutoAddCounterWhenGetPublicKey();
        this.isNeedNextAttk = soterSpec.isNeedUseNextAttk();
        return SoterUtil.getPureKeyAliasFromKeyName(keystoreAlias);
    }

    /* access modifiers changed from: protected */
    public KeyPair loadKeystoreKeyPair(String privateKeyAlias) throws ProviderException {
        try {
            Log.d(TAG, "generateKeyPair succeed");
            return SoterKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(getKeyStoreManager(), privateKeyAlias, getEntryUid());
        } catch (UnrecoverableKeyException e) {
            throw new ProviderException("Failed to load generated key pair from keystore", e);
        }
    }

    /* access modifiers changed from: protected */
    public void addExtraParameters(HwKeymasterArguments keymasterArgs) {
        super.addExtraParameters(keymasterArgs);
        keymasterArgs.addUnsignedInt(HwKeymasterDefs.KM_TAG_SOTER_UID, (long) Process.myUid());
        if (this.isAutoSignedWithCommonkWhenGetPublicKey) {
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_SOTER_IS_FROM_SOTER);
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_SOTER_IS_AUTO_SIGNED_WITH_COMMON_KEY_WHEN_GET_PUBLIC_KEY);
            if (!SoterUtil.isNullOrNil(this.mAutoSignedKeyNameWhenGetPublicKey)) {
                keymasterArgs.addBytes(HwKeymasterDefs.KM_TAG_SOTER_AUTO_SIGNED_COMMON_KEY_WHEN_GET_PUBLIC_KEY, (HwCredentials.USER_PRIVATE_KEY + this.mAutoSignedKeyNameWhenGetPublicKey).getBytes(StandardCharsets.UTF_8));
            }
        }
        if (this.isAutoAddCounterWhenGetPublicKey) {
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_SOTER_AUTO_ADD_COUNTER_WHEN_GET_PUBLIC_KEY);
        }
        if (this.isSecmsgFidCounterSignedWhenSign) {
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_SOTER_IS_SECMSG_FID_COUNTER_SIGNED_WHEN_SIGN);
        }
        if (this.isNeedNextAttk) {
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_SOTER_USE_NEXT_ATTK);
        }
    }

    /* access modifiers changed from: protected */
    public void resetAll() {
        super.resetAll();
        this.isAutoSignedWithCommonkWhenGetPublicKey = false;
        this.mAutoSignedKeyNameWhenGetPublicKey = "";
        this.isSecmsgFidCounterSignedWhenSign = false;
        this.isAutoAddCounterWhenGetPublicKey = false;
        this.isNeedNextAttk = false;
    }

    /* access modifiers changed from: protected */
    public byte[] getChallenge(HwKeyGenParameterSpec spec) {
        byte[] challenge = spec.getAttestationChallenge();
        if (challenge != null || (spec.getPurposes() & 16) == 0) {
            return challenge;
        }
        byte[] challenge2 = SOTER_DEFAULT_CHALLENGE.getBytes(StandardCharsets.UTF_8);
        Log.d(TAG, "set extra value");
        return challenge2;
    }
}
