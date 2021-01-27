package ohos.security.keystore.provider;

import java.security.Provider;
import java.security.Security;
import java.util.concurrent.Callable;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class HarmonyProviderLoader implements Callable<Boolean> {
    private static final int DEFAULT_INVALID_INDEX = -1;
    private static final HiLogLabel LABEL = KeyStoreLogger.getLabel(TAG);
    private static final String PROVIDER_ANDROID_BOUNCYCASTLE = "AndroidKeyStoreBCWorkaround";
    private static final String PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String PROVIDER_ANDROID_NETWORK = "AndroidNSSP";
    private static final String PROVIDER_ANDROID_OPENSSL = "AndroidOpenSSL";
    private static final String PROVIDER_BC = "BC";
    private static final String TAG = "HarmonyProviderLoader";

    @Override // java.util.concurrent.Callable
    public Boolean call() {
        HiLog.info(LABEL, "install harmony keystore start", new Object[0]);
        try {
            Security.removeProvider(PROVIDER_ANDROID_OPENSSL);
            Security.removeProvider(PROVIDER_ANDROID_BOUNCYCASTLE);
            Security.removeProvider(PROVIDER_ANDROID_KEYSTORE);
            Security.removeProvider(PROVIDER_ANDROID_NETWORK);
            Security.insertProviderAt(new HarmonyOpenSSLProvider(), 1);
            Provider[] providers = Security.getProviders();
            int i = 0;
            while (true) {
                if (i >= providers.length) {
                    i = -1;
                    break;
                } else if (PROVIDER_BC.equals(providers[i].getName())) {
                    break;
                } else {
                    i++;
                }
            }
            Security.addProvider(new HarmonyKeyStoreProvider());
            HarmonyKeyStoreBCWorkaroundProvider harmonyKeyStoreBCWorkaroundProvider = new HarmonyKeyStoreBCWorkaroundProvider();
            if (i != -1) {
                Security.insertProviderAt(harmonyKeyStoreBCWorkaroundProvider, i + 1);
            } else {
                Security.addProvider(harmonyKeyStoreBCWorkaroundProvider);
            }
            HiLog.info(LABEL, "install harmony keystore end", new Object[0]);
            return true;
        } catch (SecurityException unused) {
            HiLog.error(LABEL, "security manager exists and denies access to add or remove provider", new Object[0]);
            return false;
        } catch (Exception unused2) {
            HiLog.error(LABEL, "install keystore fail", new Object[0]);
            return false;
        }
    }
}
