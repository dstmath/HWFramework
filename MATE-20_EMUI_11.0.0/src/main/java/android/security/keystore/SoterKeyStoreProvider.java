package android.security.keystore;

import com.huawei.security.keystore.HwUniversalKeyStoreProvider;

public final class SoterKeyStoreProvider extends HwUniversalKeyStoreProvider {
    private SoterKeyStoreProvider() {
    }

    public static void install() {
        com.tencent.mm.security.keystore.SoterKeyStoreProvider.install();
    }
}
