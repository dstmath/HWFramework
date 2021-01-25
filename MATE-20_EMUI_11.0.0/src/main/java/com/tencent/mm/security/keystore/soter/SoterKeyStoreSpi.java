package com.tencent.mm.security.keystore.soter;

import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keystore.HwUniversalKeyStoreSpi;
import com.tencent.mm.security.keystore.SoterKeyStoreProvider;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class SoterKeyStoreSpi extends HwUniversalKeyStoreSpi {
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreSpi, java.security.KeyStoreSpi
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!isPrivateKeyEntry(alias)) {
            return null;
        }
        String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        if (password == null || !"from_soter_ui".equals(String.valueOf(password))) {
            return SoterKeyStoreProvider.loadHwKeyStorePrivateKeyFromKeystore(getKeyStoreManager(), privateKeyAlias, getUid());
        }
        return SoterKeyStoreProvider.loadHwKeyStorePublicKeyFromKeystore(getKeyStoreManager(), privateKeyAlias, getUid(), 1);
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager keyStoreManager = getKeyStoreManager();
            return keyStoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, getUid());
        }
        throw new NullPointerException("alias == null");
    }
}
