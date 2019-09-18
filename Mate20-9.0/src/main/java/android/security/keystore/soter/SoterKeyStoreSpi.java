package android.security.keystore.soter;

import android.security.keystore.SoterKeyStoreProvider;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keystore.HwUniversalKeyStoreSpi;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class SoterKeyStoreSpi extends HwUniversalKeyStoreSpi {
    public static final String TAG = "HwUniversalKeyStore";

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!isPrivateKeyEntry(alias)) {
            return null;
        }
        String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        if (password == null || !"from_soter_ui".equals(String.valueOf(password))) {
            return SoterKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(getKeyStoreManager(), privateKeyAlias, getUid());
        }
        return SoterKeyStoreProvider.loadAndroidKeyStorePublicKeyFromKeystore(getKeyStoreManager(), privateKeyAlias, getUid(), 1);
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager keyStoreManager = getKeyStoreManager();
            return keyStoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, getUid());
        }
        throw new NullPointerException("alias == null");
    }
}
